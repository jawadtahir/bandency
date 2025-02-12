package org.debs.challenger2;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Test {



    public static void main(String[] args) throws IOException {

        Date active = Date.from(Instant.parse("2025-02-12T07:54:22.935+00:00"));
        Date finished = Date.from(Instant.parse("2025-02-12T07:54:53.335+00:00"));

        Long runTime_ns = Duration.between(active.toInstant(), finished.toInstant()).toNanos();
        System.out.printf("NS: %d\n", runTime_ns);
        Long runTime_sec = Duration.between(active.toInstant(), finished.toInstant()).toSeconds();
        System.out.printf("Sec: %d\n", runTime_sec);

    }

    private static void msgPackTest() throws IOException {
        String dataDir = "/home/foobar/PhD/Data/DEBS/imaging/archive/L-PBF Dataset/Build 1/OT";
        Path dir = Paths.get(dataDir);
        List<Path> imageFilePaths = Files.list(dir).filter(f -> !f.endsWith(".tif")).collect(Collectors.toList());
        Long seq_id = 0L;
        for (Path imageFilePath : imageFilePaths) {
            byte[] imageData = Files.readAllBytes(imageFilePath);
            seq_id++;

            byte[] packedData = null;

            try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
                packer.packInt(seq_id.intValue()); //sequence
                packer.packInt(0); //print_id
                packer.packInt(0); //tile_id
                packer.packInt(0); //layer
                packer.packBinaryHeader(imageData.length);
                packer.writePayload(imageData);
                packedData = packer.toByteArray();
            }

            try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packedData)) {
                int seq = unpacker.unpackInt();
                int print = unpacker.unpackInt();
                int tile = unpacker.unpackInt();
                int layer = unpacker.unpackInt();
                int bin_size = unpacker.unpackBinaryHeader();
                byte[] packed_data = unpacker.readPayload(bin_size);
                packed_data = Arrays.copyOfRange(packed_data, 0, 50);
                System.out.printf("Seq_id = %d, print_id = %d, tile = %d, layer = %d, data = %s\n", seq, print, tile, layer, Arrays.toString(packed_data));
            }

        }
    }

    private static void createCollection() {
        String uri = "mongodb://localhost:52926/";
        String latencies = "testLatencies";
        String groups = "testGroups";
        String benchmarks = "testBenchmarks";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("challenger");
            List<String> names =  database.listCollectionNames().into(new ArrayList<>());

            if (!names.contains(groups)){
                database.createCollection(groups);
                database.getCollection(groups)
                        .createIndex(
                                Indexes.text("name"),
                                new IndexOptions().unique(true));
            } else {
                List<Document> indexes = database.getCollection(groups).listIndexes().into(new ArrayList<>());
                if (indexes.size() != 2){
                    database.getCollection(groups)
                            .createIndex(
                                    Indexes.text("name"),
                                    new IndexOptions().unique(true));
                }
            }

            if (!names.contains(benchmarks)){
                database.createCollection(benchmarks);
            }

            if (!names.contains(latencies)){
                TimeSeriesOptions timeSeriesOptions = new TimeSeriesOptions("timestamp");
                timeSeriesOptions.granularity(TimeSeriesGranularity.SECONDS);
                timeSeriesOptions.metaField("metadata");
                CreateCollectionOptions options = new CreateCollectionOptions();
                options.timeSeriesOptions(timeSeriesOptions);
                database.createCollection(latencies, options);
            }

        }
    }

    private static void populateTimeSeriesDB() {
        String uri = "mongodb://localhost:52926/";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("challenger");

            MongoCollection<Document> testTS = database.getCollection("test");
            Instant finishTIme = Instant.now().plus(50, ChronoUnit.SECONDS);
            Random rand = new Random();

            while (Instant.now().isBefore(finishTIme)){
                Document document = new Document();
                document = document.append("timestamp", Date.from(Instant.now()));
                document = document.append("latency", rand.nextLong(1_000_000L));
                document = document.append("metadata",
                        new Document("group_id",
                                new ObjectId("67898207d8fd450e551bbb8c"))
                                .append("query", 0));

                testTS.insertOne(document);
            }

        }
    }

    private static void getLatencyAnalysis() {
        String uri = "mongodb://localhost:52926/";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("challenger");
            MongoCollection<Document> benchmarks = database.getCollection("benchmarks");
            MongoCollection<Document> latencies = database.getCollection("latencies");

            Document last_benchmark = benchmarks.find(Filters.eq("group_id",
                            new ObjectId("67898207d8fd450e551bbb8c")))
                    .sort(Sorts.descending("creation_timestamp"))
                    .first();

            Bson match = Aggregates.match(Filters.and(
                    Filters.gte("timestamp", last_benchmark.get("activation_timestamp")),
                    Filters.lte("timestamp", last_benchmark.get("finished_timestamp")),
                    Filters.eq("metadata.group_id", last_benchmark.get("group_id"))));

            BsonField percentile = Accumulators.percentile("percentileLatency", "$latency", Arrays.asList(0.25,0.5,0.75,0.9), QuantileMethod.approximate());
            BsonField min = Accumulators.min("minLatency", "$latency");
            BsonField max = Accumulators.max("maxLatency", "$latency");
            BsonField count = Accumulators.sum("count", 1);
            Bson group = Aggregates.group("$metadata.query", min, max, percentile, count);

            List<Document> latencyAnalysis = latencies.aggregate(Arrays.asList(match,group)).into(new ArrayList<>());

            Document benchmarkResult = new Document();

            for (Document latAnal: latencyAnalysis){

                String queryNum = latAnal.get("_id").toString();
                Long minLat = latAnal.getLong("minLatency");
                Long maxLat = latAnal.getLong("maxLatency");
                Integer countResults = latAnal.getInteger("count");
                List<Double> percentiles = latAnal.getList("percentileLatency", Double.class);

                Document queryResults = new Document();
                queryResults = queryResults.append("percentiles", percentiles)
                        .append("min", minLat)
                        .append("max", maxLat)
                        .append("count", countResults);

//                queryResults = new Document(queryNum, queryResults);
                benchmarkResult.append(queryNum, queryResults);
            }
            Date startTime = last_benchmark.getDate("activation_timestamp");
            Date finishTime = last_benchmark.getDate("finished_timestamp");

            long runTime = Duration.between(startTime.toInstant(), finishTime.toInstant()).get(ChronoUnit.SECONDS);
            benchmarkResult.append("runtime", runTime);

            Bson results = Updates.set("test_results", benchmarkResult);

            benchmarks.findOneAndUpdate(Filters.eq("_id", last_benchmark.getObjectId("_id")), results);


        }
    }
}
