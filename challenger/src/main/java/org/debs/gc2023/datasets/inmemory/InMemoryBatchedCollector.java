package org.debs.gc2023.datasets.inmemory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collector;

import org.debs.gc2023.datasets.cache.CloseableSource;
import org.tinylog.Logger;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.bandency.DriveState;

public class InMemoryBatchedCollector implements BatchedIterator {
    private final ArrayList<Batch> batches;
    private int maxBatchSize;
    private int currentBatchSize;
    private Batch.Builder bb;
    private int batchCount;
    private int maxBatches;
    private Random random;
    
    public InMemoryBatchedCollector(int maxBatchSize, int maxBatches) {
        this.maxBatches = maxBatches;
        this.batches = new ArrayList<>();
        this.maxBatchSize = maxBatchSize;
        this.currentBatchSize = 0;
        this.batchCount = 0;

        this.random = new Random(42);
    }

    private void ensureBatch() {
        if(bb == null) {
            bb = Batch.newBuilder();
            bb.setSeqId(this.batchCount);
        }
    }

    // Returns true if we can continue collecting, false if we should stop
    public boolean collectState(DriveState.Builder state, HashSet<String> models) {
        if (currentBatchSize >= maxBatchSize) {
            bb.setLast(false);

            var modelArr = new ArrayList<String>(models);
            Collections.shuffle(modelArr, this.random);

            if(this.batchCount % 100 == 0) {
                Logger.info("Collected batches: " + this.batchCount);
            }

            for(int i = 0; i < Math.min(5, modelArr.size()); ++i) {
                bb.addModels(modelArr.get(i));
            }

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
    
    public CloseableSource<Batch> newIterator(Instant stopTime) {
        return new InMemoryHddBatchIterator(batches, stopTime);
    }
}
