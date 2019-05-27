package pl.todrzywolek.concurrency.lab1;

import java.util.HashMap;
import java.util.Map;

public class HistogramCreator {
	
	private Map<Integer, Integer> data;

	public HistogramCreator() {
		this.data = new HashMap<>();
	}

	public Map<Integer, Integer> getData() {
		return data;
	}
	
	public int addData(int data) {
		if (this.data.containsKey(data)) {
			int currentValue = this.data.get(data);
			this.data.put(data, currentValue+1);
		} else {
			this.data.put(data, 1);
		}
		return data;
	}
	
	public void printData() {
		for (Integer d : this.data.keySet()) {
			System.out.println(d + ":" + this.data.get(d));
		}
	}
	
	
	

}
