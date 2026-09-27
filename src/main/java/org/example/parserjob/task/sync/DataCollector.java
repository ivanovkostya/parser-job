package org.example.parserjob.task.sync;

import java.util.*;

public class DataCollector {
    private final List<Item> items = new ArrayList<>();
    private final Set<String> processedKeys = new HashSet<>();
    private int processedCount = 0;

    public synchronized void collectItem(Item item) {
        items.add(item);
        processedKeys.add(item.getKey());
        notifyAll();
    }

    public synchronized void incrementProcessed() { processedCount++; }
    public synchronized boolean isAlreadyProcessed(String key) { return processedKeys.contains(key); }
    public synchronized int getProcessedCount() { return processedCount; }
    public synchronized int getItemsSize() { return items.size(); }

    public synchronized void waitForItems(int minCount) throws InterruptedException {
        while (items.size() < minCount) wait();
    }
}