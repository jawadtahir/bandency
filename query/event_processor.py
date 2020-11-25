'''
Created on Oct 22, 2020

@author: foobar
'''

from builtins import sorted
from collections import OrderedDict
from datetime import datetime, timedelta
import os

from google.protobuf.json_format import MessageToDict
from shapely.geometry import Point
from shapely.geometry.polygon import Polygon

import challenger_pb2 as ch
import numpy as np
import utils


UNKNOWN = "UNKNOWN"

LATLONG_FORMATTER = "{:.6f}"
DATETIME_FORMATTER = "%Y-%m-%dT%H:%M:%SZ"


class EventProcessor:

    def __init__(self):

        self.location_pm_window_map = {}
        self.location_year_aqi_map = {}
        self.last_processed_day = None
        self.location_aqi_diff_map = {}
        self.location_improvment_map = {}

        self.location_zip_cache = {}
        self.zipcode_polygons = []

    def configure(self, location_info_list):

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

        if self.location_zip_cache.get(event["location"]):
            event["zipcode"] = self.location_zip_cache[event["location"]]
            return

        point = Point(float(event["longitude"]), float(event["latitude"]))
        for polygon in self.zipcode_polygons:
            if polygon.contains(point):
                event["zipcode"] = polygon.zipcode
                self.location_zip_cache[event["location"]] = event["zipcode"]
                return

        self.location_zip_cache[event["location"]] = UNKNOWN
        event["zipcode"] = UNKNOWN

    def pre_proc(self, batch):

        events = list(batch.lastyear) + list(batch.current)
        ret_val = []
        for event in events:
            if event.longitude is None or event.latitude is None:
                continue
            ret_val.append(MessageToDict(event))
        print("Batch {}: {} events in batch".format(batch.seq_id, len(ret_val)))
        return ret_val

    def filter(self, event):

        if event is None:
            return None

        if event["zipcode"] == UNKNOWN:
            return None
        if not event.get("timestamp"):
            return None
        if not event.get("p2"):
            return None
        if not event.get("longitude") or not event.get("latitude"):
            return None
        else:
            return event

    def enrich(self, event):

        if event.get("longitude") is None or event.get("latitude") is None:
            return None

        event["longitude"] = LATLONG_FORMATTER.format(event["longitude"])
        event["latitude"] = LATLONG_FORMATTER.format(event["latitude"])
        event["location"] = "{}, {}".format(
            event["latitude"], event["longitude"])

        self._resolve_location(event)

        event["timestamp"] = datetime.strptime(
            event["timestamp"], DATETIME_FORMATTER)

        return event

    def _calculate_AQI(self, event):

        def calculate_winavg_pm(event):

            if self.location_pm_window_map.get(event["zipcode"]):
                pm_window = self.location_pm_window_map[event["zipcode"]]
                pm_window.add(event["timestamp"], event["p2"])

            else:
                pm_window = TemporalSlidingWindow(
                    event["timestamp"], event["p2"])
                self.location_pm_window_map[event["zipcode"]] = pm_window

            return np.nanmean(list(pm_window.get_array(
            )))

        con = calculate_winavg_pm(event)
        event["AQI"] = utils.EPATableCalc(con)

        aqi_map_index = "{}_{}".format(
            event["zipcode"], event["timestamp"].year)

        if self.location_year_aqi_map.get(aqi_map_index):
            self.location_year_aqi_map[aqi_map_index].add(
                event["timestamp"], event["AQI"])
        else:
            self.location_year_aqi_map[aqi_map_index] = TemporalSlidingWindow(
                event["timestamp"], event["AQI"])

    def execute(self, event):
        if event is None:
            return

        self._calculate_AQI(event)

    def emit(self):

        max_date_in_batch = None

        for location_year in self.location_year_aqi_map.keys():
            location, year = location_year.split("_")
            if year == "2020":
                previous_aqi = self.location_year_aqi_map.get(
                    "{}_{}".format(location, int(year) - 1))

                if not previous_aqi:
                    continue

                current_aqi = self.location_year_aqi_map[location_year]

                current_aqi = np.nanmean(list(current_aqi.get_array()))
                previous_aqi = np.nanmean(list(previous_aqi.get_array()))

                if max_date_in_batch is None:
                    max_date_in_batch = self.location_year_aqi_map[location_year].maxdatetime
                elif self.location_year_aqi_map[location_year].maxdatetime is not None:
                    max_date_in_batch = max(
                        self.location_year_aqi_map[location_year].maxdatetime, max_date_in_batch)

                aqi_improvment = previous_aqi - current_aqi

                if self.location_improvment_map.get(location):
                    acc_aqi_improvement = self.location_improvment_map[location][0] + \
                        aqi_improvment
                    no_of_events = self.location_improvment_map[location][4] + 1
                    avg_aqi_improvement = float(
                        acc_aqi_improvement) / float(no_of_events)

                    self.location_improvment_map[location] = [
                        acc_aqi_improvement, previous_aqi, current_aqi, avg_aqi_improvement, no_of_events]
                else:
                    #[AQI improvement, Previous AQI, Current AQI, Averaged improvement, # of events]
                    self.location_improvment_map[location] = [
                        aqi_improvment, previous_aqi, current_aqi, aqi_improvment, 1]

        loc_improv = OrderedDict(sorted(self.location_improvment_map.items(
        ), key=lambda item: item[1][3], reverse=True))
        loc_improv_iter = iter(loc_improv.items())

        os.system('clear')
        topk = 50

        print("Top %s most improved zipcodes, last 24h - date: %s :" %
              (topk, max_date_in_batch))
        for i in range(1, topk + 1):
            res = next(loc_improv_iter)
            print("pos: %s, city: %s, avg improvement: %s, previous: %s, current: %s " % (
                i, res[0], res[1][3], res[1][1], res[1][2]))

        return ch.ResultQ1Payload(resultData=0)

    def process(self, batch):

        events = self.pre_proc(batch)
        count = 0
        for event in events:
            # if count % 1000 == 0:
            #    print("Events processed: {}".format(count))
            if event:
                event = self.enrich(event)
                event = self.filter(event)
                self.execute(event)
                count += 1

        return self.emit()


class TemporalSlidingWindow:

    def __init__(self, timestamp: datetime, value, window_hours=24):
        self.timed_window = OrderedDict({timestamp: value})
        self.window = -window_hours
        self.maxdatetime = None

    def add(self, timestamp: datetime, value):
        # Expand
        self.timed_window[timestamp] = value
        # Contract
        self.timed_window = {item[0]: item[1] for item in self.timed_window.items(
        ) if item[0] > (timestamp + timedelta(hours=self.window))}

        if self.maxdatetime is None:
            self.maxdatetime = timestamp
        else:
            self.maxdatetime = max(self.maxdatetime, timestamp)

    def get_array(self):
        return self.timed_window.values()
