package aeminium.runtime.benchmarks.doall;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqDoAll {
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = 1000000;
		if (args.length > 0)
			size = Integer.parseInt(args[0]);

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
