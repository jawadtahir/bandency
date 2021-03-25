package de.tum.i13.loadtest;

import de.tum.i13.Batch;
import de.tum.i13.Benchmark;
import de.tum.i13.BenchmarkConfiguration;
import de.tum.i13.ChallengerGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        LoadTest lt = new LoadTest();
        lt.run();
        return;
    }

    public void run() throws ExecutionException, InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(System.getenv("API_URL"), 5023)
                .usePlaintext()
                .maxRetryAttempts(1000)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(15, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(false)
                .enableRetry()
                .build();

        var challengeClient = ChallengerGrpc.newFutureStub(channel) //for demo, we show the blocking stub
                .withMaxInboundMessageSize(100 * 1024 * 1024)
                .withMaxOutboundMessageSize(100 * 1024 * 1024);

        BenchmarkConfiguration bc = BenchmarkConfiguration.newBuilder()
                .setBenchmarkName("Testrun " + new Date().toString())
                .setBatchSize(10000)
                .addQueries(BenchmarkConfiguration.Query.Q1)
                .addQueries(BenchmarkConfiguration.Query.Q2)
                .setToken(System.getenv().get("API_TOKEN")) //go to: https://challenge.msrg.in.tum.de/profile/
                //.setBenchmarkType("test") //Benchmark Type for testing
                .setBenchmarkType("evaluation") //Benchmark Type for testing
                .build();


        System.out.println("createNewBenchmark");
        Benchmark newBenchmark = challengeClient.createNewBenchmark(bc).get();

        int count = 30_000;

        CountDownLatch latch = new CountDownLatch(count);

        ExecutorService executorService = Executors.newFixedThreadPool(16);
        AtomicLong al = new AtomicLong();
        AtomicLong messageCount = new AtomicLong();
        System.out.println("begin");
        StopWatch sw = new StopWatch();
        sw.reset();
        sw.start();
        for(int i = 0; i < count; ++i) {
            executorService.submit(() -> {
                try {
                    Batch batch = challengeClient.nextBatch(newBenchmark).get();

                    al.addAndGet(batch.getSeqId());
                    messageCount.addAndGet(batch.getCurrentCount());
                    messageCount.addAndGet(batch.getLastyearCount());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }

        long expectedSumOfSeqIds = 0;
        for(int i = 0; i < count; ++i) {
            expectedSumOfSeqIds += i;
        }
        latch.await();
        sw.stop();
        executorService.shutdown();

        System.out.println("Received all batches: " + (expectedSumOfSeqIds == al.get()));
        System.out.println("Amount of batches: " + count);

        System.out.format("batches per second throughput: %.2f\n", (double)count/(double)sw.getNanoTime()*1_000_000_000.0);
        System.out.format("events per second throughput: %.2f\n", (double)messageCount.get()/(double)sw.getNanoTime()*1_000_000_000.0);
        System.out.println("finished: " + sw.formatTime());
        System.out.println("cnt: " + expectedSumOfSeqIds + " al: " + al.get());

        return;
    }
}
