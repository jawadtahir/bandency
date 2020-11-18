
import logging
from datetime import datetime

import grpc
from google.protobuf import empty_pb2

import challenger_pb2 as ch
import challenger_pb2_grpc as api


class QueryOne:
    def __init__(self, challengerstub):
        self.challengerstub = challengerstub

    def measureLatency(self, benchmark):
        ping = self.challengerstub.initializeLatencyMeasuring(benchmark)
        for i in range(10):
            ping = self.challengerstub.measure(ping)

        self.challengerstub.endMeasurement(ping)


    def run(self):
        loc = self.challengerstub.getLocations(empty_pb2.Empty())
        print('got location data: %s' % len(loc.locations))

        benchmarkconfiguration = ch.BenchmarkConfiguration(token="abc", batch_size=5000, benchmark_name="test benchmark")
        bench = self.challengerstub.createNewBenchmark(benchmarkconfiguration)

        # First, we measure the latency.
        # This is only for the testing dashboard to substract the communication latency
        self.measureLatency(bench)

        #start the benchmark
        start_time = datetime.now()
        self.challengerstub.startBenchmark(bench)
        batch = self.challengerstub.nextMessage(bench)

        num_current = 0
        num_historic = 0
        cnt = 0

        while batch:
            num_current += len(batch.current)
            num_historic += len(batch.lastyear)
            if(cnt % 100) == 0:
                duration_so_far = (datetime.now() - start_time).total_seconds()
                print("processed %s in %s seconds - num_current: %s, num_historic: %s, total_events: %s" % (cnt, duration_so_far, num_current, num_historic, ( num_current + num_historic)))

            cnt = cnt + 1
            batch = self.challengerstub.nextMessage(bench)







def main():
    op = [('grpc.max_send_message_length', 100 * 1024 * 1024),
          ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
    with grpc.insecure_channel('challenge.msrg.in.tum.de:8081', options=op) as channel:
    #with grpc.insecure_channel('127.0.0.1:8081', options=op) as channel:
        stub = api.ChallengerStub(channel)
        q1 = QueryOne(stub)
        q1.run()


if __name__ == "__main__":
    #logging.basicConfig()
    print('get')
    main()
