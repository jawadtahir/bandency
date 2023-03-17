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
    private int maxBatches;
    
    public BatchedCollector(int maxBatchSize, int maxBatches) {
        this.maxBatches = maxBatches;
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

    // Returns true if we can continue collecting, false if we should stop
    public boolean collectState(DriveState.Builder state) {
        if (currentBatchSize >= maxBatchSize) {
            bb.setLast(false);
            batches.add(bb.build());
            bb = null;
            ++this.batchCount;
            currentBatchSize = 0;
            if (this.maxBatches > 0 && this.batchCount >= maxBatches) {
                return false;
            }
        }
        ensureBatch();
        bb.addStates(state);
        ++currentBatchSize;

        return true;
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
