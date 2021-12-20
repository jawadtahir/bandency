package de.tum.i13.datasets.financial;

import java.time.Instant;
import java.util.ArrayList;

import de.tum.i13.bandency.Batch;

public class BatchedEvents {

    private final ArrayList<Batch> batches;
    
    public BatchedEvents() {
        this.batches = new ArrayList<>();
    }

    public void loadData(FinancialEventLoader fel, int batchSize) {

        while(fel.hasMoreElements()) {
            Batch.Builder bb = Batch.newBuilder();
            for(int i = 0; i < 1000 && fel.hasMoreElements(); ++i) {
                bb.addEvents(fel.nextElement());
            }

            batches.add(bb.build());
        }
    }
    
    public FinancialBatchIterator newIterator(Instant stopTime) {
        return new FinancialBatchIterator(batches, stopTime);
    }
}
