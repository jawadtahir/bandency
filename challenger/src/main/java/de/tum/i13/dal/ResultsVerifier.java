package de.tum.i13.dal;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ResultsVerifier implements Runnable{
    private final ArrayBlockingQueue<ToVerify> verificationQueue;
    private AtomicReference<Boolean> shutdown;
    private AtomicReference<Boolean> shuttingDown;

    public ResultsVerifier(ArrayBlockingQueue<ToVerify> verificationQueue, DB db) {
        this.verificationQueue = verificationQueue;
        this.shuttingDown = new AtomicReference(false);
        this.shutdown = new AtomicReference(true);
    }

    @Override
    public void run() {
        this.shuttingDown.set(false);
        this.shutdown.set(false);

        while(!shuttingDown.get() || verificationQueue.size() > 0) {
            try {
                ToVerify poll = verificationQueue.poll(100, TimeUnit.MILLISECONDS);
                if(poll != null) {
                    //Here we do some database operations, verifcation of results and so on
                    System.out.println(poll);
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
