package org.debs.gc2023.datasets.hdd;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

import org.debs.gc2023.datasets.cache.CloseableSource;

import org.debs.gc2023.bandency.Batch;

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
