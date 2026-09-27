package org.example.parserjob.task.sync;

import java.util.*;

public class UnsafeCollector {
    private final List<Item> items = new ArrayList<>();
    private int count = 0;

    public void collectItem(Item item) { items.add(item); }
    public void incrementProcessed() { count++; }
    public int getProcessedCount() { return count; }
    public int getItemsSize() { return items.size(); }
}