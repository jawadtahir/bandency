package org.debs.challenger2.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.Counter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.bson.types.ObjectId;
import org.debs.challenger2.benchmark.BenchmarkState;
import org.debs.challenger2.benchmark.BenchmarkType;
import org.debs.challenger2.benchmark.ToVerify;
import org.debs.challenger2.dataset.IDataSelector;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.dataset.TestDataSelector;
import org.debs.challenger2.db.IQueries;
import org.debs.challenger2.rest.dao.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Path("/benchmark")
public class RestServer {

    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private final IQueries q;
    private final int durationEvaluationMinutes;
    private final Random random;
    private IDataStore store;
    final private ConcurrentHashMap<String, BenchmarkState> benchmarks;

    private final ObjectMapper objectMapper = new ObjectMapper();
    static final Counter errorCounter = Counter.build()
            .name("errorsREST")
            .help("unforseen errors")
            .register();

    static final Counter createNewBenchmarkCounter = Counter.build()
            .name("createNewBenchmarkREST")
            .help("calls to createNewBenchmark methods")
            .register();

    static boolean isValid (String bmType){
        return bmType.equalsIgnoreCase("test")||bmType.equalsIgnoreCase("verification")||bmType.equalsIgnoreCase("evaluation");
    }

    public RestServer(){
        this(null, null, null, 10);
    }
    public RestServer(IDataStore store, ArrayBlockingQueue<ToVerify> dbInserter, IQueries q, int durationEvaluationMinute){
        this.store = store;
        this.dbInserter = dbInserter;
        this.q = q;
        this.durationEvaluationMinutes = durationEvaluationMinute;
        this.benchmarks = new ConcurrentHashMap<>();
        this.random = new Random(System.nanoTime());

    }

