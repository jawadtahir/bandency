
from google.protobuf import empty_pb2
import grpc

import challenger_pb2 as ch
import challenger_pb2_grpc as api

op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
      ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
    stub = api.ChallengerStub(channel)

    benchmarkconfiguration = ch.BenchmarkConfiguration(
        token="vcgeajpqzwrfuwytvqyxypjuksgbraeg",
        benchmark_name="shows_up_in_dashboard",
        benchmark_type="test",
        queries=[ch.BenchmarkConfiguration.Query.Q1])
    benchmark = stub.createNewBenchmark(benchmarkconfiguration)

    batch = stub.nextBatch(benchmark)
    while batch:
        #topkimproved = processTheBatchQ1(batch) #here is your implementation ;)
        resultQ1 = ch.ResultQ1(benchmark_id=benchmark.id,  #The id of the benchmark
                            batch_seq_id=batch.seq_id)

        stub.resultQ1(resultQ1)  # send the result of query 1, also send the result of Q2 in case you calculate both

        # do the same for Q2
        resultQ2 = ch.ResultQ1(benchmark_id=benchmark.id,  #The id of the benchmark
                            batch_seq_id=batch.seq_id)
        stub.resultQ2(resultQ2)

        if batch.last:
            break

        batch = stub.nextMessage(benchmark)

    stub.endBenchmark(benchmark)
