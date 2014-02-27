package aeminium.runtime.benchmarks.nqueens;

import java.util.Arrays;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqNQueens {

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

		while (!be.stop()) {
			be.start();

			int c = 0;
			for (int size = minSize; size <= maxSize; size++) {
				solutions[c++] = solve(size);
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

	public static int solve(int size) {
		return solve(size, new int[0]);
	}

	public static int solve(int bs, int[] array) {
		if (array.length >= bs) {
			return 1;
		} else {
			int solutions = 0;
			int row = array.length;
			outer: for (int q = 0; q < bs; ++q) {
				for (int i = 0; i < row; i++) {
					int p = array[i];
					if (q == p || q == p - (row - i) || q == p + (row - i)) continue outer; // attacked
				}

				int[] next = Arrays.copyOf(array, row + 1);
				next[row] = q;
				solutions += solve(bs, next);
			}
			return solutions;
		}
	}
}