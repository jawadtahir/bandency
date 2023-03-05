package de.tum.i13.datasets.hdd;

import java.time.Instant;
import java.util.ArrayList;

import de.tum.i13.bandency.Batch;

public class BatchedEvents {

    private final ArrayList<Batch> batches;
    
    public BatchedEvents() {
        this.batches = new ArrayList<>();
    }

    public void loadData(HddLoader fel, int batchSize) {

        long cnt = 0;
        while(fel.hasMoreElements()) {
            Batch.Builder bb = Batch.newBuilder();
            for(int i = 0; i < batchSize && fel.hasMoreElements(); ++i) {
                //bb.addEvents(fel.nextElement());
            }
            bb.setSeqId(cnt);
            //bb.addAllLookupSymbols(this.sg.nextElement());

            batches.add(bb.build());

            ++cnt;
        }

        var last = this.batches.get(batches.size()-1);
        var newLast = Batch.newBuilder(last)
            .setLast(true)
            .build();
        this.batches.set(batches.size()-1, newLast);
    }

    public int batchCount() {
        return this.batches.size();
    }
    
    public HddBatchIterator newIterator(Instant stopTime) {
        return new HddBatchIterator(batches, stopTime);
    }
}
