package de.tum.i13;

import de.tum.i13.challenger.BenchmarkType;
import de.tum.i13.datasets.location.LocationDataset;
import de.tum.i13.datasets.location.PrepareLocationDataset;

import java.io.IOException;
import java.nio.file.Path;

public class playground {

    public static void main(String args[]) throws IOException {
        PrepareLocationDataset pld = new PrepareLocationDataset(Path.of("/home/chris/data/challenge"));
        LocationDataset locationDataset = pld.loadData(BenchmarkType.Test);
    }
}
