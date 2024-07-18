package org.example.stage2;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

/**
 * Симулируем проблему с нехваткой ресурсов
 * ограничиваем размер доступной памяти в JVM options: -Xms100M -Xmx100M
 * Каждый поток выделяет 25мбайт на каждый поток
 */
@Log4j2
public class FixedThredPoolExample {
    static final int TASK_COUNT = 10;
    static final CountDownLatch LATCH = new CountDownLatch(TASK_COUNT);

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new PayloadThread(r);
            }
        });


        for (int i = 0; i < TASK_COUNT; i++) {
            try {
                executor.submit(new Task());
            } catch (Throwable e) {
                log.error("Не удалось создать поток", e);
                exit(1);
            }
        }

        try {
            log.info("Ожидание задач");
            LATCH.await();
            log.info("Все задачи выполнены");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();
    }
}

@Log4j2
class Task implements Runnable {
    @Override
    public void run() {
        try {
            log.info("Запуск");
            sleep(1_000);
            FixedThredPoolExample.LATCH.countDown();
            log.info("Завершение");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class PayloadThread extends Thread {
    private final byte[] data = new byte[1024 * 1024 * 25];

    public PayloadThread(Runnable r) {
        super(r);
    }
}

