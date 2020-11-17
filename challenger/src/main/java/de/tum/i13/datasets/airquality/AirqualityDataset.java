package de.tum.i13.datasets.airquality;

import de.tum.i13.TodoException;

import java.time.LocalDateTime;

public class AirqualityDataset {

    private final AirqualityFileAccess afa;
    private final AccessType at;

    public AirqualityDataset(AirqualityFileAccess afa, AccessType at) {
        this.afa = afa;
        this.at = at;
    }

    public AirQualityDataSource newDataSource(long batchsize) {
        switch (at) {
            case FromDisk:
                return prepareDiskReader(batchsize);
        }
        throw new TodoException("Add in memory data source");
    }

    private AirQualityDataSource prepareDiskReader(long batchsize) {
        AirqualityToBatch atb = new AirqualityToBatch(batchsize, LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2020, 4, 1, 0,0), afa);
        return atb;
    }
}
