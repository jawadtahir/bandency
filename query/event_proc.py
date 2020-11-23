class EventProcessor:

    def __init__(self):
        self.location_pm_window_map = {}
        self.location_year_aqi_map = {}
        self.last_processed_day = None
        self.location_aqi_diff_map = {}
        self.location_improvment_map = {}
        
        self.location_zip_cache = {}
        self.zipcode_geojson = {}
        with open(ZIPCODE_GEOJSON_PATH) as f:
            self.zipcode_geojson = json.load(f)
        
    def _resolve_location(self, event):
        if self.location_zip_cache.get(event["location"]):
            event["zipcode"] = self.location_zip_cache[event["location"]] 
            return
        
        point = Point(event["lon"], event["lat"])
        for feature in self.zipcode_geojson["features"]:
            polygon = shape(feature["geometry"])
            if polygon.contains(point):
                self.location_zip_cache[event["location"]] = (feature["properties"])["plz"]
                return
        
        self.location_zip_cache[event["location"]] = UNKNOWN
        event["zipcode"] = UNKNOWN 
        
        
    def pre_proc(self, raw_record):
        pass
    
        
    def filter(self, event):
        if event["zipcode"] is UNKNOWN:
            event = None
        
    def enrich(self, event):
            
        self._resolve_location(event)
        
        event["timestamp"] = datetime.fromtimestamp(event["timestamp"])
        
        if not event["P2"]:
            event["P2"] = np.nan
    
    def calculate(self, event):
        self._get_zipcode(event)
        self.calculate_aqi(event)
        self.find_diff_avg(event)
        self.announce_winner(event)
    
    def _calculate_AQI(self, event):

        def calculate_winavg_pm (event):
            
            if self.location_pm_window_map.get(event["zipcode"]):
                pm_window = self.location_pm_window_map[event["zipcode"]]
                pm_window.add(event["timestamp"], event["P2"])
                
            else:
                pm_window = TemporalSlidingWindow(event["timestamp"], event["P2"])
                self.location_pm_window_map[event["zipcode"]] = pm_window 
                
            return np.nanmean(pm_window.get_array(
                ))
            
        con = calculate_winavg_pm(event)
        event["AQI"] = utils.EPATableCalc(con)
        
        aqi_map_index = "{}_{}".format(event["zipcode"], event["timestamp"].year)
        
        if self.location_year_aqi_map.get(aqi_map_index):
            self.location_year_aqi_map[aqi_map_index] = self.location_year_aqi_map[aqi_map_index].add(event["timestamp"], event["AQI"]) 
        else:
            self.location_year_aqi_map[aqi_map_index] = TemporalSlidingWindow(event["timestamp"], event["AQI"])
     
    def execute(self, event):
        if event is None:
            return
        
        self._calculate_AQI(event)
        
    def emit(self, event):

        def get_previous_aqis(location, years_range):
            ret_val = []
            index_formatter = location + "_{}"
            for year_range in range(years_range):
                previous_aqi = self.location_year_aqi_map.get(index_formatter.format(2019 - year_range))
                if previous_aqi is None:
                    previous_aqi = np.nan
                else:
                    previous_aqi = np.nanmean(previous_aqi.get_array())
                    
                ret_val.insert(0, previous_aqi)
                
            return ret_val
            
        def announce_winners(timestamp):
            for location_year in self.location_year_aqi_map.keys():
                location, year = location_year.split("_")
                if year is 2020:
                    current_aqi = self.location_year_aqi_map[location_year]
                    avg_prev_aqi = np.nanmean(get_previous_aqis(location, 3))
                    
                    aqi_improvment = avg_prev_aqi - current_aqi
                    
                    if self.location_improvment_map.get(location):
                        self.location_improvment_map[location] = self.location_improvment_map[location] + aqi_improvment
                    else:
                        self.location_improvment_map[location] = aqi_improvment
                        
            loc_improv = OrderedDict(sorted(self.location_improvment_map, key=lambda k, v:v))
            loc_improv_iter = iter(loc_improv)
            
            print("Top 3 most improved zipcodes on {}\-{}\-{}".format(timestamp.day, timestamp.month, timestamp.year))
            print(next(loc_improv_iter))
            print(next(loc_improv_iter))
            print(next(loc_improv_iter))
                    
        if  event is None:
            return
        
        if self.last_processed_day is None:
            self.last_processed_day = event["timestamp"].day
            return
        
        if event["timestamp"].year is 2020:
            if event["timestamp"].day > self.last_processed_day:
                announce_winners(event["timestamp"])
                self.last_processed_day = event["timestamp"].day
    
    def process(self, raw_record):
        
        events = self.pre_proc(raw_record)
        for event in events:
            if event:
                self.enrich(event)
                self.filter(event)
                self.execute(event)
                self.emit(event)
                
#                 map(event, self.enrich)
#                 map(event, self.filter)
#                 map(event, self.execute)
#                 map(event, self.emit)
        

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

