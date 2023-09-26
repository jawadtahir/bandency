package org.debs.gc2023.datasets.disc;

import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.datasets.IDataStore;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.google.protobuf.InvalidProtocolBufferException;

public class RocksdbStore implements IDataStore {

    private RocksDB db;
    private Semaphore cntSemaphore;
    private Integer cnt;
    private byte[] cntKey = ByteBuffer.allocate(6).putChar('c').putChar('n').putChar('t').array();

    public RocksdbStore(RocksDB db) {
        this.db = db;
        this.cntSemaphore = new Semaphore(1, false);
        cnt = null;
    }

    @Override
    public void AddBatch(int batchCount, Batch build) throws RocksDBException {

        var key = integerToKey(batchCount);
        var value = build.toByteArray();
        this.db.put(key, value);
    }

    @Override
    public int BatchCount() throws RocksDBException, InterruptedException {
        if (cnt != null){
            return cnt;
        }
        else {
            cntSemaphore.acquire(); // well, ... still love async SemaphoreSlim in .net, or borrow a Arc, but what have you here ...
            if (cnt != null){ 
                cntSemaphore.release();
                return cnt;
            }
            else 
            {
                var byteBuf = this.db.get(cntKey);
                var asInt = ByteBuffer.wrap(byteBuf).getInt();
                cnt = asInt;
                return cnt;
            }
        }
    }

    @Override
    public Batch GetBatch(int pointer) throws RocksDBException, InvalidProtocolBufferException {
        var key = integerToKey(pointer);
        var valueAsBytes = this.db.get(key);
        return Batch.parseFrom(valueAsBytes);
    }

    

    @Override
    public void SetBatchCount(int batchCount) throws RocksDBException {
        this.db.put(cntKey,ByteBuffer.allocate(4).putInt(0, batchCount).array());
    }

    private byte[] integerToKey(int num) {
        var bb = ByteBuffer.allocate(6);
        bb.putChar(0, 'a');
        bb.putInt(2, num);
        return bb.array();
    }
}
