package de.tum.i13.datasets.financial;

import java.time.Instant;
import java.util.ArrayList;

import de.tum.i13.bandency.Batch;

public class BatchedEvents {

    private final ArrayList<Batch> batches;
    private SymbolsGenerator sg;
    
    public BatchedEvents(SymbolsGenerator sg) {
        this.sg = sg;
        this.batches = new ArrayList<>();
    }

    public void loadData(FinancialEventLoader fel, int batchSize) {

        long cnt = 0;
        while(fel.hasMoreElements()) {
            Batch.Builder bb = Batch.newBuilder();
            for(int i = 0; i < 1000 && fel.hasMoreElements(); ++i) {
                bb.addEvents(fel.nextElement());
            }
            bb.setSeqId(cnt);

            bb.addLookupSymbols("todo");

            batches.add(bb.build());

            ++cnt;
        }

        var last = this.batches.get(batches.size()-1);
        var newLast = Batch.newBuilder(last)
            .addAllLookupSymbols(this.sg.nextElement())
            .setLast(true)
            .build();
        this.batches.set(batches.size()-1, newLast);
    }
    
    public FinancialBatchIterator newIterator(Instant stopTime) {
        return new FinancialBatchIterator(batches, stopTime);
    }
}
