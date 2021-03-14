package de.tum.i13.query;

import com.google.protobuf.Timestamp;
import de.tum.i13.Measurement;
import de.tum.i13.helper.TimestampHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;

public class MeanSlidingWindow {
    private final LinkedList<Measurement> measurements;
    private BigDecimal sumP1;
    private BigDecimal sumP2;

    public MeanSlidingWindow() {
        this.measurements = new LinkedList<>();
        this.sumP1 = BigDecimal.ZERO;
        this.sumP2 = BigDecimal.ZERO;
    }

    public void addMesurement(Measurement curr) {
        this.sumP1 = this.sumP1.add(new BigDecimal(curr.getP1()));
        this.sumP2 = this.sumP2.add(new BigDecimal(curr.getP2()));

        measurements.add(curr);
    }

    public double getMeanP1() {
        return this.sumP1.divide(new BigDecimal(this.measurements.size()), 3, RoundingMode.HALF_UP).doubleValue();
    }

    public double getMeanP2() {
        return this.sumP2.divide(new BigDecimal(this.measurements.size()), 3, RoundingMode.HALF_UP).doubleValue();
    }

    public boolean hasElements() {
        return measurements.size() > 0;
    }

    private boolean firstSmaller(Timestamp ts) {
        var tsfirst = measurements.get(0).getTimestamp();
        return TimestampHelper.isSmaller(tsfirst, ts);
    }

    public void resize(Timestamp maxTimestamp) {
        if(measurements.isEmpty())
            return;

        while(!measurements.isEmpty() && firstSmaller(maxTimestamp)) {
            Measurement m = measurements.remove();
            this.sumP1 = this.sumP1.subtract(new BigDecimal(m.getP1()));
            this.sumP2 = this.sumP2.subtract(new BigDecimal(m.getP2()));
        }
    }

    public int size() {
        return measurements.size();
    }

    public boolean isActive(Timestamp activeTreshold) {
        //TODO
        if(!hasElements())
            return false;

        Measurement last = measurements.getLast();
        return TimestampHelper.isSmaller(activeTreshold, last.getTimestamp());
    }
}
