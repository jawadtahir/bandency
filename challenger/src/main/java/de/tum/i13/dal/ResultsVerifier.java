package de.tum.i13.dal;

import de.tum.i13.challenger.LatencyMeasurement;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

import java.sql.Connection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ResultsVerifier implements Runnable{
    private final ArrayBlockingQueue<ToVerify> verificationQueue;
    private final Connection db;
    private AtomicReference<Boolean> shutdown;
    private AtomicReference<Boolean> shuttingDown;

    public ResultsVerifier(ArrayBlockingQueue<ToVerify> verificationQueue, Connection db) {
        this.verificationQueue = verificationQueue;
        this.db = db;
        this.shuttingDown = new AtomicReference(false);
        this.shutdown = new AtomicReference(true);
    }

    static final Counter verifyMeasurementCounter = Counter.build()
            .name("verifyMeasurementCounter")
            .help("calls to verifyMeasurementCounter methods")
            .register();

    static final Counter durationMeasurementCounter = Counter.build()
            .name("durationMeasurementCounter")
            .help("calls to durationMeasurementCounter methods")
            .register();

    static final Histogram resultVerificationQueue = Histogram.build()
            .name("verificationQueue")
            .help("verificationQueue of Resultsverifier")
            .linearBuckets(0.0, 1_000.0, 21)
            .create()
            .register();

    @Override
    public void run() {
        this.shuttingDown.set(false);
        this.shutdown.set(false);

        while(!shuttingDown.get() || verificationQueue.size() > 0) {
            try {
                ToVerify poll = verificationQueue.poll(100, TimeUnit.MILLISECONDS);
                resultVerificationQueue.observe(verificationQueue.size());
                if(poll != null) {
                    if(poll.getType() == VerificationType.Measurement) {
                        LatencyMeasurement lm = poll.getLatencyMeasurement();

                        verifyMeasurementCounter.inc();
                    } else if(poll.getType() == VerificationType.Duration) {


                        durationMeasurementCounter.inc();
                    }
                    //Here we do some database operations, verifcation of results and so on
                    //System.out.println(poll);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.shutdown.set(true);
        System.out.println("shutting down");
    }

    public void shutdown() {
        if(this.shutdown.get()) //is already shutdown
            return;

        //set the shutdown flag to drain the queue
        this.shuttingDown.set(true);

        while(true) { //Wait till the queue is drained
            try {
                Thread.sleep(100);
                if(this.shutdown.get())
                    return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
