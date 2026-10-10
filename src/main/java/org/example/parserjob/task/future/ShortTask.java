package org.example.parserjob.task.future;

import java.util.concurrent.Callable;

public class ShortTask implements Callable<Integer> {

    private final int id;
    private final long durationMs;

    public ShortTask(int id, long durationMs) {
        this.id = id;
        this.durationMs = durationMs;
    }

    @Override
    public Integer call() throws Exception {
        Thread.sleep(durationMs);
        return id * 100;
    }
}