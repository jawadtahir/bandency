package de.tum.i13.datasets.location;

import de.tum.i13.challenger.BenchmarkType;

import java.io.IOException;
import java.nio.file.Path;

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
