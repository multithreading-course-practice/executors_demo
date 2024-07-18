package org.example.stage1;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

/**
 * Симулируем проблему с нехваткой ресурсов
 * ограничиваем размер доступной памяти в JVM options: -Xms100M -Xmx100M
 * Каждый поток выделяет 25мбайт на каждый поток
 */
@Log4j2
public class ThreadPerTask {
    static final int TASK_COUNT = 10;
    static final CountDownLatch LATCH = new CountDownLatch(TASK_COUNT);

    public static void main(String[] args) {
        for (int i = 0; i < TASK_COUNT; i++) {
            try{
                new Thread(new Task(), "Обработчик-"+i).start();
            }catch (Throwable e){
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
    }
}

@Log4j2
class Task implements Runnable {

    private final byte[] data = new byte[1024*1024*25];

    @Override
    public void run() {
        try {
            log.info("Запуск");
            sleep(10_000);
            ThreadPerTask.LATCH.countDown();
            log.info("Завершение");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
