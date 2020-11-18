'''
Created on Aug 21, 2020

@author: jawad
'''
import sys
import pulsar
import time
from _pulsar import MessageId

current_milli_time = lambda: int(round(time.time() * 1000))

def read_data_file(path):
    pass

if __name__ == '__main__':
    print(sys.version)
    client = pulsar.Client("pulsar://localhost:6650")
    reader = client.create_reader("testTopic", MessageId.earliest)
    
    data_file = open("/home/jawad/Downloads/2015-10_ppd42ns.csv")
#     consumer = client.subscribe("testTopic", "testSub")
    
    while True:
        
#         try:
            msg = reader.read_next()
            now = current_milli_time()
            pub_time = time.gmtime(msg.publish_timestamp())
            print("Message published at {} and received at {}".format(msg.publish_timestamp(), now))
            
            
#             consumer.acknowledge(msg)
#         
#         except:
#             consumer.negative_acknowledge(msg)
