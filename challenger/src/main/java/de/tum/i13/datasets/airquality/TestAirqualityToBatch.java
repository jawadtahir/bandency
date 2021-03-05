package de.tum.i13.datasets.airquality;

import com.google.protobuf.Timestamp;
import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.Batch.Builder;
import de.tum.i13.bandency.Measurement;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
        super(300, null, null, null);
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
        Long batch_size_per_city = batchSize / latLimit.longValue();
        long intervalPerRec = seconds/batch_size_per_city;
        for (long i = 0; i < batchSize; i++) {

            Long latLmt = i%(latLimit.longValue());
            // Long lngLmt = 1L;

            Instant event_ts = start.plus(intervalPerRec, ChronoUnit.SECONDS);
            Instant last_event_ts = event_ts.plus(-365L, ChronoUnit.DAYS);
            if ((i != 0) && (latLmt == 0)){
                start = event_ts;
            }
            
            Timestamp proto_ts = Timestamp.newBuilder().setSeconds(event_ts.getEpochSecond()).build();
            Timestamp last_proto_ts = Timestamp.newBuilder().setSeconds(last_event_ts.getEpochSecond()).build();

            

            Float lat = (float)latLmt - (float)0.5;
            Float lng = (float)0.5;
            Float p1 = p1enum.getC_low();
            Float p2 = p2enum.getC_low();

            
            Measurement measurement = Measurement.newBuilder().setTimestamp(proto_ts).setLatitude(lat)
                .setLongitude(lng).setP1(p1).setP2(p2).build();

            p1 = p1enum.getC_high();
            p2 = p2enum.getC_high();

            Measurement lastMeasurement = Measurement.newBuilder().setTimestamp(last_proto_ts).setLatitude(lat)
                    .setLongitude(lng).setP1(p1).setP2(p2).build();

            builder.addCurrent(measurement);
            builder.addLastyear(lastMeasurement);

            

        }
        if (is_no_current){
            builder.addAllCurrent(new ArrayList<Measurement>());
        }
        return builder;

    }


    private Batch test1 (Builder builder){
        /*
        Event timestamps are less than window length
        */

        Instant new_batch_ts = this.batchTime.plus(4, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.GOOD, EPAP2Table.GOOD);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();


    }

    private Batch test2(Builder builder){
        /**
         * Fills the window
         */

        Instant new_batch_ts = this.batchTime.plus(5, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.MODERATE, EPAP2Table.MODERATE);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();
    }


    private Batch test3(Builder builder){
        /**
         * Test AQI sliding window
         */

        Instant new_batch_ts = this.batchTime.plus(24, ChronoUnit.HOURS);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.SENSITIVE_UNHEALTY, EPAP2Table.SENSITIVE_UNHEALTY);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();

    }

    private Batch test4(Builder builder){
        /**
         * Test 2 inactive cities
         */

        Instant new_batch_ts = this.batchTime.plus(12, ChronoUnit.MINUTES);
        Builder batchBuilder = loadGen(builder, this.batchTime, new_batch_ts, this.batchSize, EPAP1Table.UNHEALTY, EPAP2Table.UNHEALTY, 3F, 1F, false);
        this.batchTime = new_batch_ts;

        return batchBuilder.build();

    }

    private Batch test5(Builder builder){
        /**
         * Test scenario if current year does not have any events
         */

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
                this.test = T5;
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

    @Override
    public void close() throws Exception {
    }

}

