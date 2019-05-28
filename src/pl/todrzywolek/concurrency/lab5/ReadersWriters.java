package pl.todrzywolek.concurrency.lab5;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Main {
    private int readersAmount;
    private int writersAmount;
    public long readerWaitingTime = 0L;
    public long writerWaitingTime = 0L;

    public Main(int readersAmount, int writersAmount) {
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
        final Library library = new Library(this);
        Reader[] reader = new Reader[readersAmount];
        Writer[] writer = new Writer[writersAmount];
        for (i = 0; i < readersAmount; ++i) {
            reader[i] = new Reader(i, library);
            reader[i].start();
        }
        for (i = 0; i < writersAmount; ++i) {
            writer[i] = new Writer(i, library);
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

class Library {
    private Lock libraryLock = new ReentrantLock();
    private Condition readers = libraryLock.newCondition();
    private Condition writers = libraryLock.newCondition();
    private int isReading = 0;
    private int isWriting = 0;
    private int writerWaiting = 0;
    private int readerWaiting = 0;
    private Main program;

    public Library(Main program) {
        this.program = program;
    }

    public void beginReading() {
        long tmp = System.nanoTime();
        libraryLock.lock();
        try {
            while (writerWaiting > 0 || isWriting > 0) {
                ++readerWaiting;
                readers.await();
            }
            program.setReaderWaitingTime(program.getReaderWaitingTime() + System.nanoTime() - tmp);
            isReading += 1;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            libraryLock.unlock();
        }
    }

    public void endReading() {
        long tmp = System.nanoTime();
        libraryLock.lock();
        program.setReaderWaitingTime(program.getReaderWaitingTime() + System.nanoTime() - tmp);
        try {
            isReading -= 1;
            if (isReading == 0) {
                if (writerWaiting > 0) {
                    --writerWaiting;
                }
                writers.signal();
            }
        } finally {
            libraryLock.unlock();
        }
    }

    public void beginWriting() {
        long tmp = System.nanoTime();
        libraryLock.lock();
        try {
            while (isReading + isWriting > 0) {
                ++writerWaiting;
                writers.await();
            }
            program.setWriterWaitingTime(program.getWriterWaitingTime() + System.nanoTime() - tmp);
            isWriting = 1;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            libraryLock.unlock();
        }
    }


    public void endWriting() {
        long tmp = System.nanoTime();
        libraryLock.lock();
        program.setWriterWaitingTime(program.getWriterWaitingTime() + System.nanoTime() - tmp);
        try {
            isWriting = 0;
            if (readerWaiting == 0) {
                if (writerWaiting > 0) {
                    --writerWaiting;
                }
                writers.signal();
            } else {
                readerWaiting = 0;
                readers.signalAll();
            }
        } finally {
            libraryLock.unlock();
        }
    }
}

class Reader extends Thread {
    private int nr;
    private Library library;

    public Reader(int nr, Library library) {
        super();
        this.nr = nr;
        this.library = library;
    }

    @Override
    public void run() {
        int i = 0;
        while (i++ < 1000) {
            library.beginReading();
            library.endReading();
        }
    }
}

class Writer extends Thread {
    private int nr;
    private Library library;

    public Writer(int nr, Library library) {
        super();
        this.nr = nr;
        this.library = library;
    }

    @Override
    public void run() {
        int i = 0;
        while (i++ < 1000) {
            library.beginWriting();
            library.endWriting();
        }
    }
}

