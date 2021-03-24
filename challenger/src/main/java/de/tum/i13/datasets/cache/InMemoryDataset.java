package de.tum.i13.datasets.cache;

import de.tum.i13.bandency.Batch;
import de.tum.i13.datasets.airquality.AirQualityDataSource;

import java.time.Instant;
import java.util.ArrayList;

public class InMemoryDataset {
    private final ArrayList<Batch> inMemoryBatches;

    public InMemoryDataset(ArrayList<Batch> inMemoryBatches) {

        this.inMemoryBatches = inMemoryBatches;
    }

    synchronized public AirQualityDataSource getIterator(Instant stopTime) {
        InMemoryDatasetIterator imdi = new InMemoryDatasetIterator(this.inMemoryBatches.iterator(), stopTime);
        return imdi;
    }

}
