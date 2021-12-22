
from google.protobuf import empty_pb2
import grpc

import challenger_pb2 as ch
import challenger_pb2_grpc as api

op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
      ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
with grpc.insecure_channel('127.0.0.1:8081', options=op) as channel:
#with grpc.insecure_channel('challenge.msrg.in.tum.de:5023', options=op) as channel:
    stub = api.ChallengerStub(channel)

    benchmarkconfiguration = ch.BenchmarkConfiguration(
        token="vcgeajpqzwrfuwytvqyxypjuksgbraeg",
        benchmark_name="shows_up_in_dashboard",
        benchmark_type="test",
        queries=[ch.Query.Q1, ch.Query.Q2])
    benchmark = stub.createNewBenchmark(benchmarkconfiguration)

    stub.startBenchmark(benchmark)

    event_count = 0

    while True:
        batch = stub.nextBatch(benchmark)
        event_count = event_count + len(batch.events)

        def queryResults(symbols:list[str]) -> list[ch.Indicator]:
            # Your part: calculate the indicators for the given symbols
            return list()

        #topkimproved = processTheBatchQ1(batch) #here is your implementation ;)
        resultQ1 = ch.ResultQ1(benchmark_id=benchmark.id, #The id of the benchmark
                            batch_seq_id=batch.seq_id, #The sequence id of the batch
                            indicators=queryResults(batch.lookup_symbols))
        stub.resultQ1(resultQ1)  # send the result of query 1 back

        def crossoverEvents() -> list[ch.CrossoverEvent]:
            #Your part: calculate the crossover events
            return list()

        # do the same for Q2
        resultQ2 = ch.ResultQ2(benchmark_id=benchmark.id, #The id of the benchmark
                            batch_seq_id=batch.seq_id, #The sequence id of the batch
                            crossover_events=crossoverEvents()) 
        stub.resultQ2(resultQ2)

        if batch.last:
            print(f"received last batch, total batches: {event_count}")
            stub.endBenchmark(benchmark)
            break
