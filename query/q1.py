from google.protobuf import empty_pb2
import grpc

import challenger_pb2 as ch
import challenger_pb2_grpc as api

op = [('grpc.max_send_message_length', 10 * 1024 * 1024),
      ('grpc.max_receive_message_length', 100 * 1024 * 1024)]
#with grpc.insecure_channel('127.0.0.1:5023', options=op) as channel:
with grpc.insecure_channel('challenge2024.debs.org:5023', options=op) as channel:
    stub = api.ChallengerStub(channel)

    benchmarkconfiguration = ch.BenchmarkConfiguration(
        token="mfoyelxsrpcroimhrlhiypfxyezvyhbm",
        benchmark_name="shows_up_in_dashboard",
        benchmark_type="test",
        queries=[ch.Query.Q1, ch.Query.Q2])
    benchmark = stub.createNewBenchmark(benchmarkconfiguration)

    stub.startBenchmark(benchmark)

    states_count = 0

    while True:
        batch = stub.nextBatch(benchmark)
        states_count = states_count + len(batch.states)
        #print(f"Got some states: {len(batch.states)}, so far: {states_count}, models: {batch.models}")
        if len(batch.states) > 0:
            first = batch.states[0]
            print(f"First Serial number: {first.serial_number}, normalized: {first.normalized}")

            

        
            resultQ1 = ch.ResultQ1(benchmark_id=benchmark.id, #The id of the benchmark
                                batch_seq_id=batch.seq_id) # Todo add more interesting query results
            stub.resultQ1(resultQ1)  # send the result of query 1 back

            # do the same for Q2
            resultQ2 = ch.ResultQ2(benchmark_id=benchmark.id, #The id of the benchmark
                                batch_seq_id=batch.seq_id, #The sequence id of the batch
                                # Todo add more interesting query results
                                )
            stub.resultQ2(resultQ2)

        if batch.last:
            print(f"received last batch, total batches: {states_count}")
            stub.endBenchmark(benchmark)
            break
