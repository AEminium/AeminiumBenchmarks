package aeminium.runtime.benchmarks.doall;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqDoAll {
	public static final int DEFAULT_SIZE = 1000000;
	public static final int DEFAULT_THRESHOLD = 1000;
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = DEFAULT_SIZE;
		if (be.args.length > 0)
			size = Integer.parseInt(be.args[0]);

		double[] a = new double[size];
		double[] b = new double[size];
		double[] c = new double[size];
		
		be.start();
		for (int i = 0; i < size; i++) {
			a[i] = Math.sqrt(i);
		}
		
		for (int i = 0; i < size; i++) {
			b[i] = Math.sin(i);
		}
		
		for (int i = 0; i < size; i++) {
			c[i] = a[i] / b[i];
		}
		be.end();
	}
}
