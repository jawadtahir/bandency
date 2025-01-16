package org.debs.challenger2.db;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.*;

public class MongoQueries implements IQueries {


    public static final String COLLECTION_GROUPS = "groups";
    public static final String COLLECTION_BENCHMARKS = "benchmarks";
    public static final String COLLECTION_LATENCY = "latencies";
    public static final String GROUP_API_KEY = "apikey";
    public static final String ID = "_id";

    private final MongoClient client;
    private final String database;


    public MongoQueries(String connectionString, String database){
        client = MongoClients.create(connectionString);
        this.database = database;
        MongoDatabase db = client.getDatabase(database);
        try{
            db.getCollection(COLLECTION_BENCHMARKS);
        } catch (IllegalArgumentException e){
            db.createCollection(COLLECTION_BENCHMARKS);
            db.getCollection(COLLECTION_BENCHMARKS).createIndex(Indexes.text("group_id"));
        }
        try {
            db.getCollection(COLLECTION_LATENCY);
        } catch (IllegalArgumentException e){
            TimeSeriesOptions timeSeriesOptions = new TimeSeriesOptions("timestamp");
            timeSeriesOptions.granularity(TimeSeriesGranularity.SECONDS);
            timeSeriesOptions.metaField("metadata");
            CreateCollectionOptions options = new CreateCollectionOptions();
            options.timeSeriesOptions(timeSeriesOptions);
            db.createCollection(COLLECTION_LATENCY, options);
        }
    }

    @Override
    public boolean checkIfGroupExists(String token) {
        MongoDatabase db = client.getDatabase(database);
        Document group = db.getCollection(COLLECTION_GROUPS).find(Filters.eq(GROUP_API_KEY, token)).first();

        return group != null;
    }

    @Override
    public ObjectId getGroupIdFromToken(String token) {
        MongoDatabase db = client.getDatabase(database);
        Document group = db.getCollection(COLLECTION_GROUPS).find(Filters.eq(GROUP_API_KEY, token)).first();
        if (group != null){
            return group.get(ID, ObjectId.class);
        } else {
            return null;
        }
    }

    @Override
    public String getGroupNameFromToken(String token) {
        return null;
    }

    @Override
    public ObjectId createBenchmark(ObjectId groupId, String benchmarkName, String bt){
        Document benchmark = new Document();
        benchmark.append("_id", new ObjectId())
                .append("group_id", groupId)
                .append("name", benchmarkName)
                .append("type", bt)
                .append("creation_timestamp", new Date(System.currentTimeMillis()))
                .append("is_active", false);

        MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> benchmarks = db.getCollection(COLLECTION_BENCHMARKS);
        InsertOneResult result = benchmarks.insertOne(benchmark);
        if (Objects.requireNonNull(result.getInsertedId()).isObjectId()){
            return result.getInsertedId().asObjectId().getValue();
        }
        return null;
    }

    @Override
    public Document markBenchmarkActive(ObjectId benchmarkId) {

        MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> benchmarks  = db.getCollection(COLLECTION_BENCHMARKS);
        Bson update = Updates.combine(Updates.set("is_active", true), Updates.set("activation_timestamp", new Date(System.currentTimeMillis())));
        UpdateResult result = benchmarks.updateOne(
                Filters.eq("_id", benchmarkId),
                update);

        if (result.wasAcknowledged()){
            return getBenchmark(benchmarkId);
        }
        return null;
    }

    public Document markBenchmarkFinished(ObjectId benchmarkId, Date finishTime){
        MongoDatabase db = client.getDatabase(database);
        MongoCollection<Document> benchmarks  = db.getCollection(COLLECTION_BENCHMARKS);
        Bson update = Updates.combine(Updates.set("is_active", false), Updates.set("finished_timestamp", finishTime));
        UpdateResult result = benchmarks.updateOne(
                Filters.eq("_id", benchmarkId),
                update);

        if (result.wasAcknowledged()){
            return benchmarks.find(Filters.eq("_id", benchmarkId)).first();
        }
        return null;
    }

    @Override
    public Document getBenchmark (ObjectId benchmarkId){
        MongoCollection<Document> benchmarks = client.getDatabase(database).getCollection(COLLECTION_BENCHMARKS);
        return benchmarks.find(Filters.eq("_id", benchmarkId)).first();
    }

    @Override
    public Document getActiveBenchmarkByGroupId(ObjectId groupId){
        MongoCollection<Document> benchmarks = client.getDatabase(database).getCollection(COLLECTION_BENCHMARKS);

        return benchmarks.find(Filters.and(Filters.eq("group_id", groupId), Filters.eq("is_active", true))).sort(Sorts.descending("creation_timestamp")).first();
    }

    @Override
    public void insertLatency(ObjectId groupId, Integer query, Long latency) {

        Document doc = new Document();
        doc.append("timestamp", new Date(System.currentTimeMillis()))
                .append("metadata", new Document("group_id", groupId).append("query", query))
                .append("latency", latency);

        MongoCollection<Document> latencies = client.getDatabase(database).getCollection(COLLECTION_LATENCY);
        InsertOneResult result = latencies.insertOne(doc);
        if (!(Objects.requireNonNull(result.getInsertedId()).isObjectId())){
            //handle failure
        }
    }

    public List<Document> getLatencyAnalysis(ObjectId groupId, Integer query, Date startTime, Date endTime){
        MongoCollection<Document> latencies = client.getDatabase(database).getCollection(COLLECTION_LATENCY);

        Bson match = Aggregates.match(Filters.and(Filters.gte("timestamp", startTime),
                Filters.lte("timestamp", endTime)));
//        Document id = new Document("")
        BsonField percentile = Accumulators.percentile("percentile", "$latency", Arrays.asList(0.5, 0.9), QuantileMethod.approximate());
//        Bson group = Aggregates.group(new Document("_id", new Document("group_id", "$metadata.group_id").append("query", "$metadata.query")), percentile);

        Bson group = Aggregates.group(Projections.fields(Filters.eq("group_id", "$metadata.group_id"),
                Filters.eq("query", "$metadata.query")), percentile);
        return latencies.aggregate(List.of(match, group)).into(new ArrayList<Document>());

    }


    @Override
    public void insertBenchmarkResult(ObjectId benchmarkId, Bson results) {

        MongoCollection<Document> benchmarks = client.getDatabase(database).getCollection(COLLECTION_BENCHMARKS);
        UpdateResult result = benchmarks.updateOne(Filters.eq("_id", benchmarkId), results);

    }

    @Override
    public void closeDB() {
        client.close();
    }


    @Override
    public void close() throws IOException {
        client.close();
    }
}
