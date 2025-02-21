package org.debs.challenger2.pending;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.debs.challenger2.db.IQueries;

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
        Date bStartTime = benchmark.getDate("activation_timestamp");
        Date bFinishTime = benchmark.getDate("finished_timestamp");

        List<Document> analysis = queries.getLatencyAnalysis(benchmark);

        queries.insertBenchmarkResult(benchmarkId, analysis, bStartTime, bFinishTime);

    }
}
