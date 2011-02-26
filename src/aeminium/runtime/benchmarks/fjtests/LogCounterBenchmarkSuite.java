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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import jsr166y.ForkJoinPool;
import aeminium.runtime.Runtime;
import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.fjtests.forkjoin.LogCounter;
import aeminium.runtime.implementations.Factory;

public class LogCounterBenchmarkSuite implements BenchmarkSuite {

	Benchmark[] tests;
	File[] files = LogCounterBenchmarkSuite.createInputFiles();
	static int numberOfFiles = 1000;

	public LogCounterBenchmarkSuite() {
		tests = new Benchmark[3];

		tests[0] = new Benchmark() {
			@Override
			public String getName() {
				return "Sequential LogCounter";
			}

			@Override
			public long run() {

				long start = System.nanoTime();
				int n = LogCounter.sequentialCounter(files);
				long end = System.nanoTime();
				assert (n == 700405);
				return end - start;
			}
		};

		tests[1] = new Benchmark() {

			ForkJoinPool pool = new ForkJoinPool();

			@Override
			public String getName() {
				return "ForkJoin LogCounter";
			}

			@Override
			public long run() {

				long start = System.nanoTime();
				int n = LogCounter.forkjoinCounter(files, pool);
				long end = System.nanoTime();
				assert (n == 700405);
				return end - start;
			}
		};

		tests[2] = new Benchmark() {

			Runtime rt = Factory.getRuntime();

			@Override
			public String getName() {
				return "Aeminium Logcounter";
			}

			@Override
			public long run() {

				long start = System.nanoTime();
				int n = LogCounter.aeminiumCounter(files, rt);
				long end = System.nanoTime();
				assert (n == 700405);
				return end - start;
			}
		};

	}

	public static void main(String[] args) {
		LogCounterBenchmarkSuite suite = new LogCounterBenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}

	public Benchmark[] getTests() {
		return tests;
	}

	public static File[] createInputFiles() {
		try {
			Random rgen = new Random(122341425252L);		
			File[] files = new File[numberOfFiles];
			for (int fid = 0; fid < numberOfFiles; fid++) {
				File tmp = File.createTempFile(String.format("test_log_%d",fid), ".log");
				tmp.deleteOnExit();
				BufferedWriter out = new BufferedWriter(new FileWriter(tmp));
			    for (int i = 0; i < 100000; i++) {
			    	for (int j = 0; j < rgen.nextInt(512); j++) {
			    		for (int k = 0; k < rgen.nextInt(512); k++) {
			    			out.write("c");
						}
			    		out.write(" ");
			    	}
			    	out.write("\n");
				}
			    out.close();
			    File tmp2 = LogCounter.compressGZip(tmp);
				files[fid] = tmp2;
			}
		    return files;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
