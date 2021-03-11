package de.tum.i13.helper;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public class TimestampHelper {
    public static boolean isSmaller(Timestamp smaller, Timestamp bigger) {

        if(smaller.getSeconds() < bigger.getSeconds()) {
            return true;
        }

        if(smaller.getSeconds() == bigger.getSeconds() && smaller.getNanos() < bigger.getNanos()) {
            return true;
        }

        return false;
    }

    public static Timestamp addTimeunit(Timestamp t, TimeUnit days, int i) {

        long oldToNanos = t.getSeconds() * 1000000000l;
        oldToNanos += t.getNanos();

        long toNanos = days.toNanos(i);
        oldToNanos += toNanos;

        int justNanos = (int)(oldToNanos % 1000000000l);
        long justSeconds = oldToNanos /1000000000l;

        return Timestamp.newBuilder()
                .setSeconds(justSeconds)
                .setNanos(justNanos).build();
    }

    public static Instant timestampToInstant(Timestamp ts) {
        return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
    }

    public static Timestamp instantToTimestamp(Instant i) {
        return Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond())
                .setNanos(i.getNano())
                .build();
    }

    public static Timestamp nextSnapshot(Timestamp ts) {

        Instant instant = timestampToInstant(ts);
        ZonedDateTime utc = instant.atZone(ZoneId.of("UTC"));

        int rest = utc.getMinute() % 5;
        ZonedDateTime zonedDateTime = utc.minusMinutes(rest).plusMinutes(5).minusSeconds(utc.getSecond());

        return instantToTimestamp(zonedDateTime.toInstant());
    }
}
