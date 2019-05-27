package pl.todrzywolek.concurrency.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Producer implements Runnable {
    private Buffer _buf;
    private int iterations;

    public Producer(Buffer _buf, int iterations) {
        this._buf = _buf;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        long producerId = Thread.currentThread().getId();
        for (int i = 0; i < iterations; ++i) {
            System.out.println("Producer " + producerId + " is producing " + i + " resource");
            _buf.put(i);
        }

        System.out.println("Producer " + producerId + " completed");
    }
}

class Consumer implements Runnable {
    private Buffer _buf;
    private int iterations;

    public Consumer(Buffer _buf, int iterations) {
        this._buf = _buf;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        long consumerId = Thread.currentThread().getId();
        for (int i = 0; i < iterations; ++i) {
            System.out.println("Consumer " + consumerId + " is consuming " + i + " resource");
            System.out.println(_buf.get());
        }

        System.out.println("Consumer " + consumerId + " completed");
    }
}


class Buffer {
    private int size;
    private List<Integer> buffer = new ArrayList<>();
    private Random rand = new Random();

    public Buffer(int size) {
        this.size = size;
    }

    public synchronized void put(int resource) {
        while (isFull()) {
            try {
                System.out.println("Buffer full. Stopping producing");
                wait();
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Added resource " + resource);
        buffer.add(resource);
        notifyAll();
    }

    public synchronized int get() {
        while (isEmpty()) {
            try {
                System.out.println("Buffer empty. Stopping consuming");
                wait();
            } catch (InterruptedException e) {
            }
        }
        int bufferIndex = rand.nextInt(buffer.size());
        Integer resource = buffer.get(bufferIndex);
        buffer.remove(bufferIndex);
        notifyAll();

        return resource;
    }

    private boolean isFull() {
        return buffer.size() > size;
    }

    private boolean isEmpty() {
        return buffer.size() <= 0;
    }
}

class Main {

    private static int PRODUCERS_NUM;
    private static int CONSUMERS_NUM;
    private static int BUFFER_SIZE;
    private static int PRODUCER_ITERATIONS;
    private static int CONSUMER_ITERATIONS;

    public static void main(String[] args) {
        readConfiguration(args);
        checkConfiguration();

        Buffer buffer = new Buffer(BUFFER_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(PRODUCERS_NUM + CONSUMERS_NUM);

        for (int i = 0; i < PRODUCERS_NUM; i++) {
            executor.submit(new Producer(buffer, PRODUCER_ITERATIONS));
        }

        for (int i = 0; i < CONSUMERS_NUM; i++) {
            executor.submit(new Consumer(buffer, CONSUMER_ITERATIONS));
        }

        executor.shutdown();
    }

    private static void readConfiguration(String[] args) {
        if (args.length != 4) {
            throw new RuntimeException("Invalid number of arguments. 5 args are required");
        }

        PRODUCERS_NUM = Integer.parseInt(args[0]);
        CONSUMERS_NUM = Integer.parseInt(args[1]);
        BUFFER_SIZE = Integer.parseInt(args[2]);
        PRODUCER_ITERATIONS = Integer.parseInt(args[3]);
        CONSUMER_ITERATIONS = PRODUCERS_NUM * PRODUCER_ITERATIONS / CONSUMERS_NUM;
    }

    private static void checkConfiguration() {
        if (PRODUCERS_NUM * PRODUCER_ITERATIONS != CONSUMERS_NUM * CONSUMER_ITERATIONS) {
            throw new RuntimeException("Invalid params. " +
                    "Number of goods produced is not equal to with number of goods consumed.");
        }
    }

}
