package org.example.parserjob.service;

import org.springframework.stereotype.Service;

@Service
public class CounterService {

    public void printInfo(String threadName, int number) {
        System.out.println("Поток: " + threadName + ", номер: " + number);
    }
}