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
        self.update_interval_mins = 5
        self.emitable = False

        self.location_year_pm_window_map = {}
        self.location_year_aqi_map = {}
        self.last_processed_day = None
        self.location_aqi_diff_map = {}
        self.location_improvement_map = {}

        self.location_zip_cache = {}
        self.zipcode_polygons = []

        self.start_time = datetime.min
        self.batch_count = 0

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
#                 polygon.zipcode = location_info.zipcode
                self.zipcode_polygons.append([polygon, location_info.zipcode])

            count += 1

    def _resolve_location(self, event):

        if self.location_zip_cache.get(event["location"]):
            event["zipcode"] = self.location_zip_cache[event["location"]]
            return

        point = Point(float(event["longitude"]), float(event["latitude"]))
        for polygon in self.zipcode_polygons:
            if polygon[0].contains(point):
                event["zipcode"] = polygon[1]
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

        for city_year, window_arr in self.location_year_pm_window_map.items():
            # Slide window in case of a offline location

            last_event_ts = list(window_arr[0].timed_window.keys())[-1]
            last_updated = self.last_updated

            if last_event_ts.year == self.max_date_last.year:
                last_updated = last_updated + timedelta(days=-365)

            if (last_event_ts + timedelta(minutes=self.update_interval_mins)) < last_updated:
                window_arr[0].slide(last_updated, np.nan)
                window_arr[1].slide(last_updated, np.nan)

            window_arr[0].resize()
            window_arr[1].resize()

            # AQI calculation
            p2_concentration = np.nanmean(window_arr[0].get_array())
            p1_concentration = np.nanmean(window_arr[1].get_array())

            p2_aqi = utils.EPATableCalc(p2_concentration)
            p1_aqi = utils.EPATableCalc(p1_concentration, "P1")

            aqi = max(p2_aqi, p1_aqi)

            aqi_map_index = city_year

            if self.location_year_aqi_map.get(aqi_map_index):
                self.location_year_aqi_map[aqi_map_index].slide(
                    last_event_ts, aqi)
            else:
                self.location_year_aqi_map[aqi_map_index] = TemporalSlidingWindow(
                    last_event_ts, aqi, 120)

    def emit(self):

        if len(self.location_improvement_map) == 0:
            return

        loc_improv = OrderedDict(sorted(self.location_improvement_map.items(
        ), key=lambda item: item[1][0], reverse=True))

        loc_improv_iter = iter(loc_improv.items())

        os.system('clear')
        topk = 50

        topklist = list()

        print("Top %s most improved zipcodes, last 24h - date: %s :" %
              (topk, self.max_date_current))

        for i in range(1, topk + 1):

            res = next(loc_improv_iter)
            res[1][0] = round(res[1][0], 3)

            current_index = "{}_{}".format(res[0], self.max_date_current.year)
            last_index = "{}_{}".format(res[0], self.max_date_last.year)

            current_p2 = round(np.nanmean(
                self.location_year_pm_window_map[current_index][0].get_array()), 3)
            current_p1 = round(np.nanmean(
                self.location_year_pm_window_map[current_index][1].get_array()), 3)

            last_p2 = np.nanmean(
                self.location_year_pm_window_map[last_index][0].get_array())
            last_p1 = np.nanmean(
                self.location_year_pm_window_map[last_index][1].get_array())

            current_aqi = round(max(utils.EPATableCalc(current_p2),
                                    utils.EPATableCalc(current_p1, "P1")), 3)
            last_aqi = round(max(utils.EPATableCalc(last_p2),
                                 utils.EPATableCalc(last_p1, "P1")), 3)

            print("pos: %s, city: %s, avg improvement: %s, previous: %s, current: %s " % (
                i, res[0], res[1][0], last_aqi, current_aqi))
            topklist.append(ch.TopKCities(
                position=1, city=res[0], averageAQIImprovement=int(res[1][0] * 1000.0), currentAQI=int(current_aqi * 1000.0), currentP1=int(current_p1 * 1000.0), currentP2=int(current_p2 * 1000.0)))

        self.location_improvement_map = {}

        return topklist

    def update(self, event):
        self._calculate_AQI()

        for (location_year, aqi_window) in self.location_year_aqi_map.items():

            location, year = location_year.split("_")

            if year == str(self.max_date_current.year):

                previous_aqi_window = self.location_year_aqi_map.get(
                    "{}_{}".format(location, int(year) - 1))

                if previous_aqi_window:

                    current_aqi = np.nanmean(
                        aqi_window.get_array())

                    previous_aqi = np.nanmean(
                        previous_aqi_window.get_array())

                    aqi_improvment = previous_aqi - current_aqi

                    if self.location_improvement_map.get(location):
                        acc_aqi_improvement = self.location_improvement_map[location][1] + \
                            aqi_improvment
                        no_of_events = self.location_improvement_map[location][2] + 1
                        avg_aqi_improvement = float(
                            acc_aqi_improvement) / float(no_of_events)

                        self.location_improvement_map[location] = [
                            avg_aqi_improvement, acc_aqi_improvement, no_of_events]
                    else:
                        #[Averaged improvement, accumalated improvement per batch, # of events]
                        self.location_improvement_map[location] = [
                            aqi_improvment, aqi_improvment, 1]

    def fill_p_windows(self, event):

        def fill(self, event):
            if event is None:
                return

            index = "{}_{}".format(event["zipcode"], event["timestamp"].year)
            if self.location_year_pm_window_map.get(index):
                pm_window = self.location_year_pm_window_map[index]

                p2_window = pm_window[0]
                p1_window = pm_window[1]

                p2_window.append(event["timestamp"],
                                 event["p2"] if event["p2"] else np.nan)
                p1_window.append(event["timestamp"],
                                 event["p1"] if event["p1"] else np.nan)

            else:
                p2_window = TemporalSlidingWindow(
                    event["timestamp"], event["p2"] if event["p2"] else np.nan)
                p1_window = TemporalSlidingWindow(
                    event["timestamp"], event["p1"] if event["p1"] else np.nan)

                pm_window = [p2_window, p1_window]
                self.location_year_pm_window_map[index] = pm_window

        fill(self, event[0])
        fill(self, event[1])

    def _determine(self, event):
        if event is None:
            return False

        event_ts = event["timestamp"]

        if event_ts.year == self.max_date_last.year:
            event_ts = event_ts + timedelta(days=365)

        if self.last_updated is datetime.min:
            self.last_updated = event_ts
            return False

        if event_ts > self.last_updated + timedelta(minutes=self.update_interval_mins):
            self.last_updated = event_ts
            return True
        else:
            return False

    def is_update_time(self, event):
        return self._determine(event[1]) if event[1] else self._determine(event[0])

    def process(self, batch):
        if self.batch_count == 0:
            self.start_time = datetime.now()

        self.batch_count += 1
        events = self.pre_proc(batch)

        for event in events:

            if event:
                event = self.enrich(event)
                event = self.filter(event)
                self.fill_p_windows(event)
                if self.is_update_time(event):

                    self.update(event)

        topk = self.emit()
        time_now = datetime.now()
        avg_time_per_batch = (time_now - self.start_time) / self.batch_count
        print("Average time per batch: {}".format(avg_time_per_batch))

        return topk


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
        return list(self.timed_window.values())
