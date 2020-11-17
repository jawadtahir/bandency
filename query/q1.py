
import logging

import grpc
from google.protobuf import empty_pb2

import challenger_pb2 as ch
import challenger_pb2_grpc


class QueryOne:
    def __init__(self, challengerstub):
        self.challengerstub = challengerstub

    def run(self):
        loc = self.challengerstub.GetLocations(empty_pb2.Empty())
        print('got location data')
        print('QueryOne: %s' % len(loc.locations))


def main():
    op = [('grpc.max_send_message_length', 100 * 1024 * 1024),
          ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
    with grpc.insecure_channel('127.0.0.1:8081', options=op) as channel:
        stub = challenger_pb2_grpc.ChallengerStub(channel)
        q1 = QueryOne(stub)
        q1.run()


if __name__ == "__main__":
    #logging.basicConfig()
    print('get')
    main()
