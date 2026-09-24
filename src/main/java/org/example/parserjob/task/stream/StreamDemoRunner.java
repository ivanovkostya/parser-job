package org.example.parserjob.task.stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class StreamDemoRunner implements CommandLineRunner {

    private static final int SIZE = 1_000_000;

    @Override
    public void run(String... args) {
        System.out.println("\n=== Демонстрация stream() vs parallelStream() ===");

        // Создаём список из млн случайных чисел
        Random random = new Random();
        List<Integer> numbers = random.ints(SIZE, 1, 1_000_000)
                .boxed()
                .collect(Collectors.toList());

        System.out.println("Создан список из " + numbers.size() + " чисел");

        // Обычный stream()
        long startSeq = System.nanoTime();
        long sumSeq = numbers.stream()
                .filter(n -> n % 2 == 0)       // только чётные
                .map(n -> n * 2)               // умножение на 2
                .mapToLong(Integer::longValue)
                .sum();                        // сумма
        long timeSeq = System.nanoTime() - startSeq;

        // parallelStream()
        long startPar = System.nanoTime();
        long sumPar = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .mapToLong(Integer::longValue)
                .sum();
        long timePar = System.nanoTime() - startPar;

        // 4. Вывод результатов
        System.out.println("--- Последовательный stream() ---");
        System.out.println("  Сумма: " + sumSeq);
        System.out.println("  Время: " + timeSeq / 1_000_000 + " мс");

        System.out.println("--- Параллельный parallelStream() ---");
        System.out.println("  Сумма: " + sumPar);
        System.out.println("  Время: " + timePar / 1_000_000 + " мс");

        System.out.println("--- Итог ---");
        System.out.println("  Доступно ядер: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  Ускорение: " + String.format("%.2f", (double) timeSeq / timePar) + "x");
    }
}