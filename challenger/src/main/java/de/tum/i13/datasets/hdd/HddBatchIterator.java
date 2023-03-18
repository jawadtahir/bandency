package de.tum.i13.datasets.hdd;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

import de.tum.i13.bandency.Batch;
import de.tum.i13.datasets.cache.CloseableSource;

public class HddBatchIterator implements CloseableSource<Batch> {
    private ArrayList<Batch> batches;
    private int pointer;
    private Instant stopTime;

    public HddBatchIterator(ArrayList<Batch> batches, Instant stopTime) {
        this.batches = batches;
        this.pointer = 0;
        this.stopTime = stopTime;
    }

    @Override
    public boolean hasMoreElements() {
        // Ignoring that temporary
        //if (Instant.now().isAfter(stopTime)) {
        //    return false;
        //} 
        //else {
        return this.batches.size() > this.pointer;
        //}
    }

    @Override
    public Batch nextElement() {
        var b = this.batches.get(this.pointer);
        ++this.pointer;
        return b;
    }

    @Override
    public void close() throws IOException {
    }
}
