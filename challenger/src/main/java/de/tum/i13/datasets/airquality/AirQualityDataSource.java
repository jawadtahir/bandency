package de.tum.i13.datasets.airquality;

import de.tum.i13.bandency.Batch;

import java.util.Enumeration;

public interface AirQualityDataSource extends Enumeration<Batch>, AutoCloseable {
}
