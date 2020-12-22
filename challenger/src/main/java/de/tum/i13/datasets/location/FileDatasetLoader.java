package de.tum.i13.datasets.location;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mapbox.geojson.*;
import com.mapbox.geojson.gson.GeoJsonAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.tinylog.Logger;

import de.tum.i13.bandency.Location;
import de.tum.i13.bandency.Locations;

class FileDatasetLoader implements IDatasetLoader {

    private static String url = "https://www.suche-postleitzahl.org/download_v1/wgs84/hoch/plz-5stellig/geojson/plz-5stellig.geojson";
    protected Path dir = null;

    public FileDatasetLoader(Path dir) {
        this.dir = dir;
    }

    @Override
    public LocationDataset load() throws IOException {
        this.ensureDataSetDownloaded();

        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapterFactory(GeoJsonAdapterFactory.create());
        gson.registerTypeAdapterFactory(GeometryAdapterFactory.create());
        Gson g = gson.create();

        try(JsonReader reader = new JsonReader(new FileReader(locationData()))) {
            Stopwatch sw = Stopwatch.createStarted();
            FeatureCollection fc = g.fromJson(reader, FeatureCollection.class);
            Logger.debug("parsing time, seconds: " + sw.elapsed(TimeUnit.SECONDS));

            int cnt = 0;
            int err_cnt = 0;
            Locations.Builder allLocations = Locations.newBuilder();

            for (Feature f : fc.features()) {
                try {
                    Location.Builder locationBuilder = Location.newBuilder();

                    String plz = getPropertyString(f, "plz");
                    locationBuilder.setZipcode(plz.trim());

                    String note = getPropertyString(f, "note");
                    note = note.replace(plz, "");
                    locationBuilder.setZipcode(note.trim());

                    float qkm = f.hasProperty("qkm") ? f.getNumberProperty("qkm").floatValue() : 0.0f;
                    locationBuilder.setQkm(qkm);

                    int einwohner = f.hasProperty("einwohner") ? f.getNumberProperty("einwohner").intValue() : 0;
                    locationBuilder.setPopulation(einwohner);

                    if (plz.isEmpty() || note.isEmpty()) {
                        Logger.debug("skipping, no plz and note: cnt: " + cnt + " plz: " + plz + " node: " + note + " qkm: " + qkm + " einwohner: " + einwohner);
                        continue;
                    }

                    Geometry geometry = f.geometry();
                    if(geometry instanceof Polygon) {
                        Polygon p = (Polygon)geometry;
                        if(p.coordinates().size() > 0) {
                            for(List<Point> points : p.coordinates()) {
                                de.tum.i13.bandency.Polygon polygon = validatedFrom(points);
                                if(polygon != null) {
                                    locationBuilder.addPolygons(polygon);
                                }
                            }
                        } else {
                            Logger.debug("skipping, polygon no coordinates: cnt: " + cnt + " plz: " + plz + " node: " + note + " qkm: " + qkm + " einwohner: " + einwohner);
                            continue;
                        }
                    } else if(geometry instanceof MultiPolygon){
                        MultiPolygon p = (MultiPolygon)geometry;

                        for(List<List<Point>> points_p : p.coordinates()) {
                            for(List<Point> point_pp : points_p) {
                                de.tum.i13.bandency.Polygon polygon = validatedFrom(point_pp);
                                if(polygon != null) {
                                    locationBuilder.addPolygons(polygon);
                                }
                            }
                        }
                    }

                    int size = locationBuilder.getPolygonsCount();
                    if(size == 0) {
                        ++err_cnt;
                        System.out.println("error size: " + plz + " err_cnt: " + err_cnt);
                    } else {
                        allLocations.addLocations(locationBuilder.build());
                    }
                    ++cnt;
                } catch (Exception ex) {
                    Logger.error(ex);
                }
            }

            sw.stop();
            Logger.info("geojson loading time: " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");

            LocationDataset ds = new LocationDataset(allLocations.build());
            return ds;
        }
    }


    de.tum.i13.bandency.Polygon validatedFrom(List<Point> points) {
        if (points.size() < 3) { //below it would be just a line
            return null;
        }

        de.tum.i13.bandency.Polygon.Builder pb = de.tum.i13.bandency.Polygon.newBuilder();

        Point first = points.get(0);
        Point last = points.get(points.size() - 1);

        if (first.longitude() == last.longitude() && first.latitude() == last.latitude()) {
            for (com.mapbox.geojson.Point point : points) {
                double latitude = point.latitude();
                double longitude = point.longitude();
                de.tum.i13.bandency.Point buildPoint = de.tum.i13.bandency.Point.newBuilder().setLatitude(latitude).setLongitude(longitude).build();

                pb.addPoints(buildPoint);
            }
        } else {
            //Logger.debug("skipping, polygon open: cnt: " + cnt + " plz: " + plz + " node: " + note + " qkm: " + qkm + " einwohner: " + einwohner);
            return null;
        }

        return pb.build();
    }

    void ensureDataSetDownloaded() throws IOException {
        File f = locationData();
        if(f.exists())
            return;
        else {
            OkHttpClient client = new OkHttpClient();

            //Download to tempfile in same folder, this should avoid that a incomplete file is used
            File tempFile = locationDataTemp();
            tempFile.delete();

            Logger.debug("started downloading locations");
            Request request = new Request.Builder().url(url).build();
            Response resp = client.newCall(request).execute();

            BufferedSink sink = Okio.buffer(Okio.sink(tempFile));
            sink.writeAll(resp.body().source());
            sink.close();

            //Rename tempfile to final name
            tempFile.renameTo(locationData());
            Logger.debug("finished downloading locations");
        }
    }


    private File locationDataTemp() {
        return new File(dir.toFile(), "plz-" + System.currentTimeMillis() + ".json");
    }

    private File locationData() {
        return new File(dir.toFile(), "plz.json");
    }

    private String getPropertyString(Feature f, String key) {
        return (f.hasProperty(key) && !f.getProperty(key).isJsonNull()) ? f.getStringProperty(key) : "";
    }
    
}
