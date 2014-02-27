package aeminium.runtime.benchmarks.gaknapsack;

public class Item {

	public Item(String n, int w, int v) {
		this.name = n;
		this.weight = w;
		this.value = v;
	}

	public String name;
	public int weight;
	public int value;
}