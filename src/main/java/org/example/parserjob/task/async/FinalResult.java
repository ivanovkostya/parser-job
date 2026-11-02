package org.example.parserjob.task.async;

public class FinalResult {

    private final String id;
    private final String name;
    private final double finalPrice;
    private final String error;

    public FinalResult(String id, String name, double finalPrice, String error) {
        this.id = id;
        this.name = name;
        this.finalPrice = finalPrice;
        this.error = error;
    }

    public static FinalResult ok(String id, String name, double finalPrice) {
        return new FinalResult(id, name, finalPrice, null);
    }

    public static FinalResult fail(String id, String error) {
        return new FinalResult(id, null, 0, error);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getFinalPrice() { return finalPrice; }
    public String getError() { return error; }
    public boolean isOk() { return error == null; }
}