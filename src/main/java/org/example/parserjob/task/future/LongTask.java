package org.example.parserjob.task.future;

import java.util.concurrent.Callable;

public class LongTask implements Callable<String> {

    private final long durationMs;

    public LongTask(long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String call() throws Exception {
        long end = System.currentTimeMillis() + durationMs;
        while (System.currentTimeMillis() < end) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("задача прервана");
            }
            Thread.sleep(100);
        }
        return "выполнено за " + durationMs + " мс";
    }
}