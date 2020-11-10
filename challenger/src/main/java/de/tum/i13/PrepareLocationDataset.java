package de.tum.i13;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeometryAdapterFactory;
import com.mapbox.geojson.gson.GeoJsonAdapterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class PrepareLocationDataset {
    private static String url = "https://www.suche-postleitzahl.org/download_v1/wgs84/hoch/plz-5stellig/geojson/plz-5stellig.geojson";
    private Path dir;

    public PrepareLocationDataset(Path dir) {
        this.dir = dir;
    }

    private File locationDataTemp() {
        return new File(dir.toFile(), "plz-" + System.currentTimeMillis() + ".json");
    }

    private File locationData() {
        return new File(dir.toFile(), "plz.json");
    }

    public LocationDataset loadData() throws IOException {
        ensureDataSetDownloaded();

        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapterFactory(GeoJsonAdapterFactory.create());
        gson.registerTypeAdapterFactory(GeometryAdapterFactory.create());
        Gson g = gson.create();

        try(JsonReader reader = new JsonReader(new FileReader(locationData()))) {
            Stopwatch sw = Stopwatch.createStarted();
            FeatureCollection fc = g.fromJson(reader, FeatureCollection.class);
            sw.stop();
            System.out.println("parsing time, seconds: " + sw.elapsed(TimeUnit.SECONDS));

            sw.reset(); sw.start();
            Feature feature = fc.features().get(0);
            sw.stop();
            System.out.println("get first feature: " + sw.elapsed(TimeUnit.SECONDS));

        }



        return new LocationDataset();

    }

    private void ensureDataSetDownloaded() throws IOException {
        File f = locationData();
        if(f.exists())
            return;
        else {
            OkHttpClient client = new OkHttpClient();

            //Download to tempfile in same folder, this should avoid that a incomplete file is used
            File tempFile = locationDataTemp();
            tempFile.delete();

            Request request = new Request.Builder().url(url).build();
            Response resp = client.newCall(request).execute();

            BufferedSink sink = Okio.buffer(Okio.sink(tempFile));
            sink.writeAll(resp.body().source());
            sink.close();

            //Rename tempfile to final name
            tempFile.renameTo(locationData());
        }
    }
}
