class Consumer extends Thread {
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final boolean consumeEven;
    
    public Consumer(int id, Buffer buffer, int sleepTime, boolean consumeEven) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.consumeEven = consumeEven;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                int item = buffer.remove(consumeEven);

                if (item == -1) break;

                System.out.println("Consumer " + id + " consumed item " + item);

                Thread.sleep(sleepTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}