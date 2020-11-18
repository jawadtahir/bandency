'''
Created on Oct 27, 2020

@author: foobar
'''
from data.record import Record

class LoadGenerator(object):
    '''
    Load generator for the event processor
    '''


    def __init__(self, sensors, month, year, historical_years_join):
        '''
        Constructor
        '''
        self.sensors = sensors
        self.month = month
        self.year = year
        self.historical_years_join = historical_years_join
        
        
    def generate(self):
        
        yield Record(self.events_map)
        