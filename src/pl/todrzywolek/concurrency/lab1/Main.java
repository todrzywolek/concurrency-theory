package pl.todrzywolek.concurrency.lab1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	
	public static final int ITERATIONS = 1000_000;
	public static int count = 0;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		List<Integer> data = new ArrayList<>();
		int i = 100;

		while(i>0) {
		    // watek inkrementujacy
			Thread incr = new Thread(new Incrementator());
			// watek dekrementujacy
			Thread decr = new Thread(new Decrementator());
			incr.start();
			decr.start();
			incr.join();
			decr.join();
			i--;
			data.add(count);
            count = 0;
		}
		FilePersistor.saveToFile(data);
	}
}
