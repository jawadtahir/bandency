package org.debs.challenger2.dataset;

import com.google.protobuf.InvalidProtocolBufferException;
import org.debs.challenger2.rest.dao.Batch;
import org.rocksdb.RocksDBException;

public interface IDataStore {

    void addBatch(long batchCount, Batch batch);

    int batchCount();

    Batch getBatch(long pointer);

}
