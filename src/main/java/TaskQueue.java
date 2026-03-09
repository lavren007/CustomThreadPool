import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TaskQueue extends LinkedBlockingQueue<Runnable> {
    private final int queueId;
    private static final Logger logger = Logger.getLogger(TaskQueue.class.getName());

    public TaskQueue(int queueId, int capacity) {
        super(capacity);
        this.queueId = queueId;
    }

    @Override
    public boolean offer(Runnable task) {
        boolean accepted = super.offer(task);
        if (accepted) {
            logger.info("[Queue-" + queueId + "] Task accepted: " + task.toString());
        }
        return accepted;
    }

    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        Runnable task = super.poll(timeout, unit);
        if (task != null) {
            logger.info("[Queue-" + queueId + "] Task retrieved: " + task.toString());
        }
        return task;
    }

    public int getQueueId() {
        return queueId;
    }
}