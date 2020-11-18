'''
Created on Oct 27, 2020

@author: foobar
'''

class Event(object):
    def __init__(self, **kwargs):
        for key in kwargs:
            setattr(self, key, kwargs[key])
        


class Record(object):
    '''
    Record class
    '''


    def __init__(self, events: dict):
        '''
        '''
        self.events = events
        