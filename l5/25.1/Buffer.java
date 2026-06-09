import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Buffer {
    private final List<Integer> data = new ArrayList<>();

    private final Semaphore empty = new Semaphore(100);
    private final Semaphore full = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    private final int totalItems;
    private final int numConsumers;
    private int consumedItems = 0;

    public Buffer(int totalItems, int numConsumers) {
        this.totalItems = totalItems;
        this.numConsumers = numConsumers;
    }

    public void put(int value) throws InterruptedException {
        empty.acquire();
        mutex.acquire();

        data.add(value);
        System.out.println("Inserted: " + value + " | Buffer size: " + data.size());

        mutex.release();
        full.release();
    }

    public int remove(boolean consumeEven) throws InterruptedException {
        while (true) {
            full.acquire();
            mutex.acquire();

            if (consumedItems >= totalItems) {
                mutex.release();
                return -1;
            }

            int value = data.remove(0);
            boolean isEven = value % 2 == 0;

            if (isEven == consumeEven) {
                consumedItems++;

                System.out.println("Removed: " + value + " | Buffer size: " + data.size());

                empty.release();

                if (consumedItems == totalItems) {
                    for (int i = 0; i < numConsumers; i++) {
                        full.release();
                    }
                }

                mutex.release();
                return value;
            }

            data.add(value);
            System.out.println("Reinserted: " + value + " | Buffer size: " + data.size());

            mutex.release();
            full.release();
        }
    }
}
