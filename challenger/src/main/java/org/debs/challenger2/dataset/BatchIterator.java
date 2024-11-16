package org.debs.challenger2.dataset;

import com.google.protobuf.InvalidProtocolBufferException;
import org.debs.challenger2.rest.dao.Batch;
import org.rocksdb.RocksDBException;

import java.time.Instant;

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
        } else {
            return this.store.BatchCount() > this.pointer;
        }
    }

    public Batch nextElement() throws InvalidProtocolBufferException, RocksDBException {
        var b = this.store.GetBatch(this.pointer);
        ++this.pointer;
        return b;
    }
}
