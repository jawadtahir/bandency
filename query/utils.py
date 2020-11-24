'''
Created on 13.10.2020

@author: ga53wuq
'''
from datetime import datetime
from pathlib import Path
import os


from zipfile import ZipFile
from getpass import getuser
import numpy
from epa import EPATable


EARTH_RADIUS = 6371
CITY_RADIUS_KM = 15

Path

ROOT_DIR = os.path.abspath(os.path.join(
    os.path.dirname(os.path.abspath(__file__)), os.pardir))
FIG_DIR = os.path.join(ROOT_DIR, "figures", str(datetime.now()).split(".")[0])
DATA_DIR = "/media/{}/debs2021/luftdaten".format(getuser())

# cities_lat_long_df = pandas.read_csv(
#     open(os.path.join(ROOT_DIR, "worldcities.csv")))
epa_table = EPATable()


def ensure_dir(dir):
    os.makedirs(dir, exist_ok=True)
    
    
def EPATableCalc(C):
        try:
            epa_entry = epa_table[C]
            fraction = (epa_entry.I_high - epa_entry.I_low) / \
                (epa_entry.C_high - epa_entry.C_low)
            return (fraction * (C - epa_entry.C_low)) + epa_entry.I_low
        except IndexError:
            return numpy.nan    
    
    
    
def it_fd_from_zipfiles(sensors: list, months: list):
    """
    sensor: list of sensor name
    months:list of months, can be a map of month where value is a list of joined months
    """
    if len(sensors) == 0 or len(months) == 0:
        return None
    if isinstance(months, list):
        for month in months:
            for sensor in sensors:
                filename = "{}_{}.zip".format(month, sensor)
                if os.path.isfile(os.path.join(DATA_DIR, filename)):
                    with ZipFile(os.path.join(DATA_DIR, filename)) as zip_fd:
                        print("Processing {}".format(filename))
                        for compressed_csv in zip_fd.infolist():
                            uncom_csv = zip_fd.extract(compressed_csv)
                            yield uncom_csv
                            os.remove(uncom_csv)
    
                else:
                    print("File {} couldn't be found".format(filename))

def get_fd_from_zipfiles(sensors: list, months) -> list:
    """
    sensor: list of sensor name
    months:list of months, can be a map of month where value is a list of joined months
    """
    if len(sensors) == 0 or len(months) == 0:
        return None
    if isinstance(months, list):
        for month in months:
            for sensor in sensors:
                filename = "{}_{}.zip".format(month, sensor)
                if os.path.isfile(os.path.join(DATA_DIR, filename)):
                    with ZipFile(os.path.join(DATA_DIR, filename)) as zip_fd:
                        print("Processing {}".format(filename))
                        for compressed_csv in zip_fd.infolist():
                            uncom_csv = zip_fd.extract(compressed_csv)
                            yield uncom_csv
                            os.remove(uncom_csv)
    
                else:
                    print("File {} couldn't be found".format(filename))

    else:
        for month in months.keys():
            for sensor in sensors:
                filename = "{}_{}.zip".format(month, sensor)
                joined_filename = "{}_{}.zip".format(months[month], sensor)
                
                filepath = os.path.join(DATA_DIR, filename)
                joined_filepath = os.path.join(DATA_DIR, joined_filename)
                
                is_filename = os.path.isfile(filepath)
                is_joined_filename = os.path.isfile(joined_filepath)
                
                if is_filename and is_joined_filename:
                    with (ZipFile(filepath), ZipFile(joined_filepath)) as (zip_fd, zip_jfd):
                        print("Processing {} and {}".format(filename, joined_filename))
                        for compressed_csv in zip_fd.infolist():
                            for compressed_jcsv in zip_jfd.infolist():
                                
                                uncom_csv = zip_fd.extract(compressed_csv)
                                uncom_jcsv = zip_jfd.extract(compressed_jcsv)
                                yield uncom_csv, uncom_jcsv
                                os.remove(uncom_csv)
                                os.remove(uncom_jcsv)
    
                else:
                    print("File {} and {} couldn't be found".format(filename, joined_filename))