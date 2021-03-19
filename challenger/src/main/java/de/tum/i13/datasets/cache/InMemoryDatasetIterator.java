package de.tum.i13.datasets.cache;

import de.tum.i13.bandency.Batch;
import de.tum.i13.datasets.airquality.AirQualityDataSource;

import java.util.Iterator;

public class InMemoryDatasetIterator implements AirQualityDataSource {


    private final Iterator<Batch> iter;

    public InMemoryDatasetIterator(Iterator<Batch> iter) {

        this.iter = iter;
    }

    @Override
    public void close() throws Exception {
        //do nothing
    }

    @Override
    public boolean hasMoreElements() {
        return this.iter.hasNext();
    }

    @Override
    public Batch nextElement() {
        return this.iter.next();
    }
}
