package de.tum.i13.query;

import com.google.protobuf.Timestamp;
import de.tum.i13.helper.TimestampHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FiveDaysAQISlidingWindow {

    private final LinkedList<Timestamp> tss;
    private final LinkedList<BigDecimal> values;
    private BigDecimal sum;

    public FiveDaysAQISlidingWindow() {
        values = new LinkedList<>();
        tss = new LinkedList<>();
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
        var tsfirst = tss.peek();
        return TimestampHelper.isSmaller(tsfirst, ts);
    }

    public boolean hasElements() {
        return tss.size() > 0;
    }

    public void resize(Timestamp ts) {
        if(tss.isEmpty())
            return;

        while(hasElements() && firstSmaller(ts)) {
            tss.remove();
            this.sum = sum.subtract(values.peek());

            values.remove();
        }
    }
}
