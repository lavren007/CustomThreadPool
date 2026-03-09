import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Worker extends Thread {
    private final TaskQueue queue;
    private final CustomThreadPool pool;
    private volatile boolean running = true;
    private final long keepAliveNanos;
    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    public Worker(TaskQueue queue, CustomThreadPool pool, String name, long keepAliveNanos) {
        super(name);
        this.queue = queue;
        this.pool = pool;
        this.keepAliveNanos = keepAliveNanos;
    }

    @Override
    public void run() {
        logger.info("[Worker] " + getName() + " started");

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Runnable task = queue.poll(keepAliveNanos, TimeUnit.NANOSECONDS);

                if (task == null) {
                    // Таймаут - поток простаивал слишком долго
                    if (pool.canWorkerTerminate()) {
                        logger.info("[Worker] " + getName() + " idle timeout, stopping.");
                        break;
                    }
                    continue;
                }

                if (running && !pool.isShutdown()) {
                    try {
                        logger.info("[Worker] " + getName() + " executes " + task.toString());
                        task.run();
                        logger.info("[Worker] " + getName() + " completed " + task.toString());
                    } catch (Exception e) {
                        logger.severe("[Worker] " + getName() + " error: " + e.getMessage());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("[Worker] " + getName() + " terminated.");
        pool.workerTerminated(this);
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}