package de.tum.i13.datasets.financial;

import java.io.IOException;
import de.tum.i13.bandency.Batch;
import de.tum.i13.datasets.cache.CloseableSource;

public class FinancialDataLoader implements CloseableSource<Batch> {

    private CloseableSource<String> lines;

    public FinancialDataLoader(CloseableSource<String> datasource) {
        this.lines = datasource;
    }

    @Override
    public boolean hasMoreElements() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Batch nextElement() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        lines.close();
    }
}
