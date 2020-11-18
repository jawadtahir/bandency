'''
Created on Aug 21, 2020

@author: jawad
'''
import sys
import pulsar
import csv
import json
import time
import datetime as dt
from time import sleep

if __name__ == '__main__':
    print(sys.version)
#     client = pulsar.Client("pulsar://localhost:6650")
#     producer = client.create_producer("testTopic")
    
#     data_file = open("/home/jawad/Downloads/2015-10_ppd42ns.csv")
    
    counter = 0
    start_time = None
    finish_time = None
    
    with open("/home/jawad/Downloads/2015-10_ppd42ns.csv") as data_file:
        csv_reader = csv.reader(data_file)
        
        start_time = time.time()
        for record in csv_reader:
            try:
                json_str = json.dumps(record)
#                 producer.send(json_str.encode("utf-8"))
                print("Sent message: {}".format(json_str))
                counter += 1
            except:
                pass
            finally:
#                 producer.close()
                pass
        else:
            finish_time = time.time()
            elapsed_time = finish_time - start_time
            print("{} messages sent in {}".format(counter, elapsed_time))
                
            
        
