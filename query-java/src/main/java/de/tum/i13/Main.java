package de.tum.i13;

import com.google.protobuf.Timestamp;
import de.tum.i13.helper.SerializationHelper;
import de.tum.i13.query.LocationLookup;
import de.tum.i13.query.Query;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.tum.i13.helper.TimestampHelper.timestampToInstant;

public class Main {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("challenge.msrg.in.tum.de", 5023)
                .usePlaintext()
                .maxRetryAttempts(10)
                .enableRetry()
                .build();


        var challengeClient = ChallengerGrpc.newBlockingStub(channel) //for demo, we show the blocking stub
                .withMaxInboundMessageSize(100 * 1024 * 1024)
                .withMaxOutboundMessageSize(100 * 1024 * 1024);

        BenchmarkConfiguration bc = BenchmarkConfiguration.newBuilder()
                .setBenchmarkName("Testrun " + new Date().toString())
                .setBatchSize(10000)
                .addQueries(BenchmarkConfiguration.Query.Q1)
                .addQueries(BenchmarkConfiguration.Query.Q2)
                .setToken(System.getenv().get("API_TOKEN")) //go to: https://challenge.msrg.in.tum.de/profile/
                .setBenchmarkType("test") //Benchmark Type for testing
                .build();

        //Create a new Benchmark
        System.out.println("createNewBenchmark");
        Benchmark newBenchmark = challengeClient.createNewBenchmark(bc);

        //Get the locations
        Locations locations = challengeClient.getLocations(newBenchmark);
        LocationLookup ll = new LocationLookup(locations.getLocationsList());
        String cacheFile = "/home/chris/temp/locationcache.obj";
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
        challengeClient.startBenchmark(newBenchmark);
        System.out.println("started benchmark");

        //Process the events
        int cnt = 0;
        StopWatch sw = StopWatch.createStarted();
        while(true) {
            Batch batch = challengeClient.nextBatch(newBenchmark);
            if (batch.getLast()) { //Stop when we get the last batch
                System.out.println("Received lastbatch, finished!");
                break;
            }

            //process the batch of events we have
            Pair<Timestamp, ArrayList<TopKCities>> timestampArrayListPair = q1.calculateTopKImproved(batch);

            if(sw.getTime(TimeUnit.SECONDS) >= 2) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
                printTopK(timestampArrayListPair, cnt);
                sw.reset();
                sw.start();
            }

            ResultQ1 q1Result = ResultQ1.newBuilder()
                    .setBenchmarkId(newBenchmark.getId())
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllTopkimproved(timestampArrayListPair.getRight())
                    .build();


            //return the result of Q1
            challengeClient.resultQ1(q1Result);
            //com.google.protobuf.u


            var histogram = calculateHistogram(batch);
            ResultQ2 q2Result = ResultQ2.newBuilder()
                    .setBenchmarkId(newBenchmark.getId())
                    .setBatchSeqId(batch.getSeqId()) //set the sequence number
                    .addAllHistogram(histogram)
                    .build();

            challengeClient.resultQ2(q2Result);
            ++cnt;


            if(cnt > 9_000 && (cnt % 10_000) == 0) {
                try {
                    SerializationHelper.writeTooFile(cacheFile, ll.snapshotCache());
                } catch (Exception ex) {
                    System.out.println("couldn't snapshot cache");
                }
            }

            if(cnt > 100_000) { //for testing you can
                break;
            }
        }

        challengeClient.endBenchmark(newBenchmark);
        System.out.println("ended Benchmark");
    }

    private static void printTopK(Pair<Timestamp, ArrayList<TopKCities>> pair, int cnt) {
        Instant instant = timestampToInstant(pair.getLeft());
        var topKImproved = pair.getRight();

        System.out.printf("Q1 - Air quality improvement %s last 24h - date: %s batch: %s\n", topKImproved.size(), instant.toString(), cnt);
        for(TopKCities city : topKImproved) {
            System.out.printf("pos: %2s, city: %25.25s, avg imp.: %8.3f, curr-AQI-P1: %8.3f, curr-AQI-P2: %8.3f \n",
                            city.getPosition(),
                    city.getCity(), city.getAverageAQIImprovement()/1000.0, city.getCurrentAQIP1() / 1000.0,
                    city.getCurrentAQIP2()/ 1000.00);
        }
        System.out.flush();

    }

    private static List<TopKStreaks> calculateHistogram(Batch batch) {
        //TODO: improve implementation

        return new ArrayList<>();
    }

    private static List<TopKCities> calculateTopKImproved(Batch batch) {
        //TODO: improve this implementation

        return new ArrayList<>();
    }
}
