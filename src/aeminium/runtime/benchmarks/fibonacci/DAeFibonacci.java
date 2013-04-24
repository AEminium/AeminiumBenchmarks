package aeminium.runtime.benchmarks.fibonacci;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;

public class DAeFibonacci {
	
	public static class FibBody implements Body {
		public volatile long value;
		private int threshold;

		public FibBody(long n, int threshold) {
			this.value = n;
			this.threshold = threshold;
		}

		public long seqFib(long n, Runtime rt, Task current) {
			if (n <= 2)
				return 1;
			else {
				long nm1 = seqFib(n-2, rt, current);
				long nm2 = decideFib(n-1, rt, current);
				return nm1 + nm2;
			}
		}
		
		
		public long decideFib(long n, Runtime rt, Task current) {
			if (n % 5 == 0) {
				return parFib(n, rt, current);
			} else {
				return seqFib(n, rt, current);
			}
		}

		private long parFib(long n, Runtime rt, Task current) {
			FibBody b1 = new FibBody(value - 1, threshold);
			Task t1 = rt.createNonBlockingTask(b1, Runtime.NO_HINTS);
			rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);

			FibBody b2 = new FibBody(value - 2, threshold);
			Task t2 = rt.createNonBlockingTask(b2, Runtime.NO_HINTS);
			rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);

			t1.getResult();
			t2.getResult();
			return b1.value + b2.value;
		}

		@Override
		public void execute(Runtime rt, Task current) {
			value = decideFib(this.value, rt, current);
		}
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int fib = Fibonacci.DEFAULT_SIZE;
		if (be.args.length >= 1) {
			fib = Integer.parseInt(be.args[0]);
		}
		int threshold = Fibonacci.DEFAULT_THRESHOLD;
		if (be.args.length >= 2) {
			threshold = Integer.parseInt(be.args[1]);
		}
		
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.init();
		FibBody body = new DAeFibonacci.FibBody(fib, threshold);
		Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
		rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("F(" + fib + ") = " + body.value);
		}
	}
}
