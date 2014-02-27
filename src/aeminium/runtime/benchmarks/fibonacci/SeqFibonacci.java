package aeminium.runtime.benchmarks.fibonacci;

import aeminium.runtime.benchmarks.helpers.Benchmark;


public class SeqFibonacci {
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int fib = Fibonacci.DEFAULT_SIZE;
		if (be.args.length > 0) {
			fib = Integer.parseInt(be.args[0]);
		}
		while (!be.stop()) {
			be.start();
			long val = seqFib(fib);
			be.end();
			
			if (be.verbose) {
				System.out.println("F(" + fib + ") = " + val);
			}
		}
	}
	
	public static long seqFib(long n) {
		if (n <= 2)
			return 1;
		else
			return (seqFib(n - 1) + seqFib(n - 2));
	}
}
