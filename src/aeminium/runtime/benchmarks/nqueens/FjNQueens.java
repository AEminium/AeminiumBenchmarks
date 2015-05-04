package aeminium.runtime.benchmarks.nqueens;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.Arrays;
import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjNQueens extends RecursiveAction {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int minSize = NQueens.DEFAULT_MIN_SIZE;
		if (be.args.length > 0) {
			minSize = Integer.parseInt(be.args[0]);
		}

		int maxSize = NQueens.DEFAULT_MAX_SIZE;
		if (be.args.length > 1) {
			maxSize = Integer.parseInt(be.args[1]);
		}

		int[] solutions = new int[maxSize - minSize + 1];

		ForkJoinPool g = new ForkJoinPool();
		while (!be.stop()) {
			be.start();

			int c = 0;
			for (int size = minSize; size <= maxSize; size++) {
				FjNQueens task = new FjNQueens(size, new int[0]);
				g.invoke(task);
				solutions[c++] = task.solutions;
			}

			be.end();
			if (be.verbose) {
				for (int i = 0; i < (maxSize - minSize + 1); i++) {
					int given = solutions[i];
					int expected = NQueens.expectedSolutions[minSize + i];
					if (given != expected) {
						System.out.println("Failed:" + (minSize + i) + ", given: " + given + " when expected " + expected);
					}
				}
			}
		}
	}

	// Boards are represented as arrays where each cell
	// holds the column number of the queen in that row

	final int[] sofar;
	FjNQueens nextSubtask; // to link subtasks
	int solutions;
	int boardSize;

	FjNQueens(int bs, int[] a) {
		this.sofar = a;
		this.boardSize = bs;
	}

	public final void compute() {
		FjNQueens subtasks;
		int bs = boardSize;
		if (sofar.length >= bs) solutions = 1;
		else if ((subtasks = explore(sofar, bs)) != null) solutions = processSubtasks(subtasks);
	}

	private static FjNQueens explore(int[] array, int bs) {
		int row = array.length;
		FjNQueens s = null; // subtask list
		outer: for (int q = 0; q < bs; ++q) {
			for (int i = 0; i < row; i++) {
				int p = array[i];
				if (q == p || q == p - (row - i) || q == p + (row - i)) continue outer; // attacked
			}
			FjNQueens first = s; // lag forks to ensure 1 kept
			if (first != null) first.fork();
			int[] next = Arrays.copyOf(array, row + 1);
			next[row] = q;
			FjNQueens subtask = new FjNQueens(bs, next);
			subtask.nextSubtask = first;
			s = subtask;
		}
		return s;
	}

	private static int processSubtasks(FjNQueens s) {
		// Always run first the task held instead of forked
		s.compute();
		int ns = s.solutions;
		s = s.nextSubtask;
		// Then the unstolen ones
		while (s != null && s.tryUnfork()) {
			s.compute();
			ns += s.solutions;
			s = s.nextSubtask;
		}
		// Then wait for the stolen ones
		while (s != null) {
			s.join();
			ns += s.solutions;
			s = s.nextSubtask;
		}
		return ns;
	}
}