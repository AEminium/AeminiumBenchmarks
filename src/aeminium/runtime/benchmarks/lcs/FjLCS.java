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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjLCS {
	
	private int blockN;
	
	public FjLCS(int blockSize) {
		this.blockN = blockSize;
	}
	
	public String parCompute(final String x, final String y) {
	
		final int M = x.length();
		final int N = y.length();
		final int[][] matrix = new int[M+1][N+1];
		final int blockSize = blockN;
		ArrayList<FutureTask<Void>> tasks = new ArrayList<FutureTask<Void>>(); 

		// launch workers
		Executor launcher = Executors.newCachedThreadPool();
		for (int im = M; im >= 0; im -= blockSize) {
            for (int jm = N; jm >= 0; jm -= blockSize) {
            	final int I = im;
            	final int J = jm;
            	FutureTask<Void> ftask = new FutureTask<Void>(new Runnable() {

					@Override
					public void run() {
						for (int i = I-1; i >= 0 && i >= (I - blockSize); i--) {
							for (int j = J-1; j >= 0 && j >= (J - blockSize); j--) {
								if (i < 0 || i >= x.length() || j < 0 || j >= y.length())
									System.out.println("j,i: " + j + "," + i);
								if (x.charAt(i) == y.charAt(j))
				                    matrix[i][j] = matrix[i+1][j+1] + 1;
				                else 
				                    matrix[i][j] = Math.max(matrix[i+1][j], matrix[i][j+1]);
							}
						}
					}},null); 
            	tasks.add(ftask);
            	launcher.execute(ftask);
            }
		}
		for (FutureTask<Void> task : tasks) {
			try {
				task.get();
				if (!task.isDone()) {
					System.out.println("Cenas");
					task.cancel(true);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		launcher = null;
		
		// rebuild solution
		StringBuilder sol = new StringBuilder();
		int i = 0, j = 0;
        while(i < M && j < N) {
            if (x.charAt(i) == y.charAt(j)) {
            	sol.append(x.charAt(i));
                i++;
                j++;
            }
            else if (matrix[i+1][j] >= matrix[i][j+1]) i++;
            else                                 j++;
        }
        return sol.toString();
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
		FjLCS longest = new FjLCS(5);
		String common = longest.parCompute(s1, s2);
		be.end();
		if (be.verbose) {
			System.out.println(common);
		}
	}
	
}
