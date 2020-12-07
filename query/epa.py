'''
Created on Oct 5, 2020

@author: jawad
'''


class EPAEntry(object):
    '''
    EPA breakpoint entries
    '''

    def __init__(self, C_high, C_low, I_high, I_low, description):
        '''
        Constructor
        '''

        self.C_high = C_high
        self.C_low = C_low
        self.I_high = I_high
        self.I_low = I_low
        self.description = description


class EPATableP2(object):

    def __init__(self):
        self.table = []
        self.table.append(EPAEntry(12.0, 0.0, 50.0, 0.0, "good"))
        self.table.append(EPAEntry(35.4, 12.1, 100.0, 51.0, "moderate"))
        self.table.append(EPAEntry(55.4, 35.5, 150.0, 101.0,
                                   "unhealthy for sensitive groups"))
        self.table.append(EPAEntry(150.4, 55.5, 200.0, 151.0, "unhealthy"))
        self.table.append(
            EPAEntry(250.4, 150.5, 300.0, 201.0, "very unhealthy"))
        self.table.append(EPAEntry(350.4, 250.5, 400.0, 301.0, "hazardous"))
        self.table.append(EPAEntry(500.4, 350.5, 500.0, 401.0, "hazardous"))
        self.table.append(EPAEntry(99999.9, 500.5, 999.0, 501.0, "hazardous"))

    def __getitem__(self, key):
        kr = round(key, 1)
        for entry in self.table:
            if entry.C_high >= kr >= entry.C_low:
                return entry
        print("error epatable- key: %s kr: %s" % (key, kr))
        raise IndexError


class EPATableP1(object):

    def __init__(self):
        self.table = []
        self.table.append(EPAEntry(54.0, 0.0, 50.0, 0.0, "good"))
        self.table.append(EPAEntry(154.0, 55.0, 100.0, 51.0, "moderate"))
        self.table.append(EPAEntry(254.0, 155.0, 150.0, 101.0,
                                   "unhealthy for sensitive groups"))
        self.table.append(EPAEntry(354.0, 255.0, 200.0, 151.0, "unhealthy"))
        self.table.append(
            EPAEntry(424.0, 355.0, 300.0, 201.0, "very unhealthy"))
        self.table.append(EPAEntry(504.0, 425.0, 400.0, 301.0, "hazardous"))
        self.table.append(EPAEntry(604.0, 505.0, 500.0, 401.0, "hazardous"))
        self.table.append(EPAEntry(99999.0, 605.0, 999.0, 501.0, "hazardous"))

    def __getitem__(self, key):
        kr = round(key)
        for entry in self.table:
            if entry.C_high >= kr >= entry.C_low:
                return entry
        print("error epatable- key: %s kr: %s" % (key, kr))
        raise IndexError
