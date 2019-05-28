package pl.todrzywolek.concurrency.lab7;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Main extends Thread {
    private Object[] o;
    private List1 list;
    private List2 list2;
    private static long sleepTime;

    public Main(Object[] o, List1 list) {
        super();
        this.o = o;
        this.list = list;
    }

    public Main(Object[] o, List2 list2) {
        super();
        this.o = o;
        this.list2 = list2;
    }

    @Override
    public void run() {
//        for (int i = 0, n = o.length; i < 10; ++i) {
//            try {
//                list.add(o[i % n]);
//                list.contains(o[(i + 1) % n]);
//                list.remove(o[(i + 2) % n]);
//                list.contains(o[(i + 3) % n]);
//                list.add(o[(i + 4) % n]);
//                list.remove(o[(i + 5) % n]);
//                list.add(o[(i + 6) % n]);
//                list.remove(o[(i + 7) % n]);
//                list.contains(o[(i + 8) % n]);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        for (int i = 0, n = o.length; i < 10; ++i) {
            try {
                list2.add(o[i % n]);
                list2.contains(o[(i + 1) % n]);
                list2.remove(o[(i + 2) % n]);
                list2.contains(o[(i + 3) % n]);
                list2.add(o[(i + 4) % n]);
                list2.remove(o[(i + 5) % n]);
                list2.add(o[(i + 6) % n]);
                list2.remove(o[(i + 7) % n]);
                list2.contains(o[(i + 8) % n]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Object[] o = {"ala ma kota", 10L, 10.0, "ola ma kota", 11L, 11.0,
                "ala ma kata", 11L, 11.0};
        List1 list = new List1("ala ma kota", null);
        List2 list2 = new List2("ala ma kota", null);

//        for (sleepTime = 0; sleepTime < 200; sleepTime += 20) {
//            list.setSleepTime(sleepTime);
//            long time = System.nanoTime();
//            Thread[] t = {new Main(o, list), new Main(o, list), new Main(o, list)};
//            for(int i = 0; i < t.length; ++i) {
//                t[i].start();
//            }
//            for(int i = 0; i < t.length; ++i) {
//                try {
//                    t[i].join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            time = System.nanoTime() - time;
//            System.out.println(sleepTime + " " + time);
//        }

        for (sleepTime = 0; sleepTime < 200; sleepTime += 20) {
            list2.setSleepTime(sleepTime);
            long time = System.nanoTime();
            Thread[] t = {new Main(o, list2), new Main(o, list2), new Main(o, list2)};
            for (int i = 0; i < t.length; ++i) {
                t[i].start();
            }
            for (int i = 0; i < t.length; ++i) {
                try {
                    t[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            time = System.nanoTime() - time;
            System.out.println(sleepTime + " " + time);
        }
    }
}

class List1 {
    private Object val;
    private List1 next;
    private Lock lock;
    private static long sleepTime;

    public List1(Object val, List1 next) {
        this.val = val;
        this.next = next;
        lock = new ReentrantLock();
    }

    public boolean contains(Object o) throws InterruptedException {
        List1 prev = null, tmp = this;
        lock.lock();
        try {
            while (tmp != null) {
                if (val == o) {
                    Thread.sleep(sleepTime / 10);
                    return true;
                }
                prev = tmp;
                tmp = tmp.next;
                try {
                    if (tmp != null) {
                        tmp.lock.lock();
                    }
                } finally {
                    prev.lock.unlock();
                }
            }
        } finally {
            if (tmp != null) {
                tmp.lock.unlock();
            }
        }
        return false;
    }


    public boolean remove(Object o) throws InterruptedException {
        List1 prevprev = null, prev = null, tmp = this;
        lock.lock();
        try {
            while (tmp != null) {
                if (val == o) {
                    if (prev != null) {
                        prev.next = tmp.next;
                        tmp.next = null;
                    }
                    Thread.sleep(sleepTime / 3);
                    return true;
                }
                prevprev = prev;
                prev = tmp;
                tmp = tmp.next;
                try {
                    if (tmp != null) {
                        tmp.lock.lock();
                    }
                } finally {
                    if (prevprev != null) {
                        prevprev.lock.unlock();
                    }
                }
            }
        } finally {
            if (prev != prevprev) {
                prev.lock.unlock();
            }
            if (tmp != null) {
                tmp.lock.unlock();
            }
        }
        return false;
    }

    public boolean add(Object o) throws InterruptedException {
        if (o == null) {
            return false;
        }
        List1 tmp = this, next = this.next;
        lock.lock();
        try {
            while (next != null) {
                try {
                    next.lock.lock();
                } finally {
                    tmp.lock.unlock();
                }
                tmp = next;
                next = next.next;
            }
            tmp.next = new List1(o, null);
            Thread.sleep(sleepTime);
            return true;
        } finally {
            tmp.lock.unlock();
            if (next != tmp && next != null) {
                next.lock.unlock();
            }
        }
    }

    public static void setSleepTime(long sleepTime) {
        List1.sleepTime = sleepTime;
    }
}


class List2 {
    private Object val;
    private List2 next;
    private static Lock lock = new ReentrantLock();
    private static long sleepTime;

    public List2(Object val, List2 next) {
        this.val = val;
        this.next = next;
    }

    public boolean contains(Object o) throws InterruptedException {
        List2 tmp = this;
        lock.lock();
        try {
            while (tmp != null) {
                if (val == o) {
                    Thread.sleep(sleepTime / 10);
                    return true;
                }
                tmp = tmp.next;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean remove(Object o) throws InterruptedException {
        List2 prev = null, tmp = this;
        lock.lock();
        try {
            while (tmp != null) {
                if (val == o) {
                    if (prev != null) {
                        prev.next = tmp.next;
                        tmp.next = null;
                    }
                    Thread.sleep(sleepTime / 3);
                    return true;
                }
                prev = tmp;
                tmp = tmp.next;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean add(Object o) throws InterruptedException {
        if (o == null) {
            return false;
        }
        List2 tmp = this, next = this.next;
        lock.lock();
        try {
            while (next != null) {
                tmp = next;
                next = next.next;
            }
            tmp.next = new List2(o, null);
            Thread.sleep(sleepTime);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public static void setSleepTime(long sleepTime) {
        List2.sleepTime = sleepTime;
    }
}

