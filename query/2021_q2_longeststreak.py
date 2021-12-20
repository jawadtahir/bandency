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

class MeanSlidingWindow:
    def __init__(self):
        self.timed_window = list()
        self.csum = 0

    def add(self, timestamp: datetime, value: float):
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

    def has_elements(self) -> bool:
        return not len(self.timed_window) == 0

    def active(self, dt: datetime) -> bool:
        if len(self.timed_window) == 0:
            return False
        else:
            return self.timed_window[len(self.timed_window) - 1][0] >= dt

    def size(self) -> int:
        return len(self.timed_window)

    def getMean(self) -> float:
        if len(self.timed_window) > 0:
            return self.csum / len(self.timed_window)
        else:
            return None


class QueryOneEventProcessor:
    def __init__(self):
        self.id_curr = 2
        self.id_lastyear = 1

        self.location_to_city = {}
        self.zipcode_polygons = []

        self.data = {self.id_curr: {}, self.id_lastyear: {}}
        self.avg_aqi = {self.id_curr: {}, self.id_lastyear: {}}

        self.streak = {}

        self.cachemiss = 0
        self.cachehit = 0
        self.movingaqi = {}
        self.nextSnapshot_curr = None
        self.next_snapshot = None

        self.interval_minutes = 1
        self.last_interval_ts = None
        self.span_year = timedelta(days=365)

    def setup_locations(self, location_info_list):
        for location_info in tqdm(location_info_list.locations):
            polygons = location_info.polygons
            for polygon in polygons:
                obj_points = []
                for point in polygon.points:
                    obj_points.append(Point(point.longitude, point.latitude))

                polygon = Polygon(obj_points)
                self.zipcode_polygons.append([location_info.zipcode, polygon])

    def _resolve_location(self, event):
        location = (event.longitude, event.latitude)

        # lookup in cache
        if location in self.location_to_city:
            return self.location_to_city[location]

        # otherwise we have to search
        point = Point(event.longitude, event.latitude)
        for [city, polygon] in self.zipcode_polygons:
            if polygon.contains(point):
                self.location_to_city[location] = city
                return city

        # haven't found the location in any polygon, hence we store it as unknown
        self.location_to_city[location] = None
        return None

    def snapshot_aqi(self, year, ts):
        if year not in self.avg_aqi:
            self.avg_aqi[year] = {}
        for (city, window) in self.data[year].items():
            window[1].resize(ts - timedelta(hours=24))
            window[2].resize(ts - timedelta(hours=24))
            if city not in self.avg_aqi[year]:
                self.avg_aqi[year][city] = MeanSlidingWindow()
            if city not in self.streak:
                self.streak[city] = ["bad", 0, None]

            if window[1].has_elements() and window[2].has_elements():
                mean_p1 = utils.EPATableCalc(window[1].getMean(), "P1")
                mean_p2 = utils.EPATableCalc(window[2].getMean(), "P2")

                if mean_p1 is not np.nan and mean_p2 is not np.nan:
                    avg = max(mean_p1, mean_p2)
                    self.avg_aqi[year][city].add(ts, avg) # this for Q1

                    if year is self.id_curr:
                        desc_p1 = utils.epaDescription(mean_p1, "P1")
                        desc_p2 = utils.epaDescription(mean_p2, "P2")

                        c_state = "bad"
                        if "good" in desc_p1 and "good" in desc_p2:
                            c_state = "good"

                        state, dtlongest, dtfrom = self.streak[city]

                        if state == "bad" and c_state == "good": #initialized with bad
                            self.streak[city][0] = "good"
                            self.streak[city][2] = ts
                        #if state == "good" and c_state == "good":
                            #span_seconds = max(dtlongest, )
                            #self.streak[city][1] = (ts - dtfrom).seconds
                        if state == "good" and c_state == "bad":
                            self.streak[city][0] = "bad"
                            self.streak[city][1] = 0 #max(dtlongest, (ts - dtfrom).seconds)

                #TODO: check if this line is needed?
                self.avg_aqi[year][city].resize(ts - timedelta(days=5))

    def next_aqi_snapshot(self, ts):
        return ts + timedelta(minutes=5)

    def process_payloads(self, year, payloads):
        per_city = self.data[year]
        for payload in payloads:
            city = self._resolve_location(payload)

            if not city:  # outside
                continue

            if city not in per_city:
                per_city[city] = {}
                per_city[city][1] = MeanSlidingWindow()
                per_city[city][2] = MeanSlidingWindow()

            ts = payload.timestamp
            dt = datetime.fromtimestamp(ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)

            if (year == self.id_curr) and dt > self.next_snapshot:
                self.snapshot_aqi(self.id_curr, dt)
                self.snapshot_aqi(self.id_lastyear, dt - timedelta(days=365))
                self.next_snapshot = self.next_aqi_snapshot(self.next_snapshot)

            per_city[city][1].add(dt, payload.p1)
            per_city[city][2].add(dt, payload.p2)

        self.data[year] = per_city

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
            dtmax_curr = self.max_timestamp(
                batch.lastyear) + timedelta(days=365)

        dtmax_lastyear = dtmax_curr - timedelta(days=365)

        if not self.next_snapshot:
            startminute = dtmax_curr.minute - (dtmax_curr.minute % 5)
            self.next_snapshot = dtmax_curr.replace(
                minute=startminute, second=0, microsecond=0) + timedelta(minutes=5)

        self.process_payloads(self.id_curr, batch.current)
        self.process_payloads(self.id_lastyear, batch.lastyear)

        activity_timeout = timedelta(minutes=10)

        cnt = 1
        res = list()
        not_active_cnt = 0
        if self.id_curr in self.avg_aqi:
            for (city, window_aqi_curr) in self.avg_aqi[self.id_curr].items():
                if city in self.avg_aqi[self.id_lastyear]:
                    window_aqi_last = self.avg_aqi[self.id_lastyear][city]

                    window_aqi_curr.resize(dtmax_curr - timedelta(days=5))
                    window_aqi_last.resize(
                        dtmax_curr - timedelta(days=365 + 5))

                    if (window_aqi_curr.active(dtmax_curr - activity_timeout)) and (window_aqi_last.active(dtmax_curr - timedelta(days=365) - activity_timeout)):
                        last_year_avg_aqi = window_aqi_last.getMean()
                        curr_year_avg_aqi = window_aqi_curr.getMean()

                        curr_year_window_p1 = self.data[self.id_curr][city][1]
                        curr_year_window_p2 = self.data[self.id_curr][city][2]
                        last_year_window_p1 = self.data[self.id_lastyear][city][1]
                        last_year_window_p2 = self.data[self.id_lastyear][city][2]

                        curr_year_window_p1.resize(
                            dtmax_curr - timedelta(hours=24))
                        curr_year_window_p2.resize(
                            dtmax_curr - timedelta(hours=24))
                        last_year_window_p1.resize(
                            dtmax_curr - timedelta(days=365, hours=24))
                        last_year_window_p2.resize(
                            dtmax_curr - timedelta(days=365, hours=24))

                        if curr_year_window_p1.active(dtmax_curr - activity_timeout) and last_year_window_p1.has_elements():
                            mtemp = curr_year_window_p1.getMean()
                            if not mtemp:
                                print("city: %s dtmax_curr: %s valvs: %s window_aqi_curr: %s window_aqi_last: %s" %
                                      (city, dtmax_curr, curr_year_window_p1.has_elements(), window_aqi_curr.size(), window_aqi_last.size()))
                                exit(0)
                            curr_year_aqi = utils.EPATableCalc(mtemp)
                            last_year_aqi = utils.EPATableCalc(
                                last_year_window_p1.getMean())

                            res.append((city,
                                        round(last_year_avg_aqi -
                                              curr_year_avg_aqi, 3),
                                        round(curr_year_aqi, 3),
                                        round(utils.EPATableCalc(curr_year_window_p1.getMean(), pollutant="P1"), 3),
                                        round(utils.EPATableCalc(curr_year_window_p2.getMean(), pollutant="P2"), 3)))
                            cnt = cnt + 1
                        else:
                            not_active_cnt = not_active_cnt + 1
                    else:
                        not_active_cnt = not_active_cnt + 1

        sort_res = sorted(res, key=lambda r: r[1], reverse=True)

        topk = 50

        topklist = list()
        for i in range(1, topk + 1):
            if len(sort_res) >= i:
                res = sort_res[i - 1]
                topklist.append(ch.TopKCities(position=i,
                                              city=res[0],
                                              averageAQIImprovement=int(res[1] * 1000.0),
                                              currentAQIP1=int(res[3] * 1000.0),
                                              currentAQIP2=int(res[4] * 1000.0)))

        streak_res = list()
        for (city, tup) in self.streak.items():
            if city in self.avg_aqi[self.id_curr]:
                window_aqi_last = self.avg_aqi[self.id_curr][city]
                if (window_aqi_curr.active(dtmax_curr - activity_timeout)) and (window_aqi_last.active(dtmax_curr - timedelta(days=365) - activity_timeout)):
                    if tup[0] == "good":
                        streak_res.append([city, (dtmax_curr - tup[2]).total_seconds()])

        sorted_streak = sorted(streak_res, key=lambda s: s[1], reverse=True)
        topk_streaks = list()
        for i in range(1, topk + 1):
            if len(sorted_streak) >= i:
                st = sorted_streak[i-1]
                streak = ch.TopKStreaks(position=i,
                                        city=st[0],
                                        streakseconds=int(st[1]))
                topk_streaks.append(streak)

        return not_active_cnt, dtmax_curr, topklist, topk_streaks


