package de.tum.i13.datasets.cache;

import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;

public class InMemoryLoader<T> {

    private CloseableSource<T> batchDatasource;

    public InMemoryLoader(CloseableSource<T> batchDatasource) {

        this.batchDatasource = batchDatasource;
    }

    public InMemoryDataset<T> loadData(int batches) {
        try (ProgressBar pb = new ProgressBar("Loading data", batches)) {
            ArrayList<T> inMemoryBatches = new ArrayList<>(batches);

            Iterator<T> batchIterator = this.batchDatasource.asIterator();

            int cnt = 0;
            while (cnt < batches && batchIterator.hasNext()) {
                inMemoryBatches.add(batchIterator.next());
                ++cnt;
                pb.step();
            }

            return new InMemoryDataset<T>(inMemoryBatches);
        }
    }
}
