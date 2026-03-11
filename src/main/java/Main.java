
import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static class TestTask implements Runnable {
        private final int taskId;
        private final long duration;
        private static final AtomicInteger counter = new AtomicInteger(0);

        public TestTask(long duration) {
            this.taskId = counter.incrementAndGet();
            this.duration = duration;
        }

        @Override
        public void run() {
            try {
                logger.info("[Task-" + taskId + "] Started (duration: " + duration + "ms)");
                Thread.sleep(duration);
                logger.info("[Task-" + taskId + "] Completed");
            } catch (InterruptedException e) {
                logger.info("[Task-" + taskId + "] Interrupted");
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String toString() {
            return "Task-" + taskId;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Настройка логирования для более чистого вывода
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + System.lineSeparator();
            }
        });
        rootLogger.addHandler(consoleHandler);
        rootLogger.setLevel(Level.ALL);

        logger.info("=== Starting Custom ThreadPool Demonstration ===");

        // Создаем пул с параметрами
        // corePoolSize=2, maxPoolSize=4, keepAliveTime=5s, queueSize=2, minSpareThreads=1
        CustomThreadPool pool = new CustomThreadPool(
                2, 4, 5, TimeUnit.SECONDS, 2, 1,
                new CallerRunsPolicy()  // Используем CallerRunsPolicy для демонстрации
               // new AbortPolicy()
               // new DiscardPolicy()
        );

        logger.info("Pool created: core=2, max=4, queueSize=2, minSpare=1");

        // Отправляем 10 задач с разной длительностью
        logger.info("\n=== Submitting 10 tasks ===");
        for (int i = 0; i < 10; i++) {
            long duration = (i % 3 + 1) * 1000; // 1-3 секунды
            pool.execute(new TestTask(duration));
            Thread.sleep(100); // Небольшая задержка между отправками
        }

        // Показываем состояние пула
        logger.info("\n=== Pool Status ===");
        logger.info("Active threads: " + pool.getActiveCount());
        logger.info("Total threads: " + pool.getPoolSize());
        logger.info("Queued tasks: " + pool.getQueueSize());

        // Ждем завершения задач
        Thread.sleep(8000);

        logger.info("\n=== Shutting down pool ===");
        pool.shutdown();

        // Пробуем отправить задачу после shutdown
        try {
            pool.execute(new TestTask(100));
        } catch (RejectedExecutionException e) {
            logger.info("[Expected] Cannot submit task after shutdown: " + e.getMessage());
        }

        logger.info("\n=== Demonstration Complete ===");
    }
}