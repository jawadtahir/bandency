package test;

import de.tum.i13.Location;
import de.tum.i13.Point;
import de.tum.i13.Polygon;
import de.tum.i13.query.LocationLookup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LocationLookupTest {

    @Test
    public void testIfJavaWorks() {
        assertEquals(true, true);
    }

    private LocationLookup extracted() {
        double[][] triangle = new double[][] {{1.0, 1.0}, {1.0, 2.0}, {2.0, 1.0}, {1.0, 1.0}};
        Polygon.Builder p = Polygon.newBuilder();

        for(double[] pos : triangle) {
            p.addPoints(Point.newBuilder().setLatitude(pos[0]).setLongitude(pos[1]).build());
        }

        Location loc = Location.newBuilder().addPolygons(p.build()).setCity("trianglecity").setZipcode("123").build();

        ArrayList<Location> locs = new ArrayList<>();
        locs.add(loc);
        return new LocationLookup(locs);
    }

    @Test
    public void testInside() {
        LocationLookup ll = extracted();
        Optional<String> inside = ll.lookupLocation(1.1, 1.1);
        assertEquals("trianglecity", inside.get());
    }

    @Test
    public void equalPoint() {
        LocationLookup ll = extracted();
        Optional<String> inside = ll.lookupLocation(1.0, 1.0);
        assertEquals("trianglecity", inside.get());
    }


    @Test
    public void testOutside() {
        LocationLookup ll = extracted();
        Optional<String> inside = ll.lookupLocation(0.9, .9);
        assertTrue(inside.isEmpty());
    }

    @Test
    public void testCache() {
        LocationLookup ll = extracted();

        ll.lookupLocation(1.1, 1.1);
        assertEquals(0, ll.getCacheHit());
        assertEquals(1, ll.getCacheMiss());

        ll.lookupLocation(1.1, 1.1);
        assertEquals(1, ll.getCacheHit());
        assertEquals(1, ll.getCacheMiss());
    }
}
