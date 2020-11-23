'''
Created on Oct 22, 2020

@author: foobar
'''

import csv
import utils
from data import load_genrator
import numpy as np
from datetime import datetime, timedelta
from collections import OrderedDict
from builtins import sorted
from shapely.geometry import shape, Point
import json
from shapely.geometry.polygon import Polygon

SENSORS = ""
MONTHSTAMPS = ""
UNKNOWN = "UNKNOWN"
ZIPCODE_GEOJSON_PATH = "/home/foobar/eclipse-workspace/gc/plz-5stellig.geojson"

LATLONG_FORMATTER = ":.6f"


class EventProcessor:

    def __init__(self):
        self.location_pm_window_map = {}
        self.location_year_aqi_map = {}
        self.last_processed_day = None
        self.location_aqi_diff_map = {}
        self.location_improvment_map = {}
        
        self.location_zip_cache = {}
        self.zipcode_polygons = []
#         with open(ZIPCODE_GEOJSON_PATH) as f:
#             self.zipcode_polygons = json.load(f)
            
            
    def configure(self, location_info_list):
        for location_info in location_info_list:
            polygons = location_info.polygons
            for polygon in polygons:
                for points in polygon:
                    obj_points = []
                    for point in points:
                        obj_points.append(Point(point.longitude, point.latitude))
                
                polygon = Polygon(obj_points)
                polygon.zipcode = location_info.zipcode
                self.zipcode_polygons.append(polygon)
                
            
        
        
    def _resolve_location(self, event):
        if self.location_zip_cache.get(event.location):
            event.zipcode = self.location_zip_cache[event.location] 
            return
        
        point = Point(float(event.longitude), float(event.latitude))
        for polygon in self.zipcode_polygons:
            if polygon.contains(point):
                event.zipcode = polygon.zipcode
                self.location_zip_cache[event.location] = event.zipcode
                return
        
        self.location_zip_cache[event.location] = UNKNOWN
        event.zipcode = UNKNOWN 
        
        
    def pre_proc(self, batch):
        events = [batch.lastyear, batch.current]
        return events
    
        
    def filter(self, event):
        if event.zipcode == UNKNOWN:
            event = None
        
    def enrich(self, event):
        event.longitude = LATLONG_FORMATTER.format(event.longitude)
        event.latitude = LATLONG_FORMATTER.format(event.latitude)
        event.location = "{}, {}".format(event.longitude, event.latitude)
            
        self._resolve_location(event)
        
        event.timestamp = datetime.fromtimestamp(event.timestamp)
        
        if not event.p2:
            event.p2 = np.nan
    
    
    def _calculate_AQI(self, event):

        def calculate_winavg_pm (event):
            
            if self.location_pm_window_map.get(event.zipcode):
                pm_window = self.location_pm_window_map[event.zipcode]
                pm_window.add(event.timestamp, event.p2)
                
            else:
                pm_window = TemporalSlidingWindow(event.timestamp, event.p2)
                self.location_pm_window_map[event.zipcode] = pm_window 
                
            return np.nanmean(pm_window.get_array(
                ))
            
        con = calculate_winavg_pm(event)
        event.AQI = utils.EPATableCalc(con)
        
        aqi_map_index = "{}_{}".format(event.zipcode, event.timestamp.year)
        
        if self.location_year_aqi_map.get(aqi_map_index):
            self.location_year_aqi_map[aqi_map_index] = self.location_year_aqi_map[aqi_map_index].add(event.timestamp, event.AQI) 
        else:
            self.location_year_aqi_map[aqi_map_index] = TemporalSlidingWindow(event.timestamp, event.AQI)
     
     
    def execute(self, event):
        if event is None:
            return
        
        self._calculate_AQI(event)
        
    def emit(self):

#         def get_previous_aqis(location, years_range):
#             ret_val = []
#             index_formatter = location + "_{}"
#             for year_range in range(years_range):
#                 previous_aqi = self.location_year_aqi_map.get(index_formatter.format(2019 - year_range))
#                 if previous_aqi is None:
#                     previous_aqi = np.nan
#                 else:
#                     previous_aqi = np.nanmean(previous_aqi.get_array())
#                     
#                 ret_val.insert(0, previous_aqi)
#                 
#             return ret_val
            
        for location_year in self.location_year_aqi_map.keys():
            location, year = location_year.split("_")
            if year == "2020":
                previous_aqi = self.location_year_aqi_map.get("{}_{}".format(location, int(year)-1))
                
                if not previous_aqi:
                    continue
                
                current_aqi = self.location_year_aqi_map[location_year]
                
                
                current_aqi = np.nanmean(current_aqi.get_array())
                previous_aqi = np.nanmean(previous_aqi.get_array())
                
                
                aqi_improvment = previous_aqi - current_aqi
                
                if self.location_improvment_map.get(location):
                    self.location_improvment_map[location] = self.location_improvment_map[location] + aqi_improvment
                else:
                    self.location_improvment_map[location] = aqi_improvment
                    
        loc_improv = OrderedDict(sorted(self.location_improvment_map, key=lambda k, v:v))
        loc_improv_iter = iter(loc_improv)
        
        print("Top 3 most improved zipcodes:")
        print(next(loc_improv_iter))
        print(next(loc_improv_iter))
        print(next(loc_improv_iter))
                    
        
        
    
    def process(self, batch):
        
        events = self.pre_proc(batch)
        for event in events:
            if event:
#                 self.enrich(event)
#                 self.filter(event)
#                 self.execute(event)
                
                map(event, self.enrich)
                map(event, self.filter)
                map(event, self.execute)
                
        self.emit()
        

class TemporalSlidingWindow:

    def __init__(self, timestamp: datetime, value, window_hours=24):
        self.timed_window = OrderedDict({timestamp: value});
        self.window = -window_hours
        
    def add(self, timestamp: datetime, value):
        # Expand
        self.timed_window[timestamp] = value
        # Contract
        for key in self.timed_window.keys():
            if key < timestamp + timedelta(hours=self.window):
                self.timed_window.pop(key)
#             else:
#                 break;
            
    def get_array(self):
        return self.timed_window.values()


# if __name__ == '__main__':
#     proc = EventProcessor()
#     
#     gen = load_genrator.LoadGenerator()
#     
#     for record in gen.generate():
#         proc.process(record)
#     
#     for fd in utils.get_fd_from_zipfiles(SENSORS , MONTHSTAMPS):
#         for row in csv.DictReader(fd, delimiter=";"):
#             proc.process(row)
        
