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

package aeminium.runtime.benchmarks.lcs;

import java.util.ArrayList;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;

public class AeLCS {

	public String solution;

	public void compute(final String x, final String y) {
		Runtime rt = Factory.getRuntime();
		rt.init();

		Task t = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				final int M = x.length();
				final int N = y.length();
				final int[][] opt = new int[M + 1][N + 1];
				final Task[][] d = new Task[M][N];
				for (int k = M - 1; k >= 0; k--) {
					for (int l = N - 1; l >= 0; l--) {
						final int i = k;
						final int j = l;
						d[i][j] = rt.createNonBlockingTask(new Body() {
							@Override
							public void execute(Runtime rt, Task current)
									throws Exception {
								if (x.charAt(i) == y.charAt(j))
									opt[i][j] = opt[i + 1][j + 1] + 1;
								else
									opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
								if (i != M - 1) {
									d[i+1][j] = null;
								}
								if (j != N - 1) {
									d[i][j+1] = null;
								}
								if (i % 10 == 0 && j % 10 == 0) System.gc();
							}
							
						}, Runtime.NO_HINTS);
						ArrayList<Task> deps = new ArrayList<Task>();
						if (i != M - 1) {
							deps.add(d[i + 1][j]);
						}
						if (j != N - 1) {
							deps.add(d[i][j + 1]);
						}
						rt.schedule(d[i][j], Runtime.NO_PARENT, deps);
					}
				}

				Task merge = rt.createNonBlockingTask(new Body() {
					@Override
					public void execute(Runtime rt, Task current) throws Exception {
						StringBuilder sol = new StringBuilder();
						// recover LCS itself and print it to standard output
						int i = 0, j = 0;
						while (i < M && j < N) {
							if (x.charAt(i) == y.charAt(j)) {
								sol.append(x.charAt(i));
								i++;
								j++;
							} else if (opt[i + 1][j] >= opt[i][j + 1])
								i++;
							else
								j++;
						}
						solution = sol.toString();
					}
				}, Runtime.NO_HINTS);
				ArrayList<Task> deps = new ArrayList<Task>();
				deps.add(d[0][0]);
				rt.schedule(merge, Runtime.NO_PARENT, deps);
			}
			
		}, Runtime.NO_HINTS);
		rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		
		String s1 = LCS.s1;
		String s2 = LCS.s1;
		if (args.length > 0) {
			s1 = LCS.readFile(args[0]);
		}
		if (args.length > 1) {
			s2 = LCS.readFile(args[1]);
		}
		be.start();
		AeLCS gen = new AeLCS();
		gen.compute(s1, s2);
		be.end();
		if (be.verbose) {
			System.out.println(gen.solution);
		}
	}

	

}