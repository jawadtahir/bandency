package org.debs.challenger2.dataset;


import org.debs.challenger2.rest.dao.Batch;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDataStore implements IDataStore {

    private ConcurrentHashMap<Long, Batch> batches;

    public InMemoryDataStore(){
        this.batches = new ConcurrentHashMap<>();
    }

    @Override
    public void addBatch(long batchCount, Batch batch) {
        batches.put(batchCount, batch);
    }

    @Override
    public int batchCount() {
        return batches.size();
    }

    @Override
    public Batch getBatch(long pointer) {
        return batches.getOrDefault(pointer, null);
    }
}
