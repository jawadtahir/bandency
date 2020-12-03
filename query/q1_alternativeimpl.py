import logging
import os
import pickle
from datetime import datetime, timedelta

import grpc
from google.protobuf import empty_pb2
from tqdm import tqdm

from event_processor import EventProcessor

from shapely.geometry import Point
from shapely.geometry.polygon import Polygon

import challenger_pb2 as ch
import challenger_pb2_grpc as api

import numpy as np
import utils


class QueryOneAlternative:
    def __init__(self, challengerstub):
        self.challengerstub = challengerstub
        self.location_to_city = {}
        self.zipcode_polygons = []
        self.data = {}
        self.avg_aqi = {}
        self.cachemiss = 0
        self.cachehit = 0
        self.movingaqi = {}
        self.id_lastyear = 1
        self.id_curr = 2
        self.nextSnapshot_curr = None
        self.nextsn = {}
        self.firstbatch = True

    def measureLatency(self, benchmark):
        ping = self.challengerstub.initializeLatencyMeasuring(benchmark)
        for i in range(10):
            ping = self.challengerstub.measure(ping)

        self.challengerstub.endMeasurement(ping)

    def setup_locations(self, location_info_list):
        print("Processing locations...")
        count = 0
        for location_info in tqdm(location_info_list.locations):
            polygons = location_info.polygons
            for polygon in polygons:
                obj_points = []
                for point in polygon.points:
                    obj_points.append(Point(point.longitude, point.latitude))

                polygon = Polygon(obj_points)
                # polygon.zipcode = location_info.zipcode
                self.zipcode_polygons.append([location_info.zipcode, polygon])

            count += 1

    def _resolve_location(self, event):
        location = (event.longitude, event.latitude)

        # lookup in cache
        if location in self.location_to_city:
            self.cachehit = self.cachehit + 1
            return self.location_to_city[location]

        # otherwise we have to search
        point = Point(event.longitude, event.latitude)
        for [city, polygon] in self.zipcode_polygons:
            if polygon.contains(point):
                self.cachemiss = self.cachemiss + 1
                self.location_to_city[location] = city
                return city

        # haven't found, hence we store it as unknown
        self.location_to_city[location] = None
        return None

    # todo: merge with process payloads
    def min_max_fromdate(self, payloads):
        dtmax = None
        dtmin = None
        for payload in payloads:
            ts = payload.timestamp
            dt = datetime.fromtimestamp(ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)
            if dtmax == None:
                dtmax = dt
            else:
                dtmax = max(dtmax, dt)

            if dtmin == None:
                dtmin = dt
            else:
                dtmin = min(dtmin, dt)

        return dtmin, dtmax

    def snapshot_aqi(self, year, ts):
        windowbegin = ts - timedelta(hours=24)
        if year not in self.avg_aqi:
            self.avg_aqi[year] = {}
        for (city, window) in self.data[year].items():
            window.resize(windowbegin)
            if city not in self.avg_aqi[year]:
                self.avg_aqi[year][city] = AverageSlidingWindow()

            if window.hasElements():
                mean_per_city = utils.EPATableCalc(window.getMean())
                if mean_per_city is not np.nan:
                    self.avg_aqi[year][city].add(ts, mean_per_city)
                self.avg_aqi[year][city].resize(windowbegin - timedelta(days=5))

    def process_payloads(self, year, payloads):
        if not year in self.data:
            self.data[year] = {}

        per_city = self.data[year]
        for payload in payloads:
            city = self._resolve_location(payload)
            if not city:  # outside
                continue

            if city not in per_city:
                per_city[city] = AverageSlidingWindow()

            ts = payload.timestamp
            dt = datetime.fromtimestamp(ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)
            if dt > self.nextsn[year]:  # do the snapshot
                if year == self.id_curr:
                    self.snapshot_aqi(self.id_curr, dt)
                    self.nextsn[self.id_curr] = self.next_ts(self.nextsn[year])

                    self.snapshot_aqi(self.id_lastyear, dt - timedelta(days=365))
                    self.nextsn[self.id_lastyear] = self.next_ts(self.nextsn[year])

                elif year == self.id_lastyear:
                    self.snapshot_aqi(self.id_curr, dt + timedelta(days=365))
                    self.nextsn[self.id_curr] = self.next_ts(self.nextsn[year])

                    self.snapshot_aqi(self.id_lastyear, dt)
                    self.nextsn[self.id_lastyear] = self.next_ts(self.nextsn[year])

            per_city[city].add(dt, payload.p2)

        self.data[year] = per_city

    def truncate_old_values(self, year, dt):
        mindate = dt - timedelta(hours=24)
        for (k, v) in self.data[year].items():
            v.resize(mindate)

    def calculate_epa_scores(self, year):
        if not year in self.data:
            return
        result = {}
        for (k, v) in self.data[year].items():
            mean_per_city = v.getMean()
            if mean_per_city is not np.nan:  # in case there are no sensor measurements
                result[k] = utils.EPATableCalc(mean_per_city)
        return result

    interval_minutes = 1
    last_interval_ts = None

    span_year = timedelta(days=365)

    def dt_comp(self, first, second, fun):
        if first is None and second is None:
            return None

        if first is None:
            return second
        if second is None:
            return first

        return fun(first, second)

    def next_ts(self, ts):
        return ts + timedelta(minutes=5)

    def max_timestamp(self, payloads):
        maxdt = None
        for payload in payloads:
            dt = datetime.fromtimestamp(payload.timestamp.seconds) + timedelta(
                microseconds=payload.timestamp.nanos / 1000.0)
            if maxdt is None:
                maxdt = dt
            else:
                maxdt = max(maxdt, dt)
        return maxdt

    def process(self, batch):
        if len(batch.current) == 0 and len(batch.lastyear) == 0:
            return None, None

        dtmax_curr = self.max_timestamp(batch.current)
        if not dtmax_curr:
            dtmax_curr = self.max_timestamp(batch.lastyear) + timedelta(days=365)

        dtmax_lastyear = dtmax_curr - timedelta(days=365)

        if len(self.nextsn) == 0:
            startminute = dtmax_curr.minute - (dtmax_curr.minute % 5)
            next_snap_curr = dtmax_curr.replace(minute=startminute, second=0, microsecond=0) + timedelta(minutes=5)
            self.nextsn[self.id_curr] = next_snap_curr
            self.nextsn[self.id_lastyear] = next_snap_curr - timedelta(days=365)

        self.process_payloads(self.id_curr, batch.current)
        self.process_payloads(self.id_lastyear, batch.lastyear)

        if self.firstbatch:
            self.snapshot_aqi(self.id_curr, dtmax_curr)
            self.snapshot_aqi(self.id_lastyear, dtmax_lastyear)
            self.firstbatch = False

        cnt = 1
        res = list()
        for (city, window_curr) in self.avg_aqi[self.id_curr].items():
            if city in self.avg_aqi[self.id_lastyear]:
                window_last = self.avg_aqi[self.id_lastyear][city]
                last_year_avg_aqi = window_curr.getMean()
                curr_year_avg_aqi = window_last.getMean()

                res.append((city, last_year_avg_aqi - curr_year_avg_aqi, last_year_avg_aqi, curr_year_avg_aqi))
                cnt = cnt + 1

        sort_res = sorted(res, key=lambda r: r[1], reverse=True)

        topk = 50

        topklist = list()
        for i in range(1, topk + 1):
            if len(sort_res) >= i:
                res = sort_res[i - 1]
                topklist.append(ch.TopKCities(position=i, city=res[0], averageAQIImprovement=res[1], currentAQI=res[2],
                                              previousAQI=res[3]))

        return (dtmax_curr, topklist)

    def run(self):
        event_proc = EventProcessor()

        locationfile = "locationcache5.pickle"
        locationcache = "locationlookupcache1.pickle"

        if os.path.exists(locationfile):
            with open(locationfile, "rb") as f:
                self.zipcode_polygons = pickle.load(f)
        else:
            loc = self.challengerstub.getLocations(empty_pb2.Empty())
            print('got location data: %s' % len(loc.locations))
            self.setup_locations(loc)
            with open(locationfile, "wb") as f:
                pickle.dump(self.zipcode_polygons, f)

        if os.path.exists(locationcache):
            with open(locationcache, "rb") as f:
                self.location_to_city = pickle.load(f)

        benchmarkconfiguration = ch.BenchmarkConfiguration(token="cpjcwuaeufgqqxhohhvqlyndjazvzymx",
                                                           batch_size=5000,
                                                           benchmark_name="test benchmark",
                                                           benchmark_type="test",
                                                           queries=[ch.BenchmarkConfiguration.Query.Q1])
        bench = self.challengerstub.createNewBenchmark(benchmarkconfiguration)

        # First, we measure the latency.
        # This is only for the testing dashboard to substract the communication latency
        self.measureLatency(bench)

        # start the benchmark
        print("start processing batch")
        start_time = datetime.now()
        self.challengerstub.startBenchmark(bench)
        batch = self.challengerstub.nextMessage(bench)

        num_current = 0
        num_historic = 0
        cnt = 0

        lastdisplay = 0

        while batch:
            if batch.last:
                break

            num_current += len(batch.current)
            num_historic += len(batch.lastyear)

            (dtmax_curr, payload) = self.process(batch)

            result = ch.ResultQ1(benchmark_id=bench.id, payload_seq_id=batch.seq_id, topk=payload)
            self.challengerstub.resultQ1(result)


            cnt = cnt + 1
            duration_so_far = (datetime.now() - start_time).total_seconds()
            if (duration_so_far - lastdisplay) >= 2:  # limit output every 2 seconds
                with open(locationcache, "wb") as f:
                    pickle.dump(self.location_to_city, f)

                os.system('clear')
                print("Top %s most improved zipcodes, last 24h - date: %s cachemiss: %s cachehit: %s cachsize: %s " % (
                    len(payload), dtmax_curr, self.cachemiss, self.cachehit, len(self.location_to_city)))
                print("processed %s in %s seconds - num_current: %s, num_historic: %s, total_events: %s" % (
                    cnt, duration_so_far, num_current, num_historic, (num_current + num_historic)))
                for topk in payload:
                    print("pos: %s, city: %s, avg improvement: %s, previous: %s, current: %s " % (
                        topk.position, topk.city, topk.averageAQIImprovement, topk.currentAQI, topk.previousAQI))

                lastdisplay = duration_so_far

            batch = self.challengerstub.nextMessage(bench)

    def process_current(self, batch):
        return


class AverageSlidingWindow:
    def __init__(self, window_hours=24):
        self.timed_window = list()
        self.csum = 0

    def add(self, timestamp: datetime, value):
        self.csum = self.csum + value
        self.timed_window.append((timestamp, value))

    def resize(self, min_treshold: datetime):
        while len(self.timed_window) > 0:
            first = self.timed_window[0]
            if first[0] < min_treshold:
                self.csum = self.csum - first[1]
                self.timed_window.pop(0)
            else:
                return

    def hasElements(self):
        return not len(self.timed_window) == 0

    def getMean(self):
        if len(self.timed_window) > 0:
            return self.csum / len(self.timed_window)
        else:
            return np.nan


def main():
    op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
          ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
    with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
        stub = api.ChallengerStub(channel)
        q1 = QueryOneAlternative(stub)
        q1.run()


if __name__ == "__main__":
    logging.basicConfig()
    main()
