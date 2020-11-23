import logging
from datetime import datetime

# If grpc is missing: pip install grpcio
import grpc
from google.protobuf import empty_pb2

# If the classes below are missing, generate them:
# python -m grpc_tools.protoc -I . --python_out=. --grpc_python_out=. challenger.proto
import challenger_pb2 as ch
import challenger_pb2_grpc as api

import challenger_pb2 as ch
import challenger_pb2_grpc as api

op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
      ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
    stub = api.ChallengerStub(channel)

    #Step 1 - get all locations
    loc = stub.getLocations(empty_pb2.Empty()) #get all the locations
    print("Fetched %s locations" % (len(loc.locations)))

    #Step 2 - Create a new Benchmark
    benchmarkconfiguration = ch.BenchmarkConfiguration(token="checkyourprofile",
                                                       batch_size=5000,
                                                       benchmark_name="shows_up_in_dashboard",
                                                       queries=[ch.BenchmarkConfiguration.Query.Q1])
    benchmark = stub.createNewBenchmark(benchmarkconfiguration)

    #Step 3 (optional) - Calibrate the latency
    ping = stub.initializeLatencyMeasuring(benchmark)
    for i in range(10):
        ping =stub.measure(ping)
    stub.endMeasurement(ping)

    #Step 4 - Start Eventprocessing
    stub.startBenchmark(benchmark)

    cnt_current = 0
    cnt_historic = 0
    cnt = 0

    batch = stub.nextMessage(benchmark)
    while batch:
        cnt_current += len(batch.current)
        cnt_historic += len(batch.lastyear)

        if(cnt % 100) == 0:
            ts_str = ""
            if len(batch.current) > 0:
                ts = batch.current[0].timestamp
                dt = datetime.utcfromtimestamp(ts.seconds)
                ts_str =dt.strftime("%Y-%m-%d %H:%M:%S.%f")

            print("processed %s - current_time: %s, num_current: %s, num_historic: %s, total_events: %s" % (cnt, ts_str, cnt_current, cnt_historic, ( cnt_current + cnt_historic)))


        #result_payload_q1 = processTheBatchQ1(batch) #here is your implementation ;)
        result_payload_q1 = ch.ResultQ1Payload(resultData=1)
        result = ch.ResultQ1(benchmark_id=benchmark.id,  #The id of the benchmark
                             payload_seq_id=batch.seq_id,
                             result=result_payload_q1)

        stub.resultQ1(result) #send the result of query 1, also send the result of Q2 in case you calculate both
        if batch.last or cnt > 1000: #here we just stop after 1000 so we see a result
            break

        cnt = cnt + 1
        batch = stub.nextMessage(benchmark)

    stub.endMeasurement(benchmark)

print("finished")
