import logging
import os
import pickle
from datetime import datetime, timedelta

import grpc
from google.protobuf import empty_pb2
from tqdm import tqdm

from event_processor import EventProcessor

from shapely.geometry import Point
from shapely.geometry.polygon import Polygon

import challenger_pb2 as ch
import challenger_pb2_grpc as api

import numpy as np
import utils

class Test:
    def __init__(self, challengerstub):
        self.challengerstub = challengerstub

    def get_locations(self):
        benchmarkconfiguration = ch.BenchmarkConfiguration(token="cpjcwuaeufgqqxhohhvqlyndjazvzymx",
                                                           batch_size=20000,
                                                           benchmark_name="test 2",
                                                           benchmark_type="test",
                                                           queries=[ch.BenchmarkConfiguration.Query.Q1, ch.BenchmarkConfiguration.Query.Q2])
        bench = self.challengerstub.createNewBenchmark(benchmarkconfiguration)

        return self.challengerstub.getLocations(bench)


def test():
    op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
          ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
    with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
    #with grpc.insecure_channel('127.0.0.1:8081', options=op) as channel:
        stub = api.ChallengerStub(channel)
        test = Test(stub)

        locs = test.get_locations().locations

        for i in range(0, 100):
            loc = locs[i]
            print("city: %s, plz: %s" % (loc.city, loc.zipcode))


if __name__ == "__main__":
    logging.basicConfig()
    test()
