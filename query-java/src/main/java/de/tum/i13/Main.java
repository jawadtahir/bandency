package de.tum.i13;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import de.tum.i13.helper.SerializationHelper;
import de.tum.i13.loadtest.LoadTest;
import de.tum.i13.query.LocationLookup;
import de.tum.i13.query.Query;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static de.tum.i13.helper.TimestampHelper.*;

public class Main {

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        LoadTest lt = new LoadTest();
        lt.run();

        return;
        /*
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
                .setBenchmarkType("evaluation") //Benchmark Type for testing
                .build();

        //Create a new Benchmark
        System.out.println("createNewBenchmark");
        Benchmark newBenchmark = challengeClient.createNewBenchmark(bc).get();

        //Get the locations
        Locations locations = challengeClient.getLocations(newBenchmark).get();
        LocationLookup ll = new LocationLookup(locations.getLocationsList());
        String cacheFile = System.getenv().get("CACHE_FILE");
        if(new File(cacheFile).exists()) {
            try {
                ll.restoreCache(SerializationHelper.fromFile(cacheFile));
            }catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("couldn't load cache");
            }
        }

        Query q1 = new Query(ll);

        //Start the benchmark
        challengeClient.startBenchmark(newBenchmark).get();
        System.out.println("started benchmark");

        //Process the events
        int cnt = 0;
        StopWatch sw = StopWatch.createStarted();
        Batch batch = challengeClient.nextBatch(newBenchmark).get();
        while(true) {
            if (batch.getLast()) { //Stop when we get the last batch
                System.out.println("Received lastbatch, finished!");
                break;
            }

            ListenableFuture<Batch> nextBatch = challengeClient.nextBatch(newBenchmark);

            //process the batch of events we have
            Triple<Timestamp, ArrayList<TopKCities>, ArrayList<TopKStreaks>> res = q1.calculateTopKImproved(batch);

            if(sw.getTime(TimeUnit.SECONDS) >= 2) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                printTopK(res.getLeft(), res.getMiddle(), cnt);
                System.out.println();
                printImproved(res.getLeft(), res.getRight(), cnt);

                sw.reset();
                sw.start();
            }

            ResultQ1 q1Result = ResultQ1.newBuilder()
                    .setBenchmarkId(newBenchmark.getId())
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllTopkimproved(res.getMiddle())
                    .build();

            //return the result of Q1
            //Send both results in parallel
            ListenableFuture<Empty> q1FutureRes = challengeClient.resultQ1(q1Result);

            ResultQ2 q2Result = ResultQ2.newBuilder()
                    .setBenchmarkId(newBenchmark.getId())
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllHistogram(res.getRight())
                    .build();

            ListenableFuture<Empty> q2FutureRes = challengeClient.resultQ2(q2Result);

            if(batch.getSeqId() > 0 && (batch.getSeqId() % 10_000) == 0) {
                writeQ1ToFile(q1Result, res.getLeft(), batch.getSeqId());
                writeQ2ToFile(q2Result, res.getLeft(), batch.getSeqId());
            }


            q1FutureRes.get();
            q2FutureRes.get();

            //if(cnt > 50_001)
            //    break;

            //get the next batch, we go for low latency and don't want to have a lot in flight
            batch = nextBatch.get();
            ++cnt;


            if(cnt > 9_000 && (cnt % 10_000) == 0) {
                try {
                    SerializationHelper.writeTooFile(cacheFile, ll.snapshotCache());
                } catch (Exception ex) {
                    System.out.println("couldn't snapshot cache");
                }
            }

            //if(cnt > 10_000) { //for testing you can
            //    break;
            //}
        }

        challengeClient.endBenchmark(newBenchmark).get();
        System.out.println("ended Benchmark");

         */
    }

    private static void printImproved(Timestamp ts, ArrayList<TopKStreaks> topKStreaks, int cnt) {
        Instant instant = timestampToInstant(ts);

        System.out.printf("Q2 - Streak Histogram: date: %s, batch: %s\n", instant.toString(), cnt);
        for(TopKStreaks streak : topKStreaks) {
            System.out.printf("bucket-from: %s, bucket-to: %s, bucket-percent: %2.2f\n", streak.getBucketFrom(), streak.getBucketTo(), streak.getBucketPercent() / 1000.0);
        }
        System.out.flush();
    }

    private static void writeQ1ToFile(ResultQ1 q1, Timestamp ts, long cnt) throws IOException {
        String print = JsonFormat.printer().print(ResultQ1.newBuilder().mergeFrom(q1));
        String filename = String.format("java_test_q1_batch-%s.json", cnt);

        try(FileWriter fw = new FileWriter(filename, true);
            BufferedWriter writer = new BufferedWriter(fw)) {

                writer.append(print);
                writer.flush();
        }
    }

    private static void writeQ2ToFile(ResultQ2 q2, Timestamp ts, long cnt) throws IOException {
        String print = JsonFormat.printer().print(ResultQ2.newBuilder().mergeFrom(q2));
        String filename = String.format("java_test_q2_batch-%s.json", cnt);

        try(FileWriter fw = new FileWriter(filename, true);
            BufferedWriter writer = new BufferedWriter(fw)) {
            writer.append(print);
            writer.flush();
        }
    }


    private static void printTopK(Timestamp ts, ArrayList<TopKCities> topKImproved, int cnt) {
        Instant instant = timestampToInstant(ts);

        System.out.printf("Q1 - Air quality improvement %s last 24h - date: %s batch: %s\n", topKImproved.size(), instant.toString(), cnt);
        for(TopKCities city : topKImproved) {
            System.out.printf("pos: %2s, city: %25.25s, avg imp.: %8.3f, curr-AQI-P1: %8.3f, curr-AQI-P2: %8.3f \n",
                            city.getPosition(),
                    city.getCity(), city.getAverageAQIImprovement()/1000.0, city.getCurrentAQIP1() / 1000.0,
                    city.getCurrentAQIP2()/ 1000.00);
        }
        System.out.flush();

    }
}
