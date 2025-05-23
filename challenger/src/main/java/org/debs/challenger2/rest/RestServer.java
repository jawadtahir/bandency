package org.debs.challenger2.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.Counter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.debs.challenger2.benchmark.BenchmarkState;
import org.debs.challenger2.benchmark.BenchmarkType;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.db.IQueries;
import org.debs.challenger2.pending.IPendingTask;
import org.debs.challenger2.rest.dao.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Path("/api")
public class RestServer {

    private static final Logger LOGGER = LogManager.getLogger(RestServer.class);

    private final ArrayBlockingQueue<IPendingTask> pending;
    private final IQueries q;
    private final IDataStore testStore;

    private final IDataStore evalStore;
    final private ConcurrentHashMap<String, BenchmarkState> benchmarks;

    private final ObjectMapper objectMapper = new ObjectMapper();
    static final Counter errorCounter = Counter.build()
            .name("errors")
            .help("unforseen errors")
            .register();

    static final Counter createNewBenchmarkCounter = Counter.build()
            .name("createNewBenchmark")
            .help("calls to createNewBenchmark methods")
            .register();

    static boolean isValid (String bmType){
        return bmType.equalsIgnoreCase("test")||bmType.equalsIgnoreCase("verification")||bmType.equalsIgnoreCase("evaluation");
    }

    public RestServer(){
        this(null, null, null, null);
    }
    public RestServer(IDataStore testStore, IDataStore evalStore, ArrayBlockingQueue<IPendingTask> pending, IQueries q){
        this.testStore = Objects.requireNonNull(testStore);
        this.evalStore = evalStore;
        this.pending = pending;
        this.q = q;
        this.benchmarks = new ConcurrentHashMap<>();

    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBenchmark(String request ){
        LOGGER.info("/create");
        BatchRequest request1 = null;
        try {
            request1 = objectMapper.readValue(request, BatchRequest.class);
        } catch (JsonProcessingException e) {
            return Response.status(Status.BAD_REQUEST).entity("JSON should contain apitoken, name, and a test flag").build();
        }
        // Validate
        if (request1.getToken() == null){
            return Response.status(Status.BAD_REQUEST).entity("apitoken is missing in query params.").build();
        }
        if (request1.getBenchmarkName() == null){
            return Response.status(Status.BAD_REQUEST).entity("name is missing in query params.").build();
        }

        ObjectId groupId = q.getGroupIdFromToken(request1.getToken());

        if (groupId == null){
            return Response.status(Status.FORBIDDEN).entity("Invalid token.").build();
        }

        Document activeBenchmark = q.getActiveBenchmarkByGroupId(groupId);

        if (activeBenchmark != null){
            return Response.status(Status.PRECONDITION_FAILED).entity(String.format("Benchmark %s is active. Please end the benchmark.", activeBenchmark.get("_id").toString())).build();
        }

        // Configure benchmark
        IDataStore dataStore = (!request1.isTest() && evalStore != null) ? evalStore : testStore;
        BenchmarkType bt = (request1.isTest()) ? BenchmarkType.Test : BenchmarkType.Evaluation;

        ObjectId benchmarkId = q.createBenchmark(groupId , request1.getBenchmarkName(), bt.toString());

        if (benchmarkId == null){
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Unable to create benchmark object").build();
        }
        BenchmarkState bms = new BenchmarkState(this.pending);
        bms.setToken(request1.getToken());
        bms.setGroupId(groupId);
        bms.setBenchmarkId(benchmarkId);
        bms.setBenchmarkName(request1.getBenchmarkName());
        bms.setDataStore(dataStore);


        this.benchmarks.put(benchmarkId.toString(), bms);
        createNewBenchmarkCounter.inc();
        Benchmark created = new Benchmark(benchmarkId.toString());
        try {
            return Response.status(Status.OK)
                    .entity(objectMapper.writeValueAsString(benchmarkId.toString()))
                    .build();
        } catch (JsonProcessingException e) {
            return  Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Error parsing benchmark").build();
        }

    }
    @POST
    @Path("/start/{benchmark_id}/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startBenchmark(@PathParam("benchmark_id") String benchmarkId){
        LOGGER.info(String.format("/start/%s", benchmarkId));
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        Long startTime = System.nanoTime();
        benchmarks.computeIfPresent(benchmarkId, (key, bmState) -> {
            bmState.setStarted(true);
            bmState.setStartNanoTime(startTime);
            return bmState;
        });
        BenchmarkStart benchmarkStart = new BenchmarkStart(benchmarkId, startTime);
        try {

            q.markBenchmarkActive(new ObjectId(benchmarkId));
            return Response.status(Status.OK).entity(objectMapper.writeValueAsString(benchmarkStart)).build();
        } catch (JsonProcessingException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to create benchmark_start").build();
        }
    }

    //TODO: Send data in binary
    @GET
    @Path("/next_batch/{benchmark_id}/")
    public Response getNextBatch(@PathParam("benchmark_id") String benchmarkId){
        LOGGER.debug(String.format("/next_batch/%s", benchmarkId));
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        if (!benchmarks.get(benchmarkId).isStarted()){
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

        if (!batch.isLast()) {
            return Response.status(Status.OK).entity(batch.getData()).build();
        } else {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    //TODO: Receive data in binary
    @POST
    @Path("/result/{query}/{benchmark_id}/{batch_seq_id}/")
    @Consumes(MediaType.WILDCARD)
    public Response result(@PathParam("benchmark_id") String benchmarkId,
                           @PathParam("batch_seq_id") Long batchSeqId,
                           @PathParam("query") Integer query,
                           byte[] jsonBody){
        LOGGER.debug(String.format("/result/0/%s/%s", benchmarkId, batchSeqId));
        long nanoTime = System.nanoTime();
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid bench_id").build();
        }
        if (!benchmarks.get(benchmarkId).isStarted()){
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

    @POST
    @Path("/end/{benchmark_id}")
    public Response endBenchmark(@PathParam("benchmark_id") String benchmarkId){
        LOGGER.info(String.format("/end/%s", benchmarkId));
        long nanoTime = System.nanoTime();
        if (!benchmarks.containsKey(benchmarkId)){
            return Response.status(Status.NOT_FOUND).entity("Invalid benchmark_id").build();
        }
        if (!benchmarks.get(benchmarkId).isStarted()){
            return Response.status(Status.PRECONDITION_FAILED).entity("Benchmark is deactivated.").build();
        }
        AtomicBoolean found = new AtomicBoolean(false);
        benchmarks.computeIfPresent(benchmarkId, (k, b) -> {
            b.endBenchmark(Date.from(Instant.now()));
            found.set(true);

            return b;
        });

        if(found.get()) {
            benchmarks.remove(benchmarkId);
        }


        return Response.status(Response.Status.OK).build();
    }

}
