'''
Created on Oct 5, 2020

@author: jawad
'''

class EPAEntry(object):
    '''
    EPA breakpoint entries
    '''
    


    def __init__(self, C_high, C_low, I_high, I_low):
        '''
        Constructor
        '''
        
        self.C_high = C_high
        self.C_low = C_low
        self.I_high = I_high
        self.I_low = I_low
        
        
class EPATable(object):
    
    def __init__(self):
        self.table = []
        self.table.append(EPAEntry(12.0, 0.0, 50.0, 0.0))
        self.table.append(EPAEntry(35.4, 12.1, 100.0, 51.0))
        self.table.append(EPAEntry(55.4, 35.5, 150.0, 101.0))
        self.table.append(EPAEntry(150.4, 55.5, 200.0, 151.0))
        self.table.append(EPAEntry(250.4, 150.5, 300.0, 201.0))
        self.table.append(EPAEntry(350.4, 250.5, 400.0, 301.0))
        self.table.append(EPAEntry(500.4, 350.5, 500.0, 401.0))
        self.table.append(EPAEntry(99999.9, 500.5, 999.0, 501.0))
        
    def __getitem__(self, key):
        for entry in self.table:
            if entry.C_high >= key >= entry.C_low:
                return entry
            
        raise IndexError