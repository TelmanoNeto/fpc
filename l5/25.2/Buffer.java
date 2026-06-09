import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Buffer {

    private final List<Integer> data = new ArrayList<>();

    private final Semaphore empty = new Semaphore(50);
    private final Semaphore full = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    public void put(int value) throws InterruptedException {

        empty.acquire();
        mutex.acquire();

        data.add(value);
        System.out.println("Inserted: " + value +
                           " | Buffer size: " + data.size());

        mutex.release();
        full.release();
    }

    public int remove() throws InterruptedException {

        full.acquire();
        mutex.acquire();

        int value = data.remove(0);

        System.out.println("Removed: " + value +
                           " | Buffer size: " + data.size());

        mutex.release();
        empty.release();

        return value;
    }
}