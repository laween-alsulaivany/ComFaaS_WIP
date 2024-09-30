import java.util.concurrent.locks.ReentrantLock;

public class MutexExample {
    private static final ReentrantLock lock = new ReentrantLock();
    private static int count = 0;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                increment();
            }
        });

        t1.start();
        t2.start();
    }

    private static void increment() {
        lock.lock();
        try {
            count++;
            System.out.println(Thread.currentThread().getName() + ": Count is " + count);
        } finally {
            lock.unlock();
        }
    }
}
