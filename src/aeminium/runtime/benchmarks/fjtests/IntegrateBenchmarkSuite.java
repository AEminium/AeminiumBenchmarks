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

import jsr166y.ForkJoinPool;
import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.fjtests.aeminium.AeminiumIntegrate;
import aeminium.runtime.benchmarks.fjtests.forkjoin.Integrate.FQuad;
import aeminium.runtime.benchmarks.fjtests.forkjoin.Integrate.SQuad;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class IntegrateBenchmarkSuite implements BenchmarkSuite {
	
	Benchmark[] tests;
	
	protected double START = -2101.0;
	protected double END = 200.0;
	
	public IntegrateBenchmarkSuite() {
		tests = new Benchmark[3];
		
		tests[0] = new Benchmark() {
			@Override
			public String getName() {
				return "Sequential Integrate";
			}
			
			@Override
			public long run() {
				
				long start = System.nanoTime();
				SQuad seq = new SQuad(START, END, 0);
				seq.compute();
				long end = System.nanoTime();
				
				return end-start;
			}
		};
		
		tests[1] = new Benchmark() {
			
			ForkJoinPool pool = new ForkJoinPool();
			@Override
			public String getName() {
				return "ForkJoin Integrate";
			}
			
			@Override
			public long run() {
				FQuad seq = new FQuad(START, END, 0);
				long start = System.nanoTime();
				pool.invoke(seq);
				long end = System.nanoTime();
				
				return end-start;
			}
		};
		
		tests[2] = new Benchmark() {
			
			Runtime rt = Factory.getRuntime();
			
			@Override
			public String getName() {
				return "Aeminium Integrate";
			}
			
			@Override
			public long run() {

				rt.init();
				long start = System.nanoTime();
				Task t1 = AeminiumIntegrate.recursiveCall(rt, Runtime.NO_PARENT, START, END, 0);
				rt.shutdown();
				long end = System.nanoTime();
				assert(t1.getResult() != null);
				return end-start;
			}
		};
		
	}
	
	
	public static void main(String[] args) {
		IntegrateBenchmarkSuite suite = new IntegrateBenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}
	
	public Benchmark[] getTests() {
		return tests;
	}

}
