package aeminium.runtime.benchmarks.gaknapsack;

public class Indiv implements Comparable<Indiv> {
	public boolean[] has;

	public double fitness = 0;
	public int size;

	public Indiv(int size) {
		this.size = size;
		has = new boolean[size];
	}

	public void set(int w, boolean h) {
		has[w] = h;
	}

	public int compareTo(Indiv other) {
	    if (this.fitness == other.fitness) {
	      return 0;
	    } else if (this.fitness > other.fitness) {
	      return 1;
	    } else {
	      return -1;
	    }
	  }
}