package de.tum.i13.datasets.airquality;

import de.tum.i13.TodoException;
import de.tum.i13.challenger.BenchmarkType;

import java.time.LocalDateTime;

public class AirqualityDataset {

    private final AirqualityFileAccess afa;
    private final AccessType at;

    public AirqualityDataset(AirqualityFileAccess afa, AccessType at) {
        this.afa = afa;
        this.at = at;
    }

    public AirQualityDataSource newDataSource(BenchmarkType benchmarkType, long batchsize) {
        switch (at) {
            case FromDisk:
                return prepareDiskReader(benchmarkType, batchsize);
        }
        throw new TodoException("Add in memory data source");
    }

    private AirQualityDataSource prepareDiskReader(BenchmarkType benchmarkType, long batchsize) {

        if(benchmarkType == BenchmarkType.Test) {
            AirqualityToBatch atb = new AirqualityToBatch(batchsize, LocalDateTime.of(2020, 3, 1, 0, 0), LocalDateTime.of(2020, 8, 1, 0,0), afa);
            return atb;
        } else if(benchmarkType == BenchmarkType.Verification) {
            AirqualityToBatch atb = new TestAirqualityToBatch();
            return atb;
        } else if(benchmarkType == BenchmarkType.Evaluation) {
            AirqualityToBatch atb = new AirqualityToBatch(batchsize, LocalDateTime.of(2020, 8, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 0,0), afa);
            return atb;
        }

        throw new TodoException("Not prepared for BenchmarkType: " + benchmarkType);
    }
}
