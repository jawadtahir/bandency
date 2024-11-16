package org.debs.challenger2.dataset;

import com.google.protobuf.InvalidProtocolBufferException;
import org.debs.challenger2.rest.dao.Batch;
import org.rocksdb.RocksDBException;

public interface IDataStore {

    void AddBatch(int batchCount, Batch build) throws RocksDBException;

    int BatchCount() throws RocksDBException, InterruptedException;

    Batch GetBatch(int pointer) throws RocksDBException, InvalidProtocolBufferException;

    void SetBatchCount(int batchCount) throws RocksDBException;
}
