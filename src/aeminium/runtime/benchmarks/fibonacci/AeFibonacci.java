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
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class AeFibonacci {
	
	public static long seqFib(long n) {
		if (n <= 2)
			return 1;
		else
			return (seqFib(n - 1) + seqFib(n - 2));
	}

	public static class FibBody implements Body {
		public volatile long value;

		public FibBody(long n) {
			this.value = n;
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if (!rt.parallelize()) {
				value = seqFib(value);
			} else {
				FibBody b1 = new FibBody(value - 1);
				Task t1 = rt.createNonBlockingTask(b1, Runtime.NO_HINTS);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);

				FibBody b2 = new FibBody(value - 2);
				Task t2 = rt.createNonBlockingTask(b2, Runtime.NO_HINTS);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);

				t1.getResult();
				t2.getResult();
				value = b1.value + b2.value;
			}
		}
	}

	public static Body createFibBody(final Runtime rt, final int n) {
		return new AeFibonacci.FibBody(n);
	}

	public static void main(String[] args) {
		long initialTime = System.currentTimeMillis();

		int fib = 46;
		if (args.length >= 1) {
			fib = Integer.parseInt(args[0]);
		}

		Runtime rt = Factory.getRuntime();
		rt.init();
		FibBody body = new AeFibonacci.FibBody(fib);
		Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
		rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();

		System.out.println("F(" + fib + ") = " + body.value);

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}
}
