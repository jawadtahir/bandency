package de.tum.i13.datasets.location;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface IDatasetLoader {
    public LocationDataset load() throws IOException;
}
