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

package aeminium.runtime.benchmarks.fjtests;

import java.util.concurrent.ForkJoinPool;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.fibonacci.AeFibonacci;
import aeminium.runtime.benchmarks.fibonacci.FjFibonacci;
import aeminium.runtime.implementations.Factory;

public class FibonacciBenchmarkSuite implements BenchmarkSuite {
	
	Benchmark[] tests;
	
	protected int PARAMETER = 46;
	protected int THRESHOLD = 13;
	
	public FibonacciBenchmarkSuite() {
		tests = new Benchmark[3];
		
		tests[0] = new Benchmark() {
			@Override
			public String getName() {
				return "Sequential Fibonacci";
			}
			
			@Override
			public long run() {
				
				long start = System.nanoTime();
				seqFib(PARAMETER);
				long end = System.nanoTime();
				
				return end-start;
			}
			public int seqFib(int n) {
				return (n <= 2) ? 1 : seqFib(n-1) + seqFib(n-2);
			}
			
		};
		
		tests[1] = new Benchmark() {
			
			ForkJoinPool pool = new ForkJoinPool();
			@Override
			public String getName() {
				return "ForkJoin Fibonacci";
			}
			
			@Override
			public long run() {
				FjFibonacci fib = new FjFibonacci(PARAMETER, THRESHOLD);
				long start = System.nanoTime();
				pool.invoke(fib);
				long end = System.nanoTime();
				
				return end-start;
			}
		};
		
		tests[2] = new Benchmark() {
			
			Runtime rt = Factory.getRuntime();
			
			@Override
			public String getName() {
				return "Aeminium Fibonacci";
			}
			
			@Override
			public long run() {

				rt.init();
				Body fibBody = AeFibonacci.createFibBody(rt, PARAMETER, THRESHOLD);
				
				long start = System.nanoTime();
				Task t1 = rt.createNonBlockingTask(fibBody, Runtime.NO_HINTS);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
				
				rt.shutdown();
				long end = System.nanoTime();
				return end-start;
			}
		};
		
	}
	
	public static void main(String[] args) {
		FibonacciBenchmarkSuite suite = new FibonacciBenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}
	
	public Benchmark[] getTests() {
		return tests;
	}

}
