package org.example.stage4;


import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

/**
 * Симулируем генерацию задач с заданной частотой
 */
@Log4j2
public class SingleThreadPoolExample {

    static final int TASK_RATE = 30;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        int id = 0;
        while (true) {
            try {
                Thread.sleep(1000 / TASK_RATE);
                executor.submit(new Task(id));
                log.info("Создан {}", id++);
            } catch (Throwable e) {
                log.error("Не удалось создать поток", e);
                exit(1);
            }
        }
    }
}

@Log4j2
class Task implements Runnable {

    final int id;

    public Task(int id) {
        this.id = id;
    }

    @Override
    public void run() {

        try {
            log.info("Запуск {}", id);
            sleep(1_000);
            log.info("Завершение {}", id);
        } catch (InterruptedException e) {
            log.error("Ошибка", e);
            throw new RuntimeException(e);
        }
    }
}


