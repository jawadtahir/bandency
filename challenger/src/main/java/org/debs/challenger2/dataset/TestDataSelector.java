package org.debs.challenger2.dataset;

import org.debs.challenger2.rest.dao.Batch;

import java.util.Random;

public class TestDataSelector implements IDataSelector{
    private int pointer;
    private int start;
    private int end;
    private final IDataStore store;
    private final int numSlices;
    public TestDataSelector(IDataStore store, int numSlices){
        this.store = store;
        this.numSlices = numSlices;
        configureSlice();
    }
    @Override
    public boolean hasMoreElements() {
        return pointer < end;
    }

    @Override
    public Batch nextElement() {
        Batch batch = store.getBatch(pointer);
        pointer++;
        return batch;
    }

    private void configureSlice(){
        int numBatches = store.batchCount();
        int batchesPerSlice = Math.floorDiv(numBatches, numSlices);
        Random rand = new Random();
        int sliceNumber = rand.nextInt(numSlices);
        pointer = (sliceNumber * batchesPerSlice) + 1;
        end = pointer + batchesPerSlice;
    }
}
