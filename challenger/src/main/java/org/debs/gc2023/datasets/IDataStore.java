package org.debs.gc2023.datasets;

import org.debs.gc2023.bandency.Batch;
import org.rocksdb.RocksDBException;

import com.google.protobuf.InvalidProtocolBufferException;

public interface IDataStore {

    void AddBatch(int batchCount, Batch build) throws RocksDBException;

    int BatchCount() throws RocksDBException, InterruptedException;

    Batch GetBatch(int pointer) throws RocksDBException, InvalidProtocolBufferException;

    void SetBatchCount(int batchCount) throws RocksDBException;    
}
