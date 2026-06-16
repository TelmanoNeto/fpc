// Buffer.java

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

class Buffer {

    private final List<Integer> data = new ArrayList<>();

    private final Semaphore empty = new Semaphore(100);
    private final Semaphore full = new Semaphore(0);
    private final Semaphore mutex = new Semaphore(1);

    private int activeProducers;

    public Buffer(int numProducers) {
        this.activeProducers = numProducers;
    }

    public void put(int value) throws InterruptedException {

        empty.acquire();
        mutex.acquire();

        data.add(value);

        System.out.println(
                "Inserted: " + value +
                " | Buffer size: " + data.size()
        );

        mutex.release();
        full.release();
    }

    public void producerFinished() {

        try {

            mutex.acquire();

            activeProducers--;

            if (activeProducers == 0) {

                // acorda consumidores bloqueados
                for (int i = 0; i < 10; i++) {
                    full.release();
                }
            }

            mutex.release();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int remove(boolean consumeEven)
            throws InterruptedException {

        while (true) {

            full.acquire();
            mutex.acquire();

            int index = -1;

            for (int i = 0; i < data.size(); i++) {

                int value = data.get(i);

                if ((value % 2 == 0) == consumeEven) {
                    index = i;
                    break;
                }
            }

            // encontrou item compatível
            if (index != -1) {

                int value = data.remove(index);

                System.out.println(
                        "Removed: " + value +
                        " | Buffer size: " + data.size()
                );

                mutex.release();
                empty.release();

                return value;
            }

            // não encontrou item compatível
            // e nenhum produtor vai inserir mais nada
            if (activeProducers == 0) {

                mutex.release();
                full.release();

                return -1;
            }

            mutex.release();

            // devolve o permit
            full.release();

            Thread.yield();
        }
    }
}


// Producer.java

class Producer extends Thread {

    private final Buffer buffer;
    private final int maxItems;
    private final int sleepTime;
    private final int id;

    public Producer(
            int id,
            Buffer buffer,
            int maxItems,
            int sleepTime) {

        this.id = id;
        this.buffer = buffer;
        this.maxItems = maxItems;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {

        for (int i = 0; i < maxItems; i++) {

            try {

                Thread.sleep(sleepTime);

                int item =
                        (int) (Math.random() * 100);

                System.out.println(
                        "Producer " + id +
                        " produced item " + item
                );

                buffer.put(item);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        buffer.producerFinished();
    }
}


// Consumer.java

class Consumer extends Thread {

    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final boolean consumeEven;

    public Consumer(
            int id,
            Buffer buffer,
            int sleepTime,
            boolean consumeEven) {

        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.consumeEven = consumeEven;
    }

    @Override
    public void run() {

        while (true) {

            try {

                int item =
                        buffer.remove(consumeEven);

                if (item == -1)
                    break;

                System.out.println(
                        "Consumer " + id +
                        " consumed item " + item
                );

                Thread.sleep(sleepTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}


// Main.java

public class Main {

    public static void main(String[] args) {

        if (args.length != 5) {

            System.out.println(
                    "Use: java Main " +
                    "<num_producers> " +
                    "<max_items_per_producer> " +
                    "<producing_time> " +
                    "<num_consumers> " +
                    "<consuming_time>"
            );

            return;
        }

        int numProducers =
                Integer.parseInt(args[0]);

        int maxItemsPerProducer =
                Integer.parseInt(args[1]);

        int producingTime =
                Integer.parseInt(args[2]);

        int numConsumers =
                Integer.parseInt(args[3]);

        int consumingTime =
                Integer.parseInt(args[4]);

        Buffer buffer =
                new Buffer(numProducers);

        Producer[] producers =
                new Producer[numProducers];

        Consumer[] consumers =
                new Consumer[numConsumers];

        for (int i = 0; i < numProducers; i++) {

            producers[i] =
                    new Producer(
                            i + 1,
                            buffer,
                            maxItemsPerProducer,
                            producingTime
                    );

            producers[i].start();
        }

        for (int i = 0; i < numConsumers; i++) {

            consumers[i] =
                    new Consumer(
                            i + 1,
                            buffer,
                            consumingTime,
                            (i % 2 == 0)
                    );

            consumers[i].start();
        }

        try {

            for (Producer p : producers)
                p.join();

            for (Consumer c : consumers)
                c.join();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Execution finished.");
    }
}