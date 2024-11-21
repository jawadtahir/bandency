package org.debs.challenger2.dataset;

import org.debs.challenger2.rest.dao.Batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataLoader {

    private final String dataDir;
    private final IDataStore dataStore;

    public DataLoader (IDataStore dataStore, String dataDir){
        this.dataDir = dataDir;
        this.dataStore = dataStore;
    }

    public void load() throws IOException {

        Path dir = Paths.get(dataDir);
        List<Path> imageFilePaths = Files.list(dir).filter(f -> !f.endsWith(".tif")).collect(Collectors.toList());
        Long seq_id = 0L;
        for (Path imageFilePath : imageFilePaths){
            byte[] imageData = Files.readAllBytes(imageFilePath);
            seq_id++;
            Batch batch = new Batch(seq_id, imageData);
            dataStore.addBatch(seq_id, batch);
        }
    }


}
