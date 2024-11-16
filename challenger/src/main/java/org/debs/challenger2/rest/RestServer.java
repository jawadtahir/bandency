package org.debs.challenger2.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.Counter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.debs.challenger2.benchmark.BenchmarkState;
import org.debs.challenger2.benchmark.BenchmarkType;
import org.debs.challenger2.benchmark.ToVerify;
import org.debs.challenger2.dataset.BatchIterator;
import org.debs.challenger2.dataset.IDataStore;
import org.debs.challenger2.rest.dao.Benchmark;
import org.debs.challenger2.db.IQueries;


import jakarta.ws.rs.Path;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Path("/benchmark")
public class RestServer {

    private final ArrayBlockingQueue<ToVerify> dbInserter;
    private final IQueries q;
    private final int durationEvaluationMinutes;
    private final Random random;
    private IDataStore store;
    final private ConcurrentHashMap<ObjectId, BenchmarkState> benchmark;

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
        this.benchmark = new ConcurrentHashMap<>();
        this.random = new Random(System.nanoTime());

    }

    @GET
    @Path("/create-benchmark")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBenchmark(@QueryParam("token") String token,
                                    @QueryParam("benchmarkType") String benchmarkType,
                                    @QueryParam("benchmarkName") String benchmarkName,
                                    @QueryParam("queries") List<String> queries){
        // Validate
        if (token == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("token is missing in query params.").build();
        }
        if (benchmarkType == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("benchmarkType is missing in query params.").build();
        }
        if (benchmarkName == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("benchmarkName is missing in query params.").build();
        }
        if (queries == null || queries.isEmpty()){
            return Response.status(Response.Status.BAD_REQUEST).entity("queries is missing in query params.").build();
        }
        if (!isValid(benchmarkType)){
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("Unsupported benchmarkType.").build();
        }

        ObjectId groupId = q.getGroupIdFromToken(token);

        if (groupId == null){
            return Response.status(Response.Status.FORBIDDEN).entity("Invalid token.").build();
        } else {
            // Configure benchmark
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
            ObjectId benchmarkId = q.insertBenchmarkStarted(groupId , benchmarkName, 1000, bt.toString());

            BenchmarkState bms = new BenchmarkState(this.dbInserter);
            bms.setToken(token);
            bms.setBenchmarkId(benchmarkId);
            bms.setToken(token);
            bms.setBenchmarkType(bt);
            bms.setBenchmarkName(benchmarkName);

            bms.setQ1(queries.get(0).contains("q1"));
            bms.setQ2(queries.get(0).contains("q2"));

            Instant stopTime = Instant.now().plus(durationEvaluationMinutes, ChronoUnit.MINUTES);

            if(bt == BenchmarkType.Evaluation) {
                var bi = new BatchIterator(this.store, stopTime);
                bms.setDatasource(bi);
            } else {
                // for the time being, there is no difference in the dataset
                var bi = new BatchIterator(this.store, stopTime);
                bms.setDatasource(bi);
            }

//        Logger.info("Ready for benchmark: " + bms.toString());

            this.benchmark.put(benchmarkId, bms);
            createNewBenchmarkCounter.inc();
            Benchmark created = new Benchmark(benchmarkId.toString());
            try {
                return Response.status(Response.Status.OK)
                        .entity(objectMapper.writeValueAsString(created))
                        .build();
            } catch (JsonProcessingException e) {
                return  Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error parsing benchmark").build();
            }

        }

    }
}
