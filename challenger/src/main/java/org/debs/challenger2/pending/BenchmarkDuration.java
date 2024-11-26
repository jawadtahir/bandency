package org.debs.challenger2.pending;

import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.debs.challenger2.db.IQueries;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class BenchmarkDuration implements IPendingTask {
    private final ObjectId groupId;
    private final ObjectId benchmarkId;

    private final Date endTime;




    public BenchmarkDuration(ObjectId groupId, ObjectId benchmarkId, Date endTime) {

        this.groupId = groupId;
        this.benchmarkId = benchmarkId;
        this.endTime = endTime;
    }

    public ObjectId getGroupId() {
        return groupId;
    }

    public ObjectId getBenchmarkId() {
        return benchmarkId;
    }

    public Date getEndTime() {
        return endTime;
    }


    @Override
    public void doPending(IQueries queries) {
        Document benchmark = queries.markBenchmarkFinished(benchmarkId, endTime);
        if (benchmark == null){
            return;
        }
        Date startTime = benchmark.getDate("activation_timestamp");
        List<Document> analysis = queries.getLatencyAnalysis(groupId, 1, startTime, endTime);
//        Document analysis_q2 = queries.getLatencyAnalysis(groupId, 2, startTime, endTime);
        Document latAnalysis = new Document();
        for (Document doc: analysis){
            if (((Document)doc.get("_id")).get("query", Integer.class) == 1){
                latAnalysis.append("query_1", new Document("percentile", doc.getList("percentile", Double.class)));
            } else if (((Document)doc.get("_id")).get("query", Integer.class) == 2) {
                latAnalysis.append("query_2", new Document("percentile", doc.getList("percentile", Double.class)));
            } else {
                //error handling
            }
        }

        long runtime = Duration.between(startTime.toInstant(), endTime.toInstant()).get(ChronoUnit.SECONDS);
        latAnalysis.append("runtime", runtime);

        Bson result = Updates.set("results", latAnalysis);

        queries.insertBenchmarkResult(benchmarkId, result);

    }
}
