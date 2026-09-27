package org.example.parserjob.task.buffer;

public class Producer implements Runnable {

    private final BoundedBuffer<Integer> buffer;
    private final int count;
    private final int id;

    public Producer(BoundedBuffer<Integer> buffer, int count, int id) {
        this.buffer = buffer;
        this.count = count;
        this.id = id;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < count; i++) {
                buffer.put(id * 1000 + i);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}