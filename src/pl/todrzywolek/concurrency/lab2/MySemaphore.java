package pl.todrzywolek.concurrency.lab2;

public class MySemaphore {
	private int value;

	public MySemaphore(int initial) {
		value = initial;
	}

	synchronized public void up() {
		++value;
		notify();
	}

	synchronized public void down() throws InterruptedException {
		while (value == 0)
			wait();
		--value;
	}
}
