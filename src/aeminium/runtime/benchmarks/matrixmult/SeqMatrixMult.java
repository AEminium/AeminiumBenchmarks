package aeminium.runtime.benchmarks.matrixmult;

import aeminium.runtime.benchmarks.helpers.Benchmark;

class SeqMatrixMult {

	static int first[][];
	static int second[][];
	static int result[][];

	public static void main(String args[]) {
		Benchmark be = new Benchmark(args);
		int m = Matrix.DEFAULT_M;
		if (be.args.length > 0) m = Integer.parseInt(be.args[0]);
		int n = Matrix.DEFAULT_N;
		if (be.args.length > 1) n = Integer.parseInt(be.args[1]);
		int p = n;
		int q = Matrix.DEFAULT_Q;
		if (be.args.length > 2) q = Integer.parseInt(be.args[2]);

		first = Matrix.createMatrix(m, n);
		second = Matrix.createMatrix(p, q);
		result = new int[m][q];

		while (!be.stop()) {
			be.start();
			for (int c = 0; c < m; c++) {
				for (int d = 0; d < q; d++) {
					int sum = 0;
					for (int k = 0; k < p; k++) {
						sum += first[c][k] * second[k][d];
					}
					result[c][d] = sum;
				}
			}
			be.end();
		}
	}
}