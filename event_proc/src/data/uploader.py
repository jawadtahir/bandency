'''
Created on Oct 1, 2020

@author: jawad
'''
import os
import csv
import json
from zipfile import ZipFile
from pymongo import MongoClient



if __name__ == '__main__':
    csv_dir = "/media/jawad/debs2021/luftdaten/"
    contents = os.listdir(csv_dir)
    counter = 0
    with MongoClient() as db_client:
        db = db_client["gc21"]
        for content in contents:
            if counter < 10:
                if "zip" in content and os.path.isfile(os.path.join(csv_dir,content)):
                    with ZipFile(os.path.join(csv_dir,content), "r") as zip_fd:
                        for compressed_file in zip_fd.infolist():
                            extracted_file = zip_fd.extract(compressed_file) 
                            
                            print(extracted_file)
                            
                            csv_fd = open(extracted_file)
                            counter += 1
                            csv_reader = csv.DictReader(csv_fd, delimiter=';')
                            for record in csv_reader:
                                sensor_collection = db[record["sensor_type"]]
#                                 sensor_collection.insert(json.dumps(record))
                                sensor_collection.insert_one(record)
                                
                            os.remove(extracted_file)
                         
                
            else:
                break