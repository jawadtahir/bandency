
import logging
import os
from datetime import datetime, timedelta

import grpc
from google.protobuf import empty_pb2
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
        self.cachemiss = 0
        self.cachehit = 0

    def measureLatency(self, benchmark):
        ping = self.challengerstub.initializeLatencyMeasuring(benchmark)
        for i in range(10):
            ping = self.challengerstub.measure(ping)

        self.challengerstub.endMeasurement(ping)

    def setup_locations(self, location_info_list):
        print("Processing locations...")
        count = 0
        for location_info in location_info_list.locations:
            if count % 1000 == 0:
                print("Locations processed: {}".format(count))

            polygons = location_info.polygons
            for polygon in polygons:
                obj_points = []
                for point in polygon.points:
                    obj_points.append(Point(point.longitude, point.latitude))

                polygon = Polygon(obj_points)
                polygon.zipcode = location_info.zipcode
                self.zipcode_polygons.append(polygon)

            count += 1

    def _resolve_location(self, event):
        location = (event.longitude, event.latitude)

        # lookup in cache
        if location in self.location_to_city:
            self.cachehit = self.cachehit + 1
            return self.location_to_city[location]

        # otherwise we have to search
        point = Point(event.longitude, event.latitude)
        for polygon in self.zipcode_polygons:
            if polygon.contains(point):
                self.cachemiss = self.cachemiss + 1
                self.location_to_city[location] = polygon.zipcode
                return polygon.zipcode

        # haven't found, hence we store it as unknown
        self.location_to_city[location] = None
        return None

    # todo: merge with process payloads
    def maxDate(self, payloads):
        dtmax = None
        for payload in payloads:
            ts = payload.timestamp
            dt = datetime.fromtimestamp(ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)
            if dtmax == None:
                dtmax = dt
            else:
                dtmax = max(dtmax, dt)

        return dtmax

    def process_payloads(self, year, payloads):
        if not year in self.data:
            self.data[year] = {}

        per_city = self.data[year]
        for payload in payloads:
            city = self._resolve_location(payload)
            if not city: #outside
                continue

            if city not in per_city:
                per_city[city] = AverageSlidingWindow()

            ts = payload.timestamp
            dt = datetime.fromtimestamp(ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)
            per_city[city].add(dt, payload.p2)

        self.data[year] = per_city

    def truncate_old_values(self, dt):
        if not dt.year in self.data:
            return

        mindate = dt - timedelta(hours=24)
        for (k, v) in self.data[dt.year].items():
            v.resize(mindate)

    def calculate_epa_scores(self, year):
        if not year in self.data:
            return
        result = {}
        for (k, v) in self.data[year].items():
            result[k] = utils.EPATableCalc(v.getMean())
        return result


    def process(self, batch):
        dtmax_current_batch = self.maxDate(batch.current)
        dtmax_lastyear_batch = self.maxDate(batch.lastyear)
        if dtmax_current_batch is None:
            dtmax_current_batch = dtmax_lastyear_batch.replace(year=dtmax_lastyear_batch.year+1)
        if dtmax_lastyear_batch is None:
            dtmax_lastyear_batch = dtmax_current_batch.replace(year=dtmax_current_batch.year-1)


        dtmax_curr = max(dtmax_current_batch, dtmax_lastyear_batch.replace(year=dtmax_current_batch.year))
        dtmax_lastyear = dtmax_curr.replace(year=dtmax_curr.year -1)

        self.process_payloads(dtmax_curr.year, batch.current)
        self.process_payloads(dtmax_lastyear.year, batch.lastyear)

        self.truncate_old_values(dtmax_curr)
        self.truncate_old_values(dtmax_lastyear)

        scores_curr = self.calculate_epa_scores(dtmax_curr.year)
        scores_lastyear = self.calculate_epa_scores(dtmax_lastyear.year)

        res = list()
        for (k, v) in scores_curr.items():
            if k in scores_lastyear:
                last_year_aqi = scores_lastyear[k]
                curr_year_aqi = v
                res.append((k, last_year_aqi - curr_year_aqi, last_year_aqi, curr_year_aqi))

        sort_res = sorted(res, key=lambda r: r[1], reverse=True)

        topk = 50

        topklist = list()
        for i in range(1, topk + 1):
            if len(sort_res) >= i:
                res = sort_res[i-1]
                topklist.append(ch.TopKCities(position=i, city=res[0], averageAQIImprovement=res[1], currentAQI=res[2], previousAQI=res[3]))

        return (dtmax_curr, topklist)

    def run(self):
        event_proc = EventProcessor()

        loc = self.challengerstub.getLocations(empty_pb2.Empty())
        print('got location data: %s' % len(loc.locations))
        self.setup_locations(loc)

        benchmarkconfiguration = ch.BenchmarkConfiguration(token="abc",
                                                           batch_size=5000,
                                                           benchmark_name="test benchmark",
                                                           queries=[ch.BenchmarkConfiguration.Query.Q1])
        bench = self.challengerstub.createNewBenchmark(benchmarkconfiguration)

        # First, we measure the latency.
        # This is only for the testing dashboard to substract the communication latency
        self.measureLatency(bench)

        # start the benchmark
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
            if (duration_so_far - lastdisplay) > 10: #limit output every 10 seconds
                os.system('clear')
                print("Top %s most improved zipcodes, last 24h - date: %s cachemiss: %s cachehit: %s" % (len(topk), dtmax_curr, self.cachemiss, self.cachehit))
                print("processed %s in %s seconds - num_current: %s, num_historic: %s, total_events: %s" % (cnt, duration_so_far, num_current, num_historic, (num_current + num_historic)))
                for topk in payload:
                    print("pos: %s, city: %s, avg improvement: %s, previous: %s, current: %s " % (topk.position, topk.city, topk.averageAQIImprovement, topk.currentAQI, topk.previousAQI))

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
