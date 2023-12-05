package org.debs.gc2023.datasets.inmemory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.debs.gc2023.datasets.IDataStore;
import org.rocksdb.RocksDBException;
import org.tinylog.Logger;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.bandency.DriveState;

public class BatchedCollector {
    private int maxBatchSize;
    private int currentBatchSize;
    private Batch.Builder bb;
    private int batchCount;
    private int maxBatches;
    private Random random;
    private IDataStore store;
    private long nextDate;
    
    public BatchedCollector(IDataStore store, int maxBatchSize, int maxBatches) {
        this.store = store;
        this.maxBatches = maxBatches;
        this.maxBatchSize = maxBatchSize;
        this.currentBatchSize = 0;
        this.batchCount = 0;

        this.random = new Random(42);
        this.nextDate = -1;
    }

    private void ensureBatch() {
        if(bb == null) {
            bb = Batch.newBuilder();
            bb.setSeqId(this.batchCount);
            bb.setDayEnd(false); // default false
        }
    }

    // Returns true if we can continue collecting, false if we should stop
    public boolean collectState(DriveState.Builder state, HashSet<String> models) throws RocksDBException {
        if (currentBatchSize >= maxBatchSize) {
            bb.setLast(false);

            var modelArr = new ArrayList<String>(models);
            Collections.shuffle(modelArr, this.random);

            if(this.batchCount % 100 == 0) {
                Logger.info("Collected batches: " + this.batchCount);
            }

            /*
            for(int i = 0; i < Math.min(5, modelArr.size()); ++i) {
                bb.addModels(modelArr.get(i));
            }
            */

            // TODO: populate vault_ids, how to create?
            bb.addVaultIds(1);


            // TODO: populate cluster_ids
            bb.addClusterIds(1);

            store.AddBatch(this.batchCount, bb.build());
            bb = null;
            ++this.batchCount;
            currentBatchSize = 0;

            if (this.maxBatches > 0 && this.batchCount >= maxBatches) {
                store.SetBatchCount(this.batchCount);
                return false;
            }
        }
        // ensure the batch is initialized
        ensureBatch();

        // ensure the next day treshold is set to mark batches with end of day
        if(nextDate == -1) {
            nextDate = state.getDate().getSeconds() + 24 * 60 * 60;
        }

        // usual operation
        bb.addStates(state);
        ++currentBatchSize;

        // set dayend to true if we are at the end of the day
        if (nextDate > state.getDate().getSeconds()) {
            bb.setDayEnd(true);
            nextDate = state.getDate().getSeconds() + 24 * 60 * 60;
        }

        return true;
    }

    public void close() throws RocksDBException {
        if(bb != null) {
            bb.setLast(true);
            this.store.AddBatch(this.batchCount, bb.build());
            bb = null;
            currentBatchSize = 0;
        }
    }

    public int batchCount() throws RocksDBException, InterruptedException {
        return this.store.BatchCount();
    }    
}
