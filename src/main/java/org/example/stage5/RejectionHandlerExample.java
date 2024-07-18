package org.example.stage5;


import lombok.extern.log4j.Log4j2;

import java.util.concurrent.*;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Симулируем генерацию задач с заданной частотой
 */
@Log4j2
public class RejectionHandlerExample {

    static final int TASK_RATE = 1;
    static final int GENERATOR_COUNT = 3;
    public static final RejectedExecutionHandler REJECTED_HANDLER[] = new RejectedExecutionHandler[]{
            new ThreadPoolExecutor.AbortPolicy(),
            new ThreadPoolExecutor.DiscardPolicy(),
            new ThreadPoolExecutor.DiscardOldestPolicy(),
            new ThreadPoolExecutor.CallerRunsPolicy(),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    log.error("Задача отклонена {}", r);
                }
            }
    };

    public static final int USED_POLICY = 4;

    public static void main(String[] args) {
        ExecutorService executor = new ThreadPoolExecutor(
                1,
                1,
                0, NANOSECONDS,
                new ArrayBlockingQueue<>(1),
                REJECTED_HANDLER[USED_POLICY]
        );


        for (int i = 0; i < GENERATOR_COUNT; i++) {
            final int genId = i;
            new Thread(() -> {
                int id = 0;
                while (true) {
                    String taskId = String.format("%d_%d", genId, id++);
                    try {
                        Thread.sleep(1000 / TASK_RATE);
                        Future<?> future = executor.submit(new Task(taskId));
                        log.info("Создание {}", taskId);
                        // if (USED_POLICY == 3 || USED_POLICY == 1) {
                        //future.get(500, TimeUnit.MILLISECONDS);

                        future.cancel(false);
                        //}
                    }/*catch (TimeoutException exception){
                        log.error("Не дождались результата задачи {}", taskId, exception);
                    }*/

                    catch (RejectedExecutionException rejectedExecutionException) {
                        log.error("Задача отклонена {}", taskId);
                    } catch (Throwable e) {
                        log.error("Не удалось создать поток", e);
                        exit(1);
                    }
                }
            }, "Генератор " + i).start();
        }

    }
}

@Log4j2
class Task implements Runnable {

    final String id;

    public Task(String id) {
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


//class Example extends Runnable{
//
//    private Object object = new Object();
//    @Override
//    public void run() {
//        synchronized (object){
//            try {
//                object.wait();
//
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//}


