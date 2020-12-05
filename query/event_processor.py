'''
Created on Oct 22, 2020

@author: foobar
'''

from builtins import sorted
from collections import OrderedDict
from datetime import datetime, timedelta, time
from itertools import zip_longest
import os

from google.protobuf.json_format import MessageToDict
from shapely.geometry import Point
from shapely.geometry.polygon import Polygon

import challenger_pb2 as ch
import numpy as np
import utils


UNKNOWN = "UNKNOWN"
CURRENT = "C"
LAST = "L"

LATLONG_FORMATTER = "{:.6f}"
DATETIME_FORMATTER = "%Y-%m-%dT%H:%M:%SZ"


def getTS(payload):
    ts = payload.timestamp
    dt = datetime.fromtimestamp(
        ts.seconds) + timedelta(microseconds=ts.nanos / 1000.0)

    return dt


class EventProcessor:

    def __init__(self):

        self.max_date_current = datetime.min
        self.max_date_last = datetime.min
        self.last_updated = datetime.min
        self.update_interval_mins = 2
        self.emitable = False

        self.location_year_pm_window_map = {}
        self.location_year_aqi_map = {}
        self.last_processed_day = None
        self.location_aqi_diff_map = {}
        self.location_improvement_map = {}

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
        def maxDate(payloads):
            dtmax = None
            for payload in payloads:
                dt = getTS(payload)
                if dtmax == None:
                    dtmax = dt
                else:
                    dtmax = max(dtmax, dt)

            return dtmax

        self.max_date_current = max(
            self.max_date_current, maxDate(batch.current))
        self.max_date_last = max(self.max_date_last, maxDate(batch.lastyear))

        last_year = []
        curr_year = []

        for event in list(batch.lastyear):
            if event.longitude is None or event.latitude is None:
                continue
            dt = getTS(event)
            dict_event = MessageToDict(event)
            dict_event["timestamp"] = dt
            last_year.append(dict_event)

        for event in list(batch.current):
            if event.longitude is None or event.latitude is None:
                continue
            dt = getTS(event)
            dict_event = MessageToDict(event)
            dict_event["timestamp"] = dt
            curr_year.append(dict_event)

        return zip_longest(last_year, curr_year)

    def filter(self, event):

        def filteration(event):
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

        ret_val = (filteration(event[0]), filteration(event[1]))
        return ret_val

    def enrich(self, event):

        def enrichment(event):
            if event is None:
                return None

            if event.get("longitude") is None or event.get("latitude") is None:
                return None

            event["longitude"] = LATLONG_FORMATTER.format(event["longitude"])
            event["latitude"] = LATLONG_FORMATTER.format(event["latitude"])
            event["location"] = "{}, {}".format(
                event["latitude"], event["longitude"])

            self._resolve_location(event)

            return event

        ret_val = (enrichment(event[0]), enrichment(event[1]))

        return ret_val

    def _calculate_AQI(self):

        for city_year, window in self.location_year_pm_window_map.items():
            # Slide window in case of a offline location

            last_event_ts = list(window.timed_window.keys())[-1]
            last_updated = self.last_updated

            if last_event_ts.year == self.max_date_last.year:
                last_updated = last_updated + timedelta(days=-365)

            if (last_event_ts + timedelta(minutes=self.update_interval_mins)) < last_updated:
                window.slide(last_updated, np.nan)

            window.resize()

            # AQI calculation
            polutant_concentration = np.nanmean(list(window.get_array()))
            aqi = utils.EPATableCalc(polutant_concentration)

            aqi_map_index = city_year

            if self.location_year_aqi_map.get(aqi_map_index):
                self.location_year_aqi_map[aqi_map_index].slide(
                    last_event_ts, aqi)
            else:
                self.location_year_aqi_map[aqi_map_index] = TemporalSlidingWindow(
                    last_event_ts, aqi, 120)

    def emit(self):

        if not self.emitable:
            return

        loc_improv = OrderedDict(sorted(self.location_improvement_map.items(
        ), key=lambda item: item[1][3], reverse=True))
        loc_improv_iter = iter(loc_improv.items())

        os.system('clear')
        topk = 50

        topklist = list()
        print("Top %s most improved zipcodes, last 24h - date: %s :" %
              (topk, self.max_date_current))
        for i in range(1, topk + 1):
            res = next(loc_improv_iter)
            print("pos: %s, city: %s, avg improvement: %s, previous: %s, current: %s " % (
                i, res[0], res[1][3], res[1][1], res[1][2]))
            topklist.append(ch.TopKCities(
                position=1, city=res[0], averageAQIImprovement=res[1][3], currentAQI=res[1][1], previousAQI=res[1][2]))

        self.location_improvement_map = {}
        self.emitable = False

        return topklist

    def update(self, event):
        self._calculate_AQI()

        for (location_year, window) in self.location_year_aqi_map.items():

            location, year = location_year.split("_")

            if year == str(self.max_date_current.year):

                previous_aqi = self.location_year_aqi_map.get(
                    "{}_{}".format(location, int(year) - 1))

                if previous_aqi:

                    current_aqi = window
                    current_aqi = np.nanmean(list(current_aqi.get_array()))
                    previous_aqi = np.nanmean(
                        list(previous_aqi.get_array()))
                    aqi_improvment = previous_aqi - current_aqi

                    if self.location_improvement_map.get(location):
                        acc_aqi_improvement = self.location_improvement_map[location][0] + \
                            aqi_improvment
                        no_of_events = self.location_improvement_map[location][4] + 1
                        avg_aqi_improvement = float(
                            acc_aqi_improvement) / float(no_of_events)

                        self.location_improvement_map[location] = [
                            acc_aqi_improvement, previous_aqi, current_aqi, avg_aqi_improvement, no_of_events]
                    else:
                        #[AQI improvement, Previous AQI, Current AQI, Averaged improvement, # of events]
                        self.location_improvement_map[location] = [
                            aqi_improvment, previous_aqi, current_aqi, aqi_improvment, 1]

    def fill_p_windows(self, event):

        def fill(self, event):
            if event is None:
                return

            index = "{}_{}".format(event["zipcode"], event["timestamp"].year)
            if self.location_year_pm_window_map.get(index):
                pm_window = self.location_year_pm_window_map[index]
                pm_window.append(event["timestamp"], event["p2"])

            else:
                pm_window = TemporalSlidingWindow(
                    event["timestamp"], event["p2"])
                self.location_year_pm_window_map[index] = pm_window

        fill(self, event[0])
        fill(self, event[1])

    def _determine(self, event):
        if event is None:
            return False

        event_ts = event["timestamp"]

        if event_ts.year == self.max_date_last:
            event_ts = event_ts + timedelta(days=-365)

        if self.last_updated is datetime.min:
            self.last_updated = event_ts
            return False

        if event_ts > self.last_updated + timedelta(minutes=self.update_interval_mins):
            self.last_updated = event_ts
            self.emitable = True
            return True
        else:
            return False

    def is_update_time(self, event):
        return self._determine(event[1]) if event[1] else self._determine(event[0])

    def process(self, batch):

        events = self.pre_proc(batch)
        count = 0
        for event in events:
            if count % 1000 == 0:
                print("Event pairs processed: {}".format(count))
            if event:
                event = self.enrich(event)
                event = self.filter(event)
                self.fill_p_windows(event)
#                 self.execute(event)
                count += 1
                if self.is_update_time(event):

                    self.update(event)

        return self.emit()


class TemporalSlidingWindow:

    def __init__(self, timestamp: datetime, value, window_hours=24):
        self.timed_window = OrderedDict({timestamp: value})
        self.window = -window_hours
        self.maxdatetime = timestamp

    def slide(self, timestamp: datetime, value):
        self.append(timestamp, value)
        self.resize()

    def append(self, timestamp: datetime, value):
        self.timed_window[timestamp] = value
        self.maxdatetime = max(self.maxdatetime, timestamp)

    def resize(self):
        self.timed_window = {item[0]: item[1] for item in self.timed_window.items(
        ) if item[0] > (self.maxdatetime + timedelta(hours=self.window))}

    def get_array(self):
        return self.timed_window.values()
