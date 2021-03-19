package de.tum.i13.datasets.cache;

import de.tum.i13.bandency.Batch;
import de.tum.i13.challenger.BenchmarkType;
import de.tum.i13.datasets.airquality.AirQualityDataSource;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;

public class InMemoryLoader {

    private AirqualityDataset ad;

    public InMemoryLoader(AirqualityDataset ad) {

        this.ad = ad;
    }

    public InMemoryDataset loadData(int batches, int batchSize) {
        try (ProgressBar pb = new ProgressBar("Loading data", batches)) {
            AirQualityDataSource airQualityDataSource = ad.newDataSource(BenchmarkType.Evaluation, batchSize);
            ArrayList<Batch> inMemoryBatches = new ArrayList<>(batches);

            Iterator<Batch> batchIterator = airQualityDataSource.asIterator();

            int cnt = 0;
            while (cnt < batches && batchIterator.hasNext()) {
                inMemoryBatches.add(batchIterator.next());
                ++cnt;
                pb.step();
            }

            return new InMemoryDataset(inMemoryBatches);
        }
    }
}
