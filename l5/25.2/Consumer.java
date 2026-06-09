class Consumer extends Thread {

    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final int totalItems;

    public Consumer(int id,
                    Buffer buffer,
                    int sleepTime,
                    int totalItems) {

        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.totalItems = totalItems;
    }

    @Override
    public void run() {

        for (int i = 0; i < totalItems; i++) {

            try {

                int item = buffer.remove();

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