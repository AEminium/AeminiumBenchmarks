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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

@SuppressWarnings("serial")
public

class FjFibonacci extends RecursiveAction { 
	public volatile int number;
	private int threshold = 13;
	
	public FjFibonacci(int n, int thre) { 
		number = n;
		threshold = thre;
	}

	private int seqFib(int n) {
		if (n <= 2) return 1;
		else return seqFib(n-1) + seqFib(n-2);
	}

	@Override
	protected void compute() {
		int n = number;
		if (n <= 1) { /* do nothing */ }
		else if (n <= threshold) 
			number = seqFib(n);
		else {
			FjFibonacci f1 = new FjFibonacci(n - 1, threshold);	
			FjFibonacci f2 = new FjFibonacci(n - 2, threshold);
			invokeAll(f1,f2);
			number = f1.number + f2.number; // compose
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
		
		ForkJoinPool pool = new ForkJoinPool();
		while (!be.stop()) {
			be.start();
			FjFibonacci t = new FjFibonacci(fib, threshold);
			pool.invoke(t);
			be.end();
			if (be.verbose) {
				System.out.println("F(" + fib + ") = " + t.number);
			}
		}
		
	}
}