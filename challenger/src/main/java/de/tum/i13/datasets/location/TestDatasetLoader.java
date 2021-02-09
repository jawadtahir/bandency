package de.tum.i13.datasets.location;

import de.tum.i13.bandency.Location;
import de.tum.i13.bandency.Locations;
import de.tum.i13.bandency.Point;
import de.tum.i13.bandency.Polygon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestDatasetLoader implements IDatasetLoader {

    public TestDatasetLoader(){
        super();
    }

    protected List<Polygon> getPolygons(float startIndex){
        List<Polygon> polygons = new ArrayList<Polygon>();

        Point p1 = Point.newBuilder().setLatitude(startIndex+0).setLongitude(0).build();
        Point p2 = Point.newBuilder().setLatitude(startIndex+0).setLongitude(1).build();
        Point p3 = Point.newBuilder().setLatitude(startIndex+1).setLongitude(1).build();
        Point p4 = Point.newBuilder().setLatitude(startIndex+1).setLongitude(0).build();
        Point p5 = Point.newBuilder().setLatitude(startIndex+0).setLongitude(0).build();

        polygons.add(Polygon.newBuilder().addPoints(p1).addPoints(p2).addPoints(p3).addPoints(p4).addPoints(p5).build());
        return polygons;
    }

    @Override
    public LocationDataset load() throws IOException {

        Locations.Builder locationsBuilder = Locations.newBuilder();
        for (Integer loc_count = 0; loc_count< 4; loc_count++){
            Location.Builder locationBuilder = Location.newBuilder();
            List<Polygon> polygons = getPolygons(loc_count);
            for (Polygon poly:polygons){
                locationBuilder.addPolygons(poly);
            }
            locationBuilder.setCity(loc_count.toString()).setPopulation(100).setQkm(10.0).setZipcode(loc_count.toString());

            locationsBuilder.addLocations(locationBuilder);
        }
        
        

        LocationDataset locationDataset = new LocationDataset(locationsBuilder.build());

        return locationDataset;
    }
    
}
