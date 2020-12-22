package de.tum.i13.datasets.location;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mapbox.geojson.*;
import com.mapbox.geojson.gson.GeoJsonAdapterFactory;
import de.tum.i13.TodoException;
import de.tum.i13.bandency.Location;
import de.tum.i13.bandency.Locations;
import de.tum.i13.challenger.BenchmarkType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrepareLocationDataset {

    private IDatasetLoader loader = null;
    private Path dir = null;

    
    public PrepareLocationDataset(Path dir) {
        this.dir = dir;
    }


    public LocationDataset loadData(BenchmarkType bType) throws IOException {

        switch (bType) {
            case Verification:
                this.loader = new TestDatasetLoader();
                break;
            default:
                this.loader = new FileDatasetLoader(this.dir);
                break;
        }


        return this.loader.load();

        
    }

   
}
