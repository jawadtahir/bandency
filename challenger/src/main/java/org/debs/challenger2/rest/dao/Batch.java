package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Batch {
    @JsonProperty("seq_id")
    private long seqId = 0;

    @JsonProperty("last")
    private boolean last = false;
    @JsonProperty("data")
    private byte[] data;

    public Batch(){

    }

    public Batch (long seqId, byte[] data){
        this.seqId = seqId;
        this.data = data;
    }

    public long getSeqId() {
        return seqId;
    }

    public void setSeqId(long seqId) {
        this.seqId = seqId;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
