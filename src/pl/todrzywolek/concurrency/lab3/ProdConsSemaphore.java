package pl.todrzywolek.concurrency.lab3;

public class ProdConsSemaphore {

    private int value;

    public ProdConsSemaphore(int initial) {
        value = initial;
    }

    public synchronized void up() {
        ++value;
        notifyAll();
    }

    public synchronized void down() throws InterruptedException {
        while (value == 0)
            wait();
        --value;
    }
}
