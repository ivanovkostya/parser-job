package org.example.parserjob.controller;

import org.example.parserjob.task.deadlock.DeadlockDemo;
import org.example.parserjob.task.deadlock.LivelockDemo;
import org.example.parserjob.task.deadlock.StarvationDemo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeadlockController {

    // 1. Запускает deadlock (приложение зависнет — процесс нужно убить)
    @GetMapping("/deadlock")
    public Map<String, Object> deadlock() {
        new DeadlockDemo().startDeadlock();
        return Map.of("status", "deadlock started — снимайте jstack, потом убивайте процесс");
    }

    // 2. Livelock
    @GetMapping("/livelock")
    public Map<String, Object> livelock() {
        new LivelockDemo().startLivelock();
        return Map.of("status", "livelock started — потоки уступают друг другу бесконечно");
    }

    // 3. Starvation
    @GetMapping("/starvation")
    public Map<String, Object> starvation() throws InterruptedException {
        StarvationDemo demo = new StarvationDemo();
        demo.startStarvation();
        Thread.sleep(2000);  // даём поработать 2 секунды
        return Map.of(
                "hogCount", demo.getHogCount(),
                "starvingCount", demo.getStarvingCount(),
                "note", "у starvingCount будет намного меньше операций"
        );
    }
}