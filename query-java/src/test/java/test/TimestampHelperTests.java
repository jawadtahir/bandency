package test;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import static de.tum.i13.helper.TimestampHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimestampHelperTests {

    @Test
    public void testIsSmaller() {
        Timestamp smaller = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp bigger = Timestamp.newBuilder().setSeconds(1001).setNanos(0).build();

        assertEquals(true, isSmaller(smaller, bigger));
    }

    @Test
    public void testIsBigger() {
        Timestamp smaller = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp bigger = Timestamp.newBuilder().setSeconds(1001).setNanos(0).build();

        assertEquals(false, isSmaller(bigger, smaller));
    }

    @Test
    public void testIsSmallerNanos() {
        Timestamp smaller = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp bigger = Timestamp.newBuilder().setSeconds(1000).setNanos(1).build();

        assertEquals(true, isSmaller(smaller, bigger));
    }

    @Test
    public void testAddDayTimeunit() {
        Timestamp t = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();

        Timestamp newts = addTimeunit(t, TimeUnit.DAYS,1);

        assertEquals(newts.getSeconds(), 1000 + 86400);
    }

    @Test
    public void testSubstractDayTimeunit() {
        Timestamp t = Timestamp.newBuilder().setSeconds(1000 + 86400).setNanos(0).build();

        Timestamp newts = addTimeunit(t, TimeUnit.DAYS,-1);
        assertEquals(newts.getSeconds(), 1000);
    }

    @Test
    public void testAddFiveDays() {
        Timestamp t = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp newts = addTimeunit(t, TimeUnit.DAYS,5);

        assertEquals(newts.getSeconds(), 1000 + (86400*5));
    }

    @Test
    public void testAddSecond() {
        Timestamp t = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp newts = addTimeunit(t, TimeUnit.SECONDS,5);

        assertEquals(newts.getSeconds(), 1005);
    }

    @Test
    public void testAddMillisecond() {
        Timestamp t = Timestamp.newBuilder().setSeconds(1000).setNanos(0).build();
        Timestamp newts = addTimeunit(t, TimeUnit.MILLISECONDS,5);

        assertEquals(newts.getSeconds(), 1000);
        assertEquals(newts.getNanos(), 5000000);
    }

    @Test
    public void testInstantConversion() {
        Instant parse = Instant.parse("2007-12-03T10:15:30.00Z");

        Timestamp ts = instantToTimestamp(parse);
        Instant convertedBack = timestampToInstant(ts);

        assertEquals(parse, convertedBack);
    }

    private ZonedDateTime nextTsFor(Instant parse) {
        Timestamp ts = instantToTimestamp(parse);
        Timestamp nextTS = nextSnapshot(ts);

        Instant nextTs = timestampToInstant(nextTS);
        ZonedDateTime utc = nextTs.atZone(ZoneId.of("UTC"));
        return utc;
    }

    @Test
    public void nextTimestampFiveAndSeconds() {
        Instant parse = Instant.parse("2007-12-03T10:15:30.00Z");
        ZonedDateTime utc = nextTsFor(parse);

        assertEquals(10, utc.getHour());
        assertEquals(20, utc.getMinute());
        assertEquals(0, utc.getSecond());
        assertEquals(0, utc.getNano());
    }

    @Test
    public void nextTimestampSevenAndSeconds() {
        Instant parse = Instant.parse("2007-12-03T10:17:30.00Z");
        ZonedDateTime utc = nextTsFor(parse);

        assertEquals(10, utc.getHour());
        assertEquals(20, utc.getMinute());
        assertEquals(0, utc.getSecond());
        assertEquals(0, utc.getNano());
    }
}
