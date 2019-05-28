package pl.todrzywolek.concurrency.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SemProducer implements Runnable {
    private SemBuffer _buf;
    private int iterations;

    public SemProducer(SemBuffer _buf, int iterations) {
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

class SemConsumer implements Runnable {
    private SemBuffer _buf;
    private int iterations;

    public SemConsumer(SemBuffer _buf, int iterations) {
        this._buf = _buf;
        this.iterations = iterations;
    }

    @Override
    public void run() {
        long consumerId = Thread.currentThread().getId();
        for (int i = 0; i < iterations; ++i) {
            System.out.println(_buf.get());
            System.out.println("Consumer " + consumerId + " consumed " + i + " resource");
        }

        System.out.println("Consumer " + consumerId + " completed");
    }
}


class SemBuffer {
    private int size;
    private List<Integer> buffer = new ArrayList<>();
    private Random rand = new Random();
    private ProdConsSemaphore bufferLockSemaphore;
    private ProdConsSemaphore bufferFull;
    private ProdConsSemaphore bufferEmpty;

    public SemBuffer(int size) {
        this.size = size;
        this.bufferLockSemaphore = new ProdConsSemaphore(1);
        this.bufferFull = new ProdConsSemaphore(size);
        this.bufferEmpty = new ProdConsSemaphore(0);
    }

    public void put(int resource) {
        if (buffer.size() >= size) {
            System.out.println("Buffer full. Producer is waiting");
        }
        try {
            bufferFull.down();
            bufferLockSemaphore.down();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        buffer.add(resource);
        System.out.println("Added resource " + resource);
        System.out.println("Buffer size= " + buffer.size());
        bufferEmpty.up();
        bufferLockSemaphore.up();
    }

    public int get() {
        if (buffer.size() <= 0) {
            System.out.println("Buffer empty. Consumer is waiting");
        }
        try {
            bufferEmpty.down();
            bufferLockSemaphore.down();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int bufferIndex = rand.nextInt(buffer.size());
        Integer resource = buffer.get(bufferIndex);
        buffer.remove(bufferIndex);
        bufferFull.up();
        bufferLockSemaphore.up();

        return resource;
    }
}

class SemMain {

    private static int PRODUCERS_NUM;
    private static int CONSUMERS_NUM;
    private static int BUFFER_SIZE;
    private static int PRODUCER_ITERATIONS;
    private static int CONSUMER_ITERATIONS;

    public static void main(String[] args) {
        readConfiguration(args);
        checkConfiguration();

        SemBuffer buffer = new SemBuffer(BUFFER_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(PRODUCERS_NUM + CONSUMERS_NUM);

        for (int i = 0; i < PRODUCERS_NUM; i++) {
            executor.submit(new SemProducer(buffer, PRODUCER_ITERATIONS));
        }

        for (int i = 0; i < CONSUMERS_NUM; i++) {
            executor.submit(new SemConsumer(buffer, CONSUMER_ITERATIONS));
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
