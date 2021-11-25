package de.tum.i13.datasets.cache;

import java.time.Instant;
import java.util.ArrayList;

public class InMemoryDataset<T> {
    private final ArrayList<T> inMemoryBatches;

    public InMemoryDataset(ArrayList<T> inMemoryBatches) {

        this.inMemoryBatches = inMemoryBatches;
    }

    synchronized public CloseableSource<T> getIterator(Instant stopTime) {
        InMemoryDatasetIterator<T> imdi = new InMemoryDatasetIterator<T>(this.inMemoryBatches.iterator(), stopTime);
        return imdi;
    }

}
