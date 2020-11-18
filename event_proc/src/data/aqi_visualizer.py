'''
Created on Oct 2, 2020

@author: jawad
'''
from getpass import getuser
from zipfile import ZipFile
import os

from geopy.distance import distance
from pandas.core.frame import DataFrame
from pymongo import MongoClient
import pymongo

from utils import ROOT_DIR
import numpy as np
import pandas as pd
import utils


MONGO_HOST = "localhost"
MONGO_PORT = 27017
MONGO_DB = "gc21"



loc_cache = pd.DataFrame(columns=[
                         "location", "distance", "city_ascii", "lat", "lng"])
loc_cache.set_index("location")


def get_data(sensor: str) -> dict:
    loc_particle_map = {}
    db_client = MongoClient(MONGO_HOST, MONGO_PORT)
    db = db_client[MONGO_DB]
    collection = db[sensor]
    locations = collection.find({}).distinct("location")

    for location in locations:
        print(location)
        particle_arr = []
        timestamp_arr = []
        loc_docs_query = {"location": location, "P2": {"$ne": ""}}
        loc_docs_curser = collection.find(loc_docs_query).sort(
            "timestamp", pymongo.ASCENDING)
        for loc_doc in loc_docs_curser:
            timestamp_arr.append(loc_doc["timestamp"])
            particle_arr.append(float(loc_doc["P2"]))

        loc_docs = np.array([timestamp_arr, particle_arr])
        loc_docs = pd.DataFrame(loc_docs.T, columns=["Timestamp", "P2"])
        loc_docs["Timestamp"] = pd.to_datetime(loc_docs["Timestamp"])
        loc_docs["P2"] = loc_docs["P2"].astype(np.float)
        loc_particle_map[location] = loc_docs

    return loc_particle_map

    pass


def calculate_aqi(raw_data: DataFrame):

    def calc(C):
        try:
            epa_entry = utils.epa_table[C]
            fraction = (epa_entry.I_high - epa_entry.I_low) / \
                (epa_entry.C_high - epa_entry.C_low)
            return (fraction * (C - epa_entry.C_low)) + epa_entry.I_low
        except IndexError:
            return np.nan

    loc_aqi_map = {}

    data_per_city = raw_data.groupby("city_ascii")
    for city in data_per_city.groups.keys():
        city_data = data_per_city.get_group(city)
        city_data = city_data.sort_values("daystamp")
        avg_p2 = city_data.groupby("daystamp")["P2"].mean()

        aqi = avg_p2.apply(calc)
        avg_aqi = aqi.rolling(7, 1).mean()

        loc_aqi_map[city] = pd.DataFrame({"AQI": aqi, "Averaged AQI": avg_aqi})

    return loc_aqi_map


def plot_aqis(aqis):
    utils.ensure_dir(utils.FIG_DIR)
    for location in aqis.keys():
        df = aqis[location]
        df.plot().figure.savefig(os.path.join(utils.FIG_DIR, location))

    print("Finsished")


def resolve_time(df: pd.DataFrame):
    df["timestamp"] = pd.to_datetime(
        df["timestamp"], infer_datetime_format=True)
    df["daystamp"] = pd.to_datetime(
        {"year": df["timestamp"].dt.year, "month": df["timestamp"].dt.month, "day": df["timestamp"].dt.day})
    #df["year"], df["month"], df["day"] = df["timestamp"].dt.year, df["timestamp"].dt.month, df["timestamp"].dt.day


def resolve_locations(df: pd.DataFrame):
    def resolve_city(loc_row):
        loc_lat_long = (loc_row["lat"], loc_row["lon"])
        dist_df = utils.cities_lat_long_df.apply(
            lambda row: distance(loc_lat_long, (row["lat"], row["lng"])).km, axis=1)

        city = utils.cities_lat_long_df.loc[dist_df.idxmin()]
        city["location"] = loc_row["location"]
        dist = dist_df.min()

        if dist > utils.CITY_RADIUS_KM:
            print("Couldn't found a nearby city. Sensor's LatLong is {}, {}. Nearest city is {}, {} kms away.".format(
                loc_row["lat"], loc_row["lon"], city["city"], dist))

        return city
    unique_locs = df[["location", "lat", "lon"]]
    unique_locs = unique_locs.loc[unique_locs.drop_duplicates().index]

    resolvd_locs = unique_locs.apply(resolve_city, axis=1)

    return pd.merge(df, resolvd_locs, how="left", on="location")


def get_data_from_zipfiles(sensors: list, months: list) -> pd.DataFrame:
    if len(sensors) == 0 or len(months) == 0:
        return None
    data = None
    for month in months:
        for sensor in sensors:
            filename = "{}_{}.zip".format(month, sensor)
            if os.path.isfile(os.path.join(utils.DATA_DIR, filename)):
                with ZipFile(os.path.join(utils.DATA_DIR, filename)) as zip_fd:
                    print("Processing {}".format(filename))
                    for compressed_csv in zip_fd.infolist():
                        uncom_csv = zip_fd.extract(compressed_csv)
                        csv_df = pd.read_csv(uncom_csv, ";")
                        if data is None:
                            data = csv_df
                        else:
                            data = pd.concat(
                                [data, csv_df], copy=False, ignore_index=True)

                        os.remove(uncom_csv)

            else:
                print("File {} couldn't be found".format(filename))

    return data


def get_data_from_files(sensors: list, months: list) -> pd.DataFrame:
    if len(sensors) == 0 or len(months) == 0:
        return None
    data = None
    for month in months:
        for sensor in sensors:
            filename = "{}_{}.csv".format(month, sensor)
            filename = os.path.join(ROOT_DIR, filename)
            if os.path.isfile(filename):
                print("Processing {}".format(filename))
                csv_df = pd.read_csv(filename)
                if data is None:
                    data = csv_df
                else:
                    data.append(csv_df, verify_integrity=True)

            else:
                print("File {} couldn't be found".format(filename))

    return data


if __name__ == '__main__':
    #raw_data = get_data_from_files(["ppd42ns", "pms3003"], ["2020-03"])
    #     raw_data = get_data_from_zipfiles(["ppd42ns", "pms3003"], ["2020-03"])
    raw_data = get_data_from_zipfiles(["pms1003", "pms3003", "pms5003", "pms7003", "ppd42ns"], [
                                      "2020-03", "2020-02", "2020-01"])
    raw_data = resolve_locations(raw_data)
    resolve_time(raw_data)
    aqis = calculate_aqi(raw_data)

    plot_aqis(aqis)
    print("Finsished")
