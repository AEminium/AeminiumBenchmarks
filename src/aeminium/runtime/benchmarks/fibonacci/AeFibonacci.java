/**
 * Copyright (c) 2010-11 The AEminium Project (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package aeminium.runtime.benchmarks.fibonacci;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeFibonacci {
	
	

	public static class FibBody implements Body {
		public volatile long value;
		private int threshold;

		public FibBody(long n, int threshold) {
			this.value = n;
			this.threshold = threshold;
		}

		public long seqFib(long n) {
			if (n <= 2)
				return 1;
			else
				return (seqFib(n - 1) + seqFib(n - 2));
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if (Benchmark.useThreshold ? value < threshold : !rt.parallelize(current)) {
				value = seqFib(value);
			} else {
				if (value <= 2) {
					value = 1;
					return;
				}
				
				FibBody b1 = new FibBody(value - 1, threshold);
				Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);

				FibBody b2 = new FibBody(value - 2, threshold);
				Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);

				t1.getResult();
				t2.getResult();
				value = b1.value + b2.value;
			}
		}
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int fib = Fibonacci.DEFAULT_SIZE;
		if (be.args.length > 0) {
			fib = Integer.parseInt(be.args[0]);
		}
		int threshold = Fibonacci.DEFAULT_THRESHOLD;
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}
		
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		while (!be.stop()) {
			be.start();
			rt.init();
			FibBody body = new AeFibonacci.FibBody(fib, threshold);
			Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
			rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
			if (be.verbose) {
				System.out.println("F(" + fib + ") = " + body.value);
			}
		}
		
	}
}
