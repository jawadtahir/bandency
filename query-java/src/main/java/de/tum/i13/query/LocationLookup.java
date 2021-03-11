package de.tum.i13.query;

import de.tum.i13.Location;
import de.tum.i13.Point;
import de.tum.i13.Polygon;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class LocationLookup {

    private long cacheHit = 0;
    private long cacheMiss = 0;

    private final List<Location> locations;
    private HashMap<Pair<Double, Double>, String> cache;

    public LocationLookup(List<Location> locations) {
        this.locations = locations;

        this.cache = new HashMap<>();
    }

    public String lookupLocation(double longitude, double latitude) {

        Pair<Double, Double> p = Pair.of(longitude, latitude);

        if(cache.containsKey(p)) {
            ++cacheHit;
            return cache.get(p);
        } else {
            for (Location loc : locations) {
                if (isInside(loc, longitude, latitude)) {
                    ++cacheMiss;
                    String city = loc.getCity();
                    cache.put(p, city);
                    return city;
                }
            }
            ++cacheMiss;
            cache.put(p, null);
            return null;
        }
    }

    // Given three colinear points p, q, r,
    // the function checks if point q lies
    // on line segment 'pr'
    static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.getLatitude() <= Math.max(p.getLatitude(), r.getLatitude()) &&
                q.getLatitude() >= Math.min(p.getLatitude(), r.getLatitude()) &&
                q.getLongitude() <= Math.max(p.getLongitude(), r.getLongitude()) &&
                q.getLongitude() >= Math.min(p.getLongitude(), r.getLongitude()))
        {
            return true;
        }
        return false;
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are colinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    static int orientation(Point p, Point q, Point r)
    {
        double val = (q.getLongitude() - p.getLongitude()) * (r.getLatitude() - q.getLatitude())
                - (q.getLatitude() - p.getLatitude()) * (r.getLongitude() - q.getLongitude());

        if (val == 0.0)
        {
            return 0; // colinear
        }
        return (val > 0.0) ? 1 : 2; // clock or counterclock wise
    }

    // The function that returns true if
    // line segment 'p1q1' and 'p2q2' intersect.
    static boolean doIntersect(Point p1, Point q1,
                               Point p2, Point q2)
    {
        // Find the four orientations needed for
        // general and special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
        {
            return true;
        }

        // Special Cases
        // p1, q1 and p2 are colinear and
        // p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1))
        {
            return true;
        }

        // p1, q1 and p2 are colinear and
        // q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1))
        {
            return true;
        }

        // p2, q2 and p1 are colinear and
        // p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2))
        {
            return true;
        }

        // p2, q2 and q1 are colinear and
        // q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2))
        {
            return true;
        }

        // Doesn't fall in any of the above cases
        return false;
    }

    private boolean isInside(Location loc, double longitude, double latitude) {
        Point sensorLoc = Point.newBuilder().setLongitude(longitude).setLatitude(latitude).build();

        int cnt = 0;
        for(Polygon poly : loc.getPolygonsList()) {
            if(poly.getPointsCount() < 3)
                continue;

            Point extreme = Point.newBuilder()
                    .setLatitude(400)
                    .setLatitude(sensorLoc.getLatitude())
                    .build();
            var polygon = poly.getPointsList();

            int count = 0, i = 0;
            do {
                int next = (i + 1) % poly.getPointsCount();

                // Check if the line segment from 'p' to
                // 'extreme' intersects with the line
                // segment from 'polygon[i]' to 'polygon[next]'
                if (doIntersect(polygon.get(i), polygon.get(next), sensorLoc, extreme)) {
                    // If the point 'p' is colinear with line
                    // segment 'i-next', then check if it lies
                    // on segment. If it lies, return true, otherwise false
                    if (orientation(polygon.get(i), sensorLoc, polygon.get(next)) == 0) {
                        return onSegment(polygon.get(i), sensorLoc, polygon.get(next));
                    }

                    count++;
                }
                i = next;
            } while (i != 0);

            // Return true if count is odd, false otherwise
            if ((count % 2 == 1) == true) { // Same as (count%2 == 1
                return true;
            }
        }
        return false;
    }

    public long getCacheHit() {
        return cacheHit;
    }

    public long getCacheMiss() {
        return cacheMiss;
    }

    public void restoreCache(Object fromFile) {
        this.cache = (HashMap<Pair<Double, Double>, String>) fromFile;
    }

    public Object snapshotCache() {
        return this.cache;
    }
}
