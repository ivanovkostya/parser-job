package org.example.parserjob.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "httpExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor httpExecutor() {
        AtomicInteger counter = new AtomicInteger(1);

        return new ThreadPoolExecutor(
                4,                              // corePoolSize
                8,                              // maximumPoolSize
                60L, TimeUnit.SECONDS,          // keepAliveTime
                new LinkedBlockingQueue<>(100), // очередь задач
                r -> new Thread(r, "http-worker-" + counter.getAndIncrement()),
                new ThreadPoolExecutor.CallerRunsPolicy()  // что делать при переполнении
        );
    }
}