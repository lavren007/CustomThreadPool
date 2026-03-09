import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

@FunctionalInterface
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, CustomThreadPool executor);
}

class AbortPolicy implements RejectedExecutionHandler {
    private static final Logger logger = Logger.getLogger(AbortPolicy.class.getName());

    @Override
    public void rejectedExecution(Runnable r, CustomThreadPool executor) {
        logger.severe("[Rejected] Task " + r.toString() + " was rejected due to overload!");
        throw new RejectedExecutionException("Task rejected from " + executor.toString());
    }
}

class CallerRunsPolicy implements RejectedExecutionHandler {
    private static final Logger logger = Logger.getLogger(CallerRunsPolicy.class.getName());

    @Override
    public void rejectedExecution(Runnable r, CustomThreadPool executor) {
        logger.warning("[Rejected] Task " + r.toString() + " will run in caller thread");
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}

class DiscardPolicy implements RejectedExecutionHandler {
    private static final Logger logger = Logger.getLogger(DiscardPolicy.class.getName());

    @Override
    public void rejectedExecution(Runnable r, CustomThreadPool executor) {
        logger.warning("[Rejected] Task " + r.toString() + " was discarded");
        // Просто игнорируем задачу
    }
}