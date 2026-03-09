import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveNanos;
    private final int minSpareThreads;
    private final List<TaskQueue> queues;
    private final List<Worker> workers;
    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectedHandler;
    private final AtomicInteger queueIndex = new AtomicInteger(0);
    private volatile boolean shutdown = false;
    private static final Logger logger = Logger.getLogger(CustomThreadPool.class.getName());

    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime,
                            TimeUnit unit, int queueSize, int minSpareThreads,
                            RejectedExecutionHandler rejectedHandler) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveNanos = unit.toNanos(keepAliveTime);
        this.minSpareThreads = minSpareThreads;
        this.threadFactory = new CustomThreadFactory("MyPool");
        this.rejectedHandler = rejectedHandler;

        // Создаем очереди для каждого core потока (Round Robin)
        this.queues = new ArrayList<>();
        for (int i = 0; i < corePoolSize; i++) {
            queues.add(new TaskQueue(i, queueSize));
        }

        this.workers = Collections.synchronizedList(new ArrayList<>());

        // Запускаем core потоки
        for (int i = 0; i < corePoolSize; i++) {
            addWorker(queues.get(i));
        }
    }

    private void addWorker(TaskQueue queue) {
        Worker worker = new Worker(queue, this,
                threadFactory.newThread(() -> {}).getName(), keepAliveNanos);
        workers.add(worker);
        worker.start();
    }

    @Override
    public void execute(Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("Pool is shutdown");
        }

        // Выбираем очередь по алгоритму Round Robin
        int index = queueIndex.getAndIncrement() % queues.size();
        TaskQueue queue = queues.get(Math.abs(index));

        if (queue.offer(command)) {
            logger.info("[Pool] Task accepted into queue #" + queue.getQueueId() +
                    ": " + command.toString());

            // Проверяем необходимость создания дополнительных потоков
            synchronized (this) {
                int activeWorkers = countActiveWorkers();
                int freeWorkers = workers.size() - activeWorkers;

                if (freeWorkers < minSpareThreads && workers.size() < maxPoolSize) {
                    // Создаем новый поток с наименее загруженной очередью
                    TaskQueue leastLoadedQueue = getLeastLoadedQueue();
                    addWorker(leastLoadedQueue);
                    logger.info("[Pool] Creating additional worker due to minSpareThreads");
                }
            }
        } else {
            // Очередь переполнена - применяем политику отказа
            rejectedHandler.rejectedExecution(command, this);
        }
    }

    private TaskQueue getLeastLoadedQueue() {
        return queues.stream()
                .min(Comparator.comparingInt(TaskQueue::size))
                .orElse(queues.get(0));
    }

    private int countActiveWorkers() {
        return (int) workers.stream()
                .filter(Thread::isAlive)
                .count();
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> futureTask = new FutureTask<>(callable);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        logger.info("[Pool] Shutdown initiated");
        shutdown = true;

        for (Worker worker : workers) {
            worker.shutdown();
        }
    }

    @Override
    public void shutdownNow() {
        logger.info("[Pool] ShutdownNow initiated");
        shutdown = true;

        for (Worker worker : workers) {
            worker.shutdown();
            worker.interrupt();
        }

        // Очищаем очереди
        for (TaskQueue queue : queues) {
            queue.clear();
        }
    }

    public boolean isShutdown() {
        return shutdown;
    }

    boolean canWorkerTerminate() {
        return workers.size() > corePoolSize;
    }

    void workerTerminated(Worker worker) {
        workers.remove(worker);
        logger.info("[Pool] Worker removed, current size: " + workers.size());
    }

    public int getActiveCount() {
        return countActiveWorkers();
    }

    public int getPoolSize() {
        return workers.size();
    }

    public int getQueueSize() {
        return queues.stream().mapToInt(TaskQueue::size).sum();
    }
}