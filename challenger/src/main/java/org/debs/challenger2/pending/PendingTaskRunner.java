package org.debs.challenger2.pending;

import org.debs.challenger2.db.IQueries;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PendingTaskRunner implements Runnable, Closeable {
    private final ArrayBlockingQueue<IPendingTask> pendingTasks;

    private final IQueries queryImpl;
    private AtomicReference<Boolean> shutdown;
    private AtomicReference<Boolean> shuttingDown;

    public PendingTaskRunner(ArrayBlockingQueue<IPendingTask> pendingTasks, IQueries queries){
        this.pendingTasks = pendingTasks;
        this.queryImpl = queries;
        this.shutdown = new AtomicReference<>(false);
        this.shuttingDown = new AtomicReference<>(false);
    }
    @Override
    public void run() {
        while (!shuttingDown.get() || !pendingTasks.isEmpty()){
            try {
                IPendingTask task = pendingTasks.poll(100, TimeUnit.MILLISECONDS);
                if (task!=null){
                    task.doPending(queryImpl);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        shutdown.set(true);

    }

    @Override
    public void close() throws IOException {
        if (shutdown.get()){
            return;
        }
        shuttingDown.set(true);

        while (true){
            try {
                Thread.sleep(100);
                if (shutdown.get()){
                    return;
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
