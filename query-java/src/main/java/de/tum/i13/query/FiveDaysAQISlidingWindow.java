package de.tum.i13.query;

import com.google.protobuf.Timestamp;
import de.tum.i13.helper.TimestampHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class FiveDaysAQISlidingWindow {

    private final ArrayList<Timestamp> tss;
    private final ArrayList<BigDecimal> values;
    private BigDecimal sum;

    public FiveDaysAQISlidingWindow() {
        values = new ArrayList<>();
        tss = new ArrayList<>();
        sum = new BigDecimal(0);
    }

    public int size() {
        return values.size();
    }

    public void addMeasurement(Timestamp ts, double v) {
        BigDecimal bigDecimal = new BigDecimal(v);
        values.add(bigDecimal);
        sum = sum.add(bigDecimal);
        tss.add(ts);
    }

    public double getMean() {
        return sum.divide(new BigDecimal(values.size()), 3, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean firstSmaller(Timestamp ts) {
        var tsfirst = tss.get(0);
        return TimestampHelper.isSmaller(tsfirst, ts);
    }

    public boolean hasElements() {
        return tss.size() > 0;
    }

    public void resize(Timestamp ts) {
        if(tss.isEmpty())
            return;

        while(hasElements() && firstSmaller(ts)) {
            tss.remove(0);
            this.sum = sum.subtract(values.get(0));
            values.remove(0);
        }
    }
}
