package test;

import com.google.protobuf.Timestamp;
import de.tum.i13.query.FiveDaysAQISlidingWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FiveDaysAQISlidingWindowTest {

    @Test
    public void testHasEntries() {
        var fdaslw = new FiveDaysAQISlidingWindow();

        assertEquals(0, fdaslw.size());

        fdaslw.addMeasurement(Timestamp.newBuilder().build(), 1.0);
        assertEquals(1, fdaslw.size());
    }

    @Test
    public void getMeanSimple() {
        var fdaslw = new FiveDaysAQISlidingWindow();
        fdaslw.addMeasurement(Timestamp.newBuilder().build(), 1.0);
        fdaslw.addMeasurement(Timestamp.newBuilder().build(), 2.0);

        double res = fdaslw.getMean();
        assertEquals(1.5, res);
    }

    @Test
    public void testResize() {
        var fdaslw = new FiveDaysAQISlidingWindow();
        fdaslw.addMeasurement(Timestamp.newBuilder().setSeconds(500).setNanos(500).build(), 1.0);
        fdaslw.addMeasurement(Timestamp.newBuilder().setSeconds(1000).build(), 2.0);

        fdaslw.resize(Timestamp.newBuilder().setSeconds(500).setNanos(501).build());

        assertEquals(1, fdaslw.size());
    }
}
