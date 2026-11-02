package org.example.parserjob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AsyncExecutorConfig {

    @Bean(name = "asyncExecutor", destroyMethod = "shutdown")
    public ExecutorService asyncExecutor() {
        AtomicInteger counter = new AtomicInteger(1);

        return new ThreadPoolExecutor(
                4, 8,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> {
                    Thread t = new Thread(r, "async-worker-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}