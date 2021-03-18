package test;

import com.google.protobuf.Timestamp;
import de.tum.i13.Measurement;
import de.tum.i13.query.MeanSlidingWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeanSlidingWindowTest {

    @Test
    public void testEmptyWindow() {
        var msw = new MeanSlidingWindow(null);
        assertEquals(false, msw.hasElements());
    }

    @Test
    public void testOneElement(){
        var msw = new MeanSlidingWindow(null);
        Measurement m = Measurement.newBuilder()
                .setP1(1.0f)
                .setP2(2.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(1000).build())
                .build();

        msw.addMesurement(m);

        assertEquals(1.0, msw.getMeanP1());
        assertEquals(2.0, msw.getMeanP2());
    }

    @Test
    public void testTwoElement(){
        var msw = new MeanSlidingWindow(null);
        Measurement m1 = Measurement.newBuilder()
                .setP1(1.0f)
                .setP2(2.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(1000).build())
                .build();

        msw.addMesurement(m1);

        Measurement m2 = Measurement.newBuilder()
                .setP1(2.0f)
                .setP2(3.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(1000).build())
                .build();

        msw.addMesurement(m2);

        assertEquals(1.5, msw.getMeanP1());
        assertEquals(2.5, msw.getMeanP2());
    }

    @Test
    public void testResizeSeconds(){
        var msw = new MeanSlidingWindow(null);
        Measurement m1 = Measurement.newBuilder()
                .setP1(1.0f)
                .setP2(2.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(500).build())
                .build();

        msw.addMesurement(m1);

        Measurement m2 = Measurement.newBuilder()
                .setP1(2.0f)
                .setP2(3.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(1000).build())
                .build();

        msw.addMesurement(m2);

        msw.resize(Timestamp.newBuilder().setSeconds(600).build());

        assertEquals(1, msw.size());
    }

    @Test
    public void testResizeNanos(){
        var msw = new MeanSlidingWindow(null);
        Measurement m1 = Measurement.newBuilder()
                .setP1(1.0f)
                .setP2(2.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(500).setNanos(500).build())
                .build();

        msw.addMesurement(m1);

        Measurement m2 = Measurement.newBuilder()
                .setP1(2.0f)
                .setP2(3.0f)
                .setTimestamp(Timestamp.newBuilder().setSeconds(1000).build())
                .build();

        msw.addMesurement(m2);
        msw.resize(Timestamp.newBuilder().setSeconds(500).setNanos(501).build());

        assertEquals(1, msw.size());

        assertEquals(2.0f, msw.getMeanP1());
        assertEquals(3.0f, msw.getMeanP2());
    }
}
