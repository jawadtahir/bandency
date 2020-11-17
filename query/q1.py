
import logging

import grpc

import challenger_pb2
import challenger_pb2_grpc


class QueryOne:
    def __init__(self, challengerstub):
        self.challengerstub = challengerstub

    def run(self):
        locations = self.challengerstub.GetLocations(None)
        logging.info('QueryOne: %s', len(locations))


def main():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = challenger_pb2_grpc.ChallengerStub(channel)
        q1 = QueryOne(stub)
        q1.run()


if __name__ == "__main__":
    logging.basicConfig()
    main()
