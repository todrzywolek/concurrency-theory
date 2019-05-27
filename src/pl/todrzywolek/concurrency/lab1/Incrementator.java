package pl.todrzywolek.concurrency.lab1;

public class Incrementator extends Thread {

	@Override
	public void run() {
		for(int i=0; i<Main.ITERATIONS; i++) {
			Main.count++;
		}
	}
}
