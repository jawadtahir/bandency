package org.debs.challenger2.rest.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Batch {
    @JsonProperty("seq_id")
    private long seqId;

    @JsonProperty("last")
    private boolean last;
    @JsonProperty("images")
    private List<Image> images;

    public Batch(){
        this.images = new ArrayList<>();
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

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
