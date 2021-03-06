package de.tum.i13.datasets.airquality;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.Measurement;
import org.tinylog.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AirqualityToBatch implements AirQualityDataSource {

    protected final long batchSize;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final AirqualityFileAccess afa;
    private long batchCnt;
    private boolean needsInit;
    private AirQualityParser currentParser;
    private AirQualityParser lastParser;
    private long diffmu;
    private Batch curr;

    public AirqualityToBatch(long batchSize, LocalDateTime from, LocalDateTime to, AirqualityFileAccess afa) {
        this.batchSize = batchSize;
        this.from = from;
        this.to = to;
        this.afa = afa;

        needsInit = true;
        diffmu = 0;
        this.batchCnt = 0;
    }

    private void init() {
        this.currentParser = new AirQualityParser(from, to, afa);

        LocalDateTime fromLastYear = from.minusDays(365);
        LocalDateTime toLastYear = to.minusDays(365);

        Logger.info("Starting new AirqualityToBatch - from: " + from + " to: " + to + " fromLastYear: " + fromLastYear + " toLastYear: " + toLastYear);

        this.diffmu = fromLastYear.until(from, ChronoUnit.SECONDS) * 1000;
        this.lastParser = new AirQualityParser(fromLastYear, toLastYear, afa);

        this.curr = readNextBatch();

        needsInit = false;
    }

    private long muFrom(Measurement a) {
        return (a.getTimestamp().getSeconds() * 1000L) + a.getTimestamp().getNanos();
    }

    private Batch readNextBatch() {

        Batch.Builder b = Batch.newBuilder();

        Measurement curr = currentParser.nextElement();
        Measurement last = lastParser.nextElement();
        long cnt = 0;
        while (cnt < this.batchSize && (curr != null) && (last != null)) {
            long diff = muFrom(curr) - (muFrom(last) + diffmu);

            if(diff > 0) {
                b.addLastyear(last);
                last = lastParser.nextElement();
            } else if(diff < 0) {
                b.addCurrent(curr);
                curr = currentParser.nextElement();
            } else { //in case they are the same, alternate between both sources
                if((batchCnt % 2) == 0) {
                    b.addLastyear(last);
                    last = lastParser.nextElement();
                } else {
                    b.addCurrent(curr);
                    curr = currentParser.nextElement();
                }
            }

            ++cnt;
        }

        b.setSeqId(this.batchCnt);
        b.setLast(false);
        ++this.batchCnt;

        return b.build();
    }

    @Override
    public boolean hasMoreElements() {
        if(needsInit) {
            init();
        }

        return this.currentParser.hasMoreElements() && this.lastParser.hasMoreElements();
    }

    @Override
    public Batch nextElement() {
        if(needsInit) {
            init();
        }

        Batch ret = curr;
        curr = readNextBatch();

        return ret;
    }


    @Override
    public void close() throws Exception {
        this.currentParser.close();
        this.lastParser.close();
    }
}
