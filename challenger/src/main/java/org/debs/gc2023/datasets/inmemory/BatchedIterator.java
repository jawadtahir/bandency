package org.debs.gc2023.datasets.inmemory;

import java.time.Instant;

import org.debs.gc2023.bandency.Batch;
import org.debs.gc2023.datasets.cache.CloseableSource;

public interface BatchedIterator {
    CloseableSource<Batch> newIterator(Instant stopTime);
}
