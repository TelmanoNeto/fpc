public class Main {

    public static void main(String[] args) {

        if (args.length != 5) {
            System.out.println(
                "Use: java Main <num_producers> " +
                "<max_items_per_producer> " +
                "<producing_time> " +
                "<num_consumers> " +
                "<consuming_time>"
            );
            return;
        }

        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);

        Buffer buffer = new Buffer();

        Producer[] producers = new Producer[numProducers];
        Consumer[] consumers = new Consumer[numConsumers];

        int totalItems =
            (numProducers * maxItemsPerProducer)
            / numConsumers;

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
                    totalItems
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
    }
}


