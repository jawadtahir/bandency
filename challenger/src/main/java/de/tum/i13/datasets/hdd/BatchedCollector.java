package de.tum.i13.datasets.hdd;

import java.time.Instant;
import java.util.ArrayList;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.DriveState;

public class BatchedCollector {
    private final ArrayList<Batch> batches;
    private int maxBatchSize;
    private int currentBatchSize;
    private Batch.Builder bb;
    private int batchCount;
    
    public BatchedCollector(int maxBatchSize) {
        this.batches = new ArrayList<>();
        this.maxBatchSize = maxBatchSize;
        this.currentBatchSize = 0;
        this.batchCount = 0;
    }

    private void ensureBatch() {
        if(bb == null) {
            bb = Batch.newBuilder();
            bb.setSeqId(this.batchCount);
        }
    }

    public void collectState(DriveState state) {
        if (currentBatchSize >= maxBatchSize) {
            bb.setLast(false);
            batches.add(bb.build());
            bb = null;
            ++this.batchCount;
            currentBatchSize = 0;
        }
        ensureBatch();
        bb.addStates(state);
        ++currentBatchSize;
    }

    public void close() {
        if(bb != null) {
            bb.setLast(true);
            batches.add(bb.build());
            bb = null;
            currentBatchSize = 0;
        }
    }

    public int batchCount() {
        return this.batches.size();
    }
    
    public HddBatchIterator newIterator(Instant stopTime) {
        return new HddBatchIterator(batches, stopTime);
    }
}
