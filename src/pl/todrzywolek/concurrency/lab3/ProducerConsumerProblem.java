package pl.todrzywolek.concurrency.lab3;

import java.util.ArrayList;
import java.util.List;

class Producer extends Thread {
    private Buffer _buf;

    public void run() {
        for (int i = 0; i < 100; ++i) {
            _buf.put(i);
        }
    }
}

class Consumer extends Thread {
    private Buffer _buf;

    public void run() {
        for (int i = 0; i < 100; ++i) {
            System.out.println(_buf.get());
        }
    }
}



class Buffer {

    private List<Integer> buffer = new ArrayList<>();

    public void put(int i) {

    }

    public int get() {

    }
}

class Main {

    public static void main(String[] args) {


    }
}
