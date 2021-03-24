package de.tum.i13.datasets.cache;

import de.tum.i13.bandency.Batch;
import de.tum.i13.datasets.airquality.AirQualityDataSource;
import org.tinylog.Logger;

import java.time.Instant;
import java.util.Iterator;

public class InMemoryDatasetIterator implements AirQualityDataSource {


    private final Iterator<Batch> iter;
    private final Instant stopTime;

    public InMemoryDatasetIterator(Iterator<Batch> iter, Instant stopTime) {

        this.iter = iter;
        this.stopTime = stopTime;
    }

    @Override
    public void close() throws Exception {
        //do nothing
    }

    @Override
    public boolean hasMoreElements() {
        boolean timeOut = Instant.now().isBefore(this.stopTime);
        if(!timeOut) {
            Logger.info("Timeout of datasource reached");
        }
        return timeOut && this.iter.hasNext();
    }

    @Override
    public Batch nextElement() {

        return this.iter.next();
    }
}
