import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class CustomThreadFactory implements java.util.concurrent.ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private static final Logger logger = Logger.getLogger(CustomThreadFactory.class.getName());

    public CustomThreadFactory(String poolName) {
        this.namePrefix = poolName + "-worker-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        logger.info("[ThreadFactory] Creating new thread: " + thread.getName());
        thread.setUncaughtExceptionHandler((t, e) ->
                logger.severe("[Thread] " + t.getName() + " crashed: " + e.getMessage()));
        return thread;
    }
}