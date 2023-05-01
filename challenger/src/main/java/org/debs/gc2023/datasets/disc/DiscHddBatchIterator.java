package org.debs.gc2023.datasets.disc;

import java.io.IOException;
import java.time.Instant;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.datasets.cache.CloseableSource;
import org.debs.gc2023.datasets.inmemory.BatchedIterator;

public class DiscHddBatchIterator implements BatchedIterator{

    @Override
    public CloseableSource<Batch> newIterator(Instant stopTime) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newIterator'");
    }
}