class QueryOneAlternative:
    def __init__(self, challengerstub):
        self.event_processor = QueryOneEventProcessor()
        self.challengerstub = challengerstub

    def measureLatency(self, benchmark):
        ping = self.challengerstub.initializeLatencyMeasuring(benchmark)
        for i in range(10):
            ping = self.challengerstub.measure(ping)

        self.challengerstub.endMeasurement(ping)

    def run(self):
        locationfile = "locationcache5.pickle"
        locationcache = "locationlookupcache1.pickle"

        benchmarkconfiguration = ch.BenchmarkConfiguration(token="cpjcwuaeufgqqxhohhvqlyndjazvzymx",
                                                           batch_size=20000,
                                                           benchmark_name="test benchmark",
                                                           benchmark_type="test",
                                                           queries=[ch.BenchmarkConfiguration.Query.Q1])
        bench = self.challengerstub.createNewBenchmark(benchmarkconfiguration)

        if os.path.exists(locationfile):
            with open(locationfile, "rb") as f:
                self.event_processor.zipcode_polygons = pickle.load(f)
        else:
            loc = self.challengerstub.getLocations(bench)
            print('got location data: %s' % len(loc.locations))
            self.event_processor.setup_locations(loc)
            with open(locationfile, "wb") as f:
                pickle.dump(self.event_processor.zipcode_polygons, f)

        if os.path.exists(locationcache):
            with open(locationcache, "rb") as f:
                self.event_processor.location_to_city = pickle.load(f)


        # First, we measure the latency.
        # This is only for the testing dashboard to substract the communication latency
        self.measureLatency(bench)

        # start the benchmark
        print("start processing batch")
        start_time = datetime.now()
        self.challengerstub.startBenchmark(bench)
        batch = self.challengerstub.nextBatch(bench)

        num_current = 0
        num_historic = 0
        cnt = 0
        emptycount = 0

        lastdisplay = 0

        while batch:
            if batch.last:
                break

            num_current += len(batch.current)
            num_historic += len(batch.lastyear)

            (not_active, dtmax_curr, payload, streaks) = self.event_processor.process(batch)

            if len(payload) == 0:
                emptycount = emptycount + 1

            result = ch.ResultQ1(benchmark_id=bench.id, payload_seq_id=batch.seq_id, topkimproved=payload)
            self.challengerstub.resultQ1(result)

            cnt = cnt + 1
            duration_so_far = (datetime.now() - start_time).total_seconds()
            if (duration_so_far - lastdisplay) >= 2:  # limit output every 2 seconds
                with open(locationcache, "wb") as f:
                    pickle.dump(self.event_processor.location_to_city, f)

                os.system('clear')
                print("Top %s most improved zipcodes, last 24h - date: %s " %
                      (len(payload), dtmax_curr))
                print("processed %s in %s seconds - empty: %s not_active: %s num_current: %s, num_historic: %s, total_events: %s" % (
                    cnt, duration_so_far, emptycount, not_active, num_current, num_historic, (num_current + num_historic)))
                for topk in payload:
                    print("pos: %2s, city: %25.25s, avg imp.: %8.3f, curr-AQI-P1: %8.3f, curr-AQI-P2: %8.3f " % (
                        topk.position, topk.city, topk.averageAQIImprovement /
                        1000.0, topk.currentAQIP1 / 1000.0,
                        topk.currentAQIP2 / 1000.00))

                print("Longest streaks " % (len(payload), dtmax_curr))
                for topk_streaks in streaks:
                    print("pos: %2s, city: %25.25s, streakseconds: %s" % (
                        topk_streaks.position, topk_streaks.city, topk_streaks.streakseconds))


                lastdisplay = duration_so_far

            batch = self.challengerstub.nextBatch(bench)

    def process_current(self, batch):
        return


def main():
    op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
          ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
    with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
        #with grpc.insecure_channel('127.0.0.1:8081', options=op) as channel:
        stub = api.ChallengerStub(channel)
        q1 = QueryOneAlternative(stub)
        q1.run()


if __name__ == "__main__":
    logging.basicConfig()
    main()
