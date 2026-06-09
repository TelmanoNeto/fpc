public class Main {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_consumers> <consuming_time>");
            return;
        }
        
        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);

        int totalItems = numProducers * maxItemsPerProducer;

        Buffer buffer = new Buffer(totalItems, numConsumers);

        Producer[] producers = new Producer[numProducers];
        Consumer[] consumers = new Consumer[numConsumers];
        
        for (int i = 0; i < numProducers; i++) {
            producers[i] = new Producer(i + 1, buffer, maxItemsPerProducer, producingTime);
            producers[i].start();
        }
        
        for (int i = 0; i < numConsumers; i++) {
            boolean consumeEven = (i % 2 == 0);

            consumers[i] = new Consumer(i + 1, buffer, consumingTime, consumeEven);
            consumers[i].start();
        }

        try {
            for (Producer producer : producers) {
                producer.join();
            }

            for (Consumer consumer : consumers) {
                consumer.join();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}