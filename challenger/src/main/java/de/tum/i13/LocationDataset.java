package de.tum.i13;

import de.tum.i13.bandency.Locations;

public class LocationDataset {
    private Locations allLocations;

    public LocationDataset(Locations allLocations) {

        this.allLocations = allLocations;
    }

    public Locations getAllLocations() {
        return allLocations;
    }
}
