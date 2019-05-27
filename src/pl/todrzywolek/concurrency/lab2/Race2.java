package pl.todrzywolek.concurrency.lab2;

// Race2.java
// race

class Counter {
	private int _val;

	public Counter(int n) {
		_val = n;
	}

	public void inc() {
		int n;
		n = _val;
		n = n + 1;
		_val = n;
	}

	public void dec() {
		int n;
		n = _val;
		n = n - 1;
		_val = n;
	}

	public int value() {
		return _val;
	}

	public void reset() {
		_val = 0;
	}
}

class IThread extends Thread {
	private Counter _cnt;
	private MySemaphore mySemaphore;

	public IThread(Counter c, MySemaphore mySemaphore) {
		_cnt = c;
		this.mySemaphore = mySemaphore;
	}

	public void run() {
		for (int i = 0; i < 1000_000; ++i) {
			try {
				mySemaphore.down(); // zajecie zasobu
				_cnt.inc();
				mySemaphore.up();  // zwolnienie zasobu
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class DThread extends Thread {
	private Counter _cnt;
	private MySemaphore mySemaphore;

	public DThread(Counter c, MySemaphore mySemaphore) {
		_cnt = c;
		this.mySemaphore = mySemaphore;
	}

	public void run() {
		for (int i = 0; i < 1000_000; ++i) {
			try {
				mySemaphore.down();
				_cnt.dec();
				mySemaphore.up();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}
}

class Race2 {
	public static void main(String[] args) {
		Counter cnt = new Counter(0);
		// utworzenie semafora binarnego
		MySemaphore semaphore = new MySemaphore(1);

		int i = 100;
		try {
			while (i >= 0) {
			    // utworzenie IThread z semaforem binarnym
				IThread it = new IThread(cnt, semaphore);
                // utworzenie DThread z semaforem binarnym
				DThread dt = new DThread(cnt, semaphore);
				it.start();
				dt.start();
				it.join();
				dt.join();
				System.out.println(i + ". value=" + cnt.value());
				i--;
			}

		} catch (InterruptedException ie) {
		}
	}
}