    //TODO: Change it to request params from query params
    @POST
    @Path("/create-benchmark")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBenchmark(String request ){
        BatchRequest request1 = null;
        try {
            request1 = objectMapper.readValue(request, BatchRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // Validate
        if (request1.getToken() == null){
            return Response.status(Status.BAD_REQUEST).entity("token is missing in query params.").build();
        }
        if (request1.getBenchmarkType() == null){
            return Response.status(Status.BAD_REQUEST).entity("benchmarkType is missing in query params.").build();
        }
        if (request1.getBenchmarkName() == null){
            return Response.status(Status.BAD_REQUEST).entity("benchmarkName is missing in query params.").build();
        }
        if (!isValid(request1.getBenchmarkType())){
            return Response.status(Status.PRECONDITION_FAILED).entity("Unsupported benchmarkType.").build();
        }

        ObjectId groupId = q.getGroupIdFromToken(request1.getToken());

        if (groupId == null){
            return Response.status(Status.FORBIDDEN).entity("Invalid token.").build();
        } else {
            // Configure benchmark
            BenchmarkType bt = getBenchmarkType(request1.getBenchmarkType());
            ObjectId benchmarkId = q.insertBenchmarkStarted(groupId , request1.getBenchmarkName(), 1000, bt.toString());

            BenchmarkState bms = new BenchmarkState(this.dbInserter);
            bms.setToken(request1.getToken());
            bms.setBenchmarkId(benchmarkId);
            bms.setBenchmarkType(bt);
            bms.setBenchmarkName(request1.getBenchmarkName());

            Instant stopTime = Instant.now().plus(durationEvaluationMinutes, ChronoUnit.MINUTES);

            if(bt == BenchmarkType.Evaluation) {
                // TODO: Change it to eval data selector
                IDataSelector dataSelector = new TestDataSelector(store, 3);
                bms.setDataSelector(dataSelector);
            } else {
                // for the time being, there is no difference in the dataset
                IDataSelector dataSelector = new TestDataSelector(store, 3);
                bms.setDataSelector(dataSelector);
            }

//        Logger.info("Ready for benchmark: " + bms.toString());

            this.benchmarks.put(benchmarkId.toString(), bms);
            createNewBenchmarkCounter.inc();
            Benchmark created = new Benchmark(benchmarkId.toString());
            try {
                return Response.status(Status.OK)
                        .entity(objectMapper.writeValueAsString(created))
                        .build();
            } catch (JsonProcessingException e) {
                return  Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Error parsing benchmark").build();
            }

        }

    }
    @GET
    @Path("/start-benchmark/{benchmark_id}/")
    public Response startBenchmark(@PathParam("benchmark_id") String benchmarkId){
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        Long startTime = System.nanoTime();
        benchmarks.computeIfPresent(benchmarkId, (key, bmState) -> {
            bmState.setIsStarted(true);
            bmState.startBenchmark(startTime);
            return bmState;
        });
        BenchmarkStart benchmarkStart = new BenchmarkStart(benchmarkId, startTime);
        try {
            return Response.status(Status.OK).entity(objectMapper.writeValueAsString(benchmarkStart)).build();
        } catch (JsonProcessingException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to create benchmark_start").build();
        }
    }

    //TODO: Send data in binary
    @GET
    @Path("/next-batch/{benchmark_id}/")
    public Response getNextBatch(@PathParam("benchmark_id") String benchmarkId){
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        if (!benchmarks.get(benchmarkId).getIsStarted()){
            return Response.status(Status.PRECONDITION_FAILED).entity("Benchmark is deactivated").build();
        }
        AtomicReference<Batch> batchRef = new AtomicReference<>();
        benchmarks.computeIfPresent(benchmarkId, (key, bmState) -> {
            batchRef.set(bmState.getNextBatch(benchmarkId));
            return bmState;
        });

        Batch batch = batchRef.getAcquire();
        if (batch == null){
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Please contact DEBS organizers").build();
        }

        try {
            return Response.status(Status.OK).entity(objectMapper.writeValueAsString(batch)).build();
        } catch (JsonProcessingException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error parsing batch.").build();
        }
    }

    //TODO: Receive data in binary
    @POST
    @Path("/result/{benchmark_id}/{batch_seq_id}/{query}/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response result(@PathParam("benchmark_id") String benchmarkId,
                           @PathParam("batch_seq_id") Long batchSeqId,
                           @PathParam("query") Integer query,
                           String jsonBody){
        long nanoTime = System.nanoTime();
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        if (!benchmarks.get(benchmarkId).getIsStarted()){
            return Response.status(Status.PRECONDITION_FAILED).entity("Benchmark is deactivated.").build();
        }

        benchmarks.computeIfPresent(benchmarkId, (key, bmState)->{
            bmState.markResult(batchSeqId, nanoTime, query);
            return bmState;
        });
        ResultResponse response = new ResultResponse(benchmarkId, batchSeqId, query, nanoTime);
        try {
            return Response.status(Status.OK).entity(objectMapper.writeValueAsString(response)).build();
        } catch (JsonProcessingException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to create result response.").build();
        }
    }

    @GET
    @Path("/end-benchmark/{benchmark_id}")
    public Response endBenchmark(@PathParam("benchmark_id") String benchmarkId){
        long nanoTime = System.nanoTime();
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        if (!benchmarks.get(benchmarkId).getIsStarted()){
            return Response.status(Status.PRECONDITION_FAILED).entity("Benchmark is deactivated.").build();
        }
        AtomicBoolean found = new AtomicBoolean(false);
        benchmarks.computeIfPresent(benchmarkId, (k, b) -> {
            b.endBenchmark(benchmarkId, nanoTime);
            found.set(true);

            //Logger.info("Ended benchmark: " + b.toString());
            return b;
        });

        if(found.get()) {
            benchmarks.remove(benchmarkId);
        }

//        endBenchmarkCounter.inc();

        return Response.status(Response.Status.OK).build();
    }

    private static BenchmarkType getBenchmarkType(String benchmarkType) {
        BenchmarkType bt = BenchmarkType.Test;
        int batchSize = 1_000;

        if(benchmarkType.equalsIgnoreCase("test")) {
            bt = BenchmarkType.Test;
            batchSize = 1_000;
        } else if (benchmarkType.equalsIgnoreCase("verification")) {
            bt = BenchmarkType.Verification;
        } else if (benchmarkType.equalsIgnoreCase("evaluation")){
            bt = BenchmarkType.Evaluation;
            batchSize = 1_000;
        }
        return bt;
    }
}
