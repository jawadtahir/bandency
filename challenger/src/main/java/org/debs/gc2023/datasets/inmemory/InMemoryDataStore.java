package org.debs.gc2023.datasets.inmemory;

import java.util.ArrayList;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.datasets.IDataStore;

public class InMemoryDataStore implements IDataStore {
    private final ArrayList<Batch> batches;

    public InMemoryDataStore() {
        this.batches = new ArrayList<>();
    }

    @Override
    public void AddBatch(int batchCount, Batch build) {
        this.batches.add(build);
    }

    @Override
    public int BatchCount() {
        return this.batches.size();
    }

    @Override
    public Batch GetBatch(int pointer) {
        return this.batches.get(pointer);
    }

    @Override
    public void SetBatchCount(int batchCount) {
        // Nothing to do in the in memory case
    }
}
