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
                                                       benchmark_name="shows_up_in_dashboard")
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
            print("processed %s - num_current: %s, num_historic: %s, total_events: %s" % (cnt, cnt_current, cnt_historic, ( cnt_current + cnt_historic)))

    #result_payload = processTheBatch(batch) #here is your implementation ;)
        result_payload = ch.ResultPayload(resultData=1)
        result = ch.Result(benchmark_id=benchmark.id, #The id of the benchmark
                       payload_seq_id=batch.seq_id,
                       result=result_payload)

        stub.processed(result) #send the result
        if batch.last:
            break

        cnt = cnt + 1
        batch = stub.nextMessage(benchmark)

print("finished")
