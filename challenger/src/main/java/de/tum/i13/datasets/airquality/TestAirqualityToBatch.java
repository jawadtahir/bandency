package de.tum.i13.datasets.airquality;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Random;

import com.google.protobuf.Timestamp;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.Measurement;
import de.tum.i13.bandency.Batch.Builder;

public class TestAirqualityToBatch extends AirqualityToBatch {

    public static final String T1 = "T1";
    public static final String T2 = "T2";
    public static final String T3 = "T3";
    public static final String T4 = "T4";
    public static final String T5 = "T5";
    public static final String T6 = "T6";

    String test = T1;
    Instant snapshotTime = Instant.MIN;
    Instant batchTime = Instant.MIN;
    Instant startTime = Instant.MIN;

    public TestAirqualityToBatch() {
        super(10, null, null, null);
        Instant now = Instant.now();
        this.snapshotTime = now;
        this.batchTime = now;
        this.startTime = now;

    }

    protected static Builder loadGen(Builder builder, Instant start, Instant end, Long batchSize, EPAP1Table p1enum, EPAP2Table p2enum) {

        
        return loadGen(builder, start, end, batchSize, p1enum, p2enum, 6F, 1F, false);

    }


    protected static Builder loadGen(Builder builder, Instant start, Instant end, Long batchSize, EPAP1Table p1enum, EPAP2Table p2enum, Float latLimit, Float lngLimit, Boolean is_no_current) {

        Long seconds = Duration.between(start,end).getSeconds();
        long intervalPerRec = seconds/batchSize;
        for (long i = 0; i < batchSize; i++) {
            Instant event_ts = start.plus(intervalPerRec, ChronoUnit.SECONDS);
            Instant last_event_ts = event_ts.plus(-365L, ChronoUnit.DAYS);
            start = event_ts;
            Timestamp proto_ts = Timestamp.newBuilder().setSeconds(event_ts.getEpochSecond()).build();
            Timestamp last_proto_ts = Timestamp.newBuilder().setSeconds(last_event_ts.getEpochSecond()).build();

            Float lat = getRandomFloat(0, latLimit);
            Float lng = getRandomFloat(0, lngLimit);
            Float p1 = getRandomFloat(p1enum.getC_low(), p1enum.getC_high());
            Float p2 = getRandomFloat(p2enum.getC_low(), p2enum.getC_high());

            
            Measurement measurement = null;
            if (!is_no_current){
                measurement = Measurement.newBuilder().setTimestamp(proto_ts).setLatitude(lat)
                    .setLongitude(lng).setP1(p1).setP2(p2).build();
            }

            p1 = getRandomFloat(p1enum.getC_low(), p1enum.getC_high());
            p2 = getRandomFloat(p2enum.getC_low(), p2enum.getC_high());

            Measurement lastMeasurement = Measurement.newBuilder().setTimestamp(last_proto_ts).setLatitude(lat)
                    .setLongitude(lng).setP1(p1).setP2(p2).build();

            builder.addCurrent(measurement);
            builder.addLastyear(lastMeasurement);

        }
        return builder;

    }


    protected static Float getRandomFloat(float min, float max){
        return min + new Random().nextFloat() * (max - min);
    }

    private Batch test1 (Builder builder){

        Instant new_batch_ts = this.batchTime.plus(3, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.GOOD, EPAP2Table.GOOD);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();


    }

    private Batch test2(Builder builder){

        Instant new_batch_ts = this.batchTime.plus(5, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.MODERATE, EPAP2Table.MODERATE);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();
    }


    private Batch test3(Builder builder){

        Instant new_batch_ts = this.batchTime.plus(24, ChronoUnit.HOURS);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.SENSITIVE_UNHEALTY, EPAP2Table.SENSITIVE_UNHEALTY);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();

    }

    private Batch test4(Builder builder){

        Instant new_batch_ts = this.batchTime.plus(12, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.UNHEALTY, EPAP2Table.UNHEALTY, 3F, 1F, false);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();

    }

    private Batch test5(Builder builder){

        Instant new_batch_ts = this.batchTime.plus(24, ChronoUnit.HOURS);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.VERY_UNHEALTHY, EPAP2Table.VERY_UNHEALTHY, 6F, 1F, true);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();

    }

    @Override
    public Batch nextElement() {
        Builder builder = Batch.newBuilder();
        Batch batch = null;

        switch (this.test) {
            case T1:
                batch = test1(builder);
                this.test = T2;
                break;

            case T2:

                batch = test2(builder);
                this.test = T3;
                break;
            
            case T3:
                batch = test3(builder);
                this.test = T4;
                break;

            case T4:

                batch = test4(builder);
                this.test = T6;
                break;
            case T5:

                batch = test5(builder);
                this.test = T6;
                break;

            default:
                break;
        }


        return batch;

    }


    @Override
    public boolean hasMoreElements() {
        if(this.test.equals(T6)) {
            return false;
        } else {
            return true;
        }

    }

}
