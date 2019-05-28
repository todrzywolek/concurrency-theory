package pl.todrzywolek.concurrency.lab4;

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
            System.out.println("Producer " + producerId + " iteration no " + i);
            try {
                _buf.put(i);
            } catch (NoConsumersException e) {
                System.out.println("No consumers exist. Forced to stop production");
                break;
            }
        }

        System.out.println("Producer " + producerId + " completed");

        synchronized (this) {
            _buf.setPRODUCERS(_buf.getPRODUCERS() - 1);
        }

        System.out.println("Produced total = " + Buffer.PRODUCED_TOTAL);
        System.out.println("Consumed total = " + Buffer.CONSUMED_TOTAL);
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
            try {
                System.out.println("Consumer iteration number " + i);
                _buf.get();
            } catch (NoProducersException e) {
                System.out.println("No producers exist. Forced to stop consumption");
                break;
            }
        }

        System.out.println("Consumer " + consumerId + " completed");
        synchronized (this) {
            _buf.setCONSUMERS(_buf.getCONSUMERS() - 1);
        }

        System.out.println("Produced total = " + Buffer.PRODUCED_TOTAL);
        System.out.println("Consumed total = " + Buffer.CONSUMED_TOTAL);
    }
}


class Buffer {
    private int size;
    private List<Integer> buffer = new ArrayList<>();
    private Random rand = new Random();
    private int CONSUMERS;
    private int PRODUCERS;
    public static int CONSUMED_TOTAL = 0;
    public static int PRODUCED_TOTAL = 0;

    public Buffer(int size, int consumers, int producers) {
        this.size = size;
        CONSUMERS = consumers;
        PRODUCERS = producers;
    }

    public synchronized void put(int resource) throws NoConsumersException {
        int numOfResources = rand.nextInt(size / 2) + 1;
        System.out.println("Producer " + Thread.currentThread().getId() + " is putting " + numOfResources + " resources");

        try {
            for (int j = 0; j < numOfResources; j++) {
                putResource(resource + j);
            }
        } finally {
            notifyAll();
        }
    }

    private void putResource(int resource) throws NoConsumersException {
        while (isFull()) {
            if (CONSUMERS > 0) {
                try {
                    System.out.println("Buffer full. Stopping producing");
                    wait();
                } catch (InterruptedException e) {
                }
            } else {
                throw new NoConsumersException();
            }

        }
        System.out.println("Added resource " + resource);
        buffer.add(resource);
        PRODUCED_TOTAL++;
    }

    public synchronized void get() throws NoProducersException {
        int numOfResources = rand.nextInt(size / 2) + 1;
        System.out.println("Consumer " + Thread.currentThread().getId() + " is consuming " + numOfResources + " resources");

        try {
            for (int j = 0; j < numOfResources; j++) {
                System.out.println(consumeResource());
            }
        } finally {
            notifyAll();
        }

    }

    private Integer consumeResource() throws NoProducersException {
        while (isEmpty()) {
            if (PRODUCERS > 0) {
                try {
                    System.out.println("Buffer empty. Stopping consuming");
                    wait();
                } catch (InterruptedException e) {
                }
            } else {
                throw new NoProducersException();
            }
        }
        int bufferIndex = rand.nextInt(buffer.size());
        Integer resource = buffer.get(bufferIndex);
        buffer.remove(bufferIndex);
        CONSUMED_TOTAL++;

        return resource;
    }

    private boolean isFull() {
        return buffer.size() > size;
    }

    private boolean isEmpty() {
        return buffer.size() <= 0;
    }

    public int getCONSUMERS() {
        return CONSUMERS;
    }

    public void setCONSUMERS(int CONSUMERS) {
        this.CONSUMERS = CONSUMERS;
    }

    public int getPRODUCERS() {
        return PRODUCERS;
    }

    public void setPRODUCERS(int PRODUCERS) {
        this.PRODUCERS = PRODUCERS;
    }
}

class NoProducersException extends Exception {
}

class NoConsumersException extends Exception {
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

        Buffer buffer = new Buffer(BUFFER_SIZE, CONSUMERS_NUM, PRODUCERS_NUM);

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
            throw new RuntimeException("Invalid number of arguments. 4 args are required");
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

