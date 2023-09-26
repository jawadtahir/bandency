package org.debs.gc2023.datasets;

import java.time.Instant;
import org.rocksdb.RocksDBException;

import com.google.protobuf.InvalidProtocolBufferException;

import org.debs.gc2023.bandency.Batch;

public class BatchIterator {
    private int pointer;
    private IDataStore store;
    private Instant stopTime;

    public BatchIterator(IDataStore store, Instant stopTime) {
        this.store = store;
        this.pointer = 0;
        this.stopTime = stopTime;
    }

    public boolean hasMoreElements() throws RocksDBException, InterruptedException {
        if (Instant.now().isAfter(this.stopTime)) {
            return false;
        } 
        else {
            return this.store.BatchCount() > this.pointer;
        }
    }

    public Batch nextElement() throws InvalidProtocolBufferException, RocksDBException {
        var b = this.store.GetBatch(this.pointer);
        ++this.pointer;
        return b;
    }
}
