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

import java.util.Random;

import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.fjtests.aeminium.AeminiumLCS;
import aeminium.runtime.benchmarks.fjtests.forkjoin.LCS;
import aeminium.runtime.Runtime;
import aeminium.runtime.implementations.Factory;

public class LCSBenchmarkSuite implements BenchmarkSuite {
	
	Benchmark[] tests;
	
	protected int BLOCKSIZE = 100;
	Random r = new Random();
	String s1 = generateString(r, "abcdefghijklnmnopqrstuvxywz ", 800);
	String s2 = generateString(r, "abcdefghijklnmnopqrstuvxywz ", 800);
	
	
	public LCSBenchmarkSuite() {
		tests = new Benchmark[3];
		s1 = s1 + s1 + s1;
		
		tests[0] = new Benchmark() {
			@Override
			public String getName() {
				return "Sequential LCS";
			}
			
			@Override
			public long run() {
				LCS gen = new LCS(BLOCKSIZE);
				long start = System.nanoTime();
				gen.seqCompute(s1, s2);
				long end = System.nanoTime();
				
				return end-start;
			}
			
		};
		
		tests[1] = new Benchmark() {
			
			@Override
			public String getName() {
				return "ThreadExecutor LCS";
			}
			
			@Override
			public long run() {
				LCS gen = new LCS(BLOCKSIZE);
				long start = System.nanoTime();
				gen.parCompute(s1, s2);
				long end = System.nanoTime();
				
				return end-start;
			}
		};
		
		tests[2] = new Benchmark() {
			
			Runtime rt = Factory.getRuntime();
			
			@Override
			public String getName() {
				return "Aeminium LCS";
			}
			
			@Override
			public long run() {

				rt.init();
				AeminiumLCS gen = new AeminiumLCS(BLOCKSIZE);
				
				long start = System.nanoTime();
				gen.compute(rt, s1, s2);
				
				rt.shutdown();
				long end = System.nanoTime();
				return end-start;
			}
		};
		
	}
	
	
	public static void main(String[] args) {
		LCSBenchmarkSuite suite = new LCSBenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}
	
	public Benchmark[] getTests() {
		return tests;
	}
	
	public static String generateString(Random rng, String characters, int length)
	{
	    char[] text = new char[length];
	    for (int i = 0; i < length; i++)
	    {
	        text[i] = characters.charAt(rng.nextInt(characters.length()));
	    }
	    return new String(text);
	}

}
