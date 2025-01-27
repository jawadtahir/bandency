package org.debs.challenger2.dataset;

import org.debs.challenger2.rest.dao.Batch;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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

            byte[] packedData = null;

            try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
                packer.packInt(seq_id.intValue()); //sequence
                packer.packInt(0); //print_id
                packer.packInt(0); //tile_id
                packer.packInt(0); //layer
                packer.packBinaryHeader(imageData.length);
                packer.writePayload(imageData);
                packedData = packer.toByteArray();
            }

            Batch batch = new Batch(seq_id, packedData);
            dataStore.addBatch(seq_id, batch);
        }
    }


}
