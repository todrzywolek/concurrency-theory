package pl.todrzywolek.concurrency.lab5;

import java.util.concurrent.Semaphore;

class MainWithSemaphoreReaderPriority {
    private int readersAmount;
    private int writersAmount;
    public long readerWaitingTime = 0L;
    public long writerWaitingTime = 0L;

    public MainWithSemaphoreReaderPriority(int readersAmount, int writersAmount) {
        this.readersAmount = readersAmount;
        this.writersAmount = writersAmount;
    }

    public static void main(String[] args) {
        Main tmp;
        for (int writersAmount = 1; writersAmount <= 10; ++writersAmount) {
            for (int readersAmount = 10; readersAmount <= 100; readersAmount += 5) {
                tmp = new Main(readersAmount, writersAmount);
                tmp.run();
            }
        }
    }

    public void run() {
        int i;
        final LibraryWithSemaphoreReaderPriority library = new LibraryWithSemaphoreReaderPriority(this);
        ReaderWithSemaphoreReaderPriority[] reader = new ReaderWithSemaphoreReaderPriority[readersAmount];
        WriterWithSemaphoreReaderPriority[] writer = new WriterWithSemaphoreReaderPriority[writersAmount];
        for (i = 0; i < readersAmount; ++i) {
            reader[i] = new ReaderWithSemaphoreReaderPriority(i, library);
            reader[i].start();
        }
        for (i = 0; i < writersAmount; ++i) {
            writer[i] = new WriterWithSemaphoreReaderPriority(i, library);
            writer[i].start();
        }
        for (i = 0; i < readersAmount; ++i) {
            try {
                reader[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (i = 0; i < writersAmount; ++i) {
            try {
                writer[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(String.format("%d %d %d %d",
                readersAmount, writersAmount, readerWaitingTime / readersAmount, writerWaitingTime / writersAmount));
    }

    public long getReaderWaitingTime() {
        return readerWaitingTime;
    }

    public void setReaderWaitingTime(long readerWaitingTime) {
        this.readerWaitingTime = readerWaitingTime;
    }

    public long getWriterWaitingTime() {
        return writerWaitingTime;
    }

    public void setWriterWaitingTime(long writerWaitingTime) {
        this.writerWaitingTime = writerWaitingTime;
    }
}

class LibraryWithSemaphoreReaderPriority {
    private Semaphore write = new Semaphore(1, true); // for updating read count
    private Semaphore mutex = new Semaphore(1, true); // for mutual exclusion of access to library
    private int readCount = 0;
    private MainWithSemaphoreReaderPriority program;

    public LibraryWithSemaphoreReaderPriority(MainWithSemaphoreReaderPriority program) {
        this.program = program;
    }

    public void beginReading() throws InterruptedException {
        long tmp = System.nanoTime();
        mutex.acquire();
        try {
            readCount++;
            if (readCount == 1) {
                write.acquire();
            }
        } finally {
            mutex.release();
        }
        program.setReaderWaitingTime(program.getReaderWaitingTime() + System.nanoTime() - tmp);

        // read
        Thread.sleep(1000);
    }

    public void endReading() throws InterruptedException {
        long tmp = System.nanoTime();
        mutex.acquire();
        program.setReaderWaitingTime(program.getReaderWaitingTime() + System.nanoTime() - tmp);

        try {
            readCount--;
            if (readCount == 0) {
                write.release();

            }
        } finally {
            mutex.release();
        }
    }

    public void beginWriting() throws InterruptedException {
        long tmp = System.nanoTime();
        write.acquire();
        program.setWriterWaitingTime(program.getWriterWaitingTime() + System.nanoTime() - tmp);

        // write
        Thread.sleep(1000);
    }

    public void endWriting() {
        long tmp = System.nanoTime();
        write.release();
        program.setWriterWaitingTime(program.getWriterWaitingTime() + System.nanoTime() - tmp);
    }
}

class ReaderWithSemaphoreReaderPriority extends Thread {
    private int nr;
    private LibraryWithSemaphoreReaderPriority library;

    public ReaderWithSemaphoreReaderPriority(int nr, LibraryWithSemaphoreReaderPriority library) {
        this.nr = nr;
        this.library = library;
    }

    @Override
    public void run() {
        int i = 0;
        while (i++ < 1000) {
            try {
                library.beginReading();
                library.endReading();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

class WriterWithSemaphoreReaderPriority extends Thread {
    private int nr;
    private LibraryWithSemaphoreReaderPriority library;

    public WriterWithSemaphoreReaderPriority(int nr, LibraryWithSemaphoreReaderPriority library) {
        super();
        this.nr = nr;
        this.library = library;
    }

    @Override
    public void run() {
        int i = 0;
        while (i++ < 1000) {
            try {
                library.beginWriting();
                library.endWriting();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
