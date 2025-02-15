package comfaas.theAlgoTools;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryDataCollector {
    // Fixed-size array to hold up to 5 values.
    private final double[] values = new double[5];
    // Points to the index where the next value will be written.
    private int currentIndex = 0;
    // Keeps track of how many valid values have been collected for the first fill.
    // Once count reaches the length of the array, it will remain at that value.
    private int count = 0;
    // Flag to indicate that the buffer has been filled at least once.
    private boolean bufferFilled = false;
    // Lock to protect concurrent access to the circular buffer.
    private final ReentrantLock lock = new ReentrantLock();
    // Condition that is signaled when the buffer is initially filled.
    private final Condition filledCondition = lock.newCondition();
    // Scheduler for automatically calling collect() every second.
    private ScheduledExecutorService scheduler;

    /**
     * Calls OtherClass.function(), retrieves a double value, and adds it to the circular buffer.
     * Once the buffer is filled the first time, the flag 'bufferFilled' remains true.
     */
    public void collect() {
        // Assume OtherClass.function() is a static function that returns a double.
        double newValue = MemoryUtility.getFreeMemoryInMB() ;

        lock.lock();
        try {
            values[currentIndex] = newValue;
            currentIndex = (currentIndex + 1) % values.length;
            // Increase count only if we haven't filled the buffer yet.
            if (!bufferFilled) {
                count++;
                if (count == values.length) {
                    bufferFilled = true;
                    filledCondition.signalAll();
                }
            }
            // System.out.printf("Collected value: %.2f%n", newValue);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Computes the average of the stored values.
     * This method will wait (block) until the circular buffer has been filled at least once.
     *
     * @return The average of the collected values.
     */
    public double average() {
        lock.lock();
        try {
            // Wait until the buffer has been filled at least once.
            while (!bufferFilled) {
                try {
                    filledCondition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0;
                }
            }
            double sum = 0;
            for (double value : values) {
                sum += value;
            }
            return sum / values.length;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Starts a scheduled task that calls collect() once every second.
     */
    public void startAutoCollect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::collect, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Stops the auto-collection thread gracefully.
     */
    public void stopAutoCollect() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Example usage:
    public static void main(String[] args) {
        MemoryDataCollector collector = new MemoryDataCollector();
        collector.startAutoCollect();

        // Continuously print the average every second.
        // Note: The average() call will block until the first 5 values have been collected.
        ScheduledExecutorService printer = Executors.newSingleThreadScheduledExecutor();
        printer.scheduleAtFixedRate(() -> {
            double avg = collector.average();
            System.out.printf("Current average: %.2f%n", avg);
        }, 0, 1, TimeUnit.SECONDS);

        // Let the program run for 15 seconds before shutting down.
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        collector.stopAutoCollect();
        printer.shutdownNow();
    }
}
