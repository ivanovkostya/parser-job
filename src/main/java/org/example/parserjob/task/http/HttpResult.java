package org.example.parserjob.task.http;

public class HttpResult {

    private final String url;
    private final int status;
    private final long timeMs;
    private final String error;

    public HttpResult(String url, int status, long timeMs, String error) {
        this.url = url;
        this.status = status;
        this.timeMs = timeMs;
        this.error = error;
    }

    public static HttpResult ok(String url, int status, long timeMs) {
        return new HttpResult(url, status, timeMs, null);
    }

    public static HttpResult fail(String url, long timeMs, String error) {
        return new HttpResult(url, -1, timeMs, error);
    }

    public String getUrl() { return url; }
    public int getStatus() { return status; }
    public long getTimeMs() { return timeMs; }
    public String getError() { return error; }
    public boolean isOk() { return error == null; }
}