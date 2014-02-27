package aeminium.runtime.benchmarks.matrixmult;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjMatrixMult extends RecursiveAction {

	private static final long serialVersionUID = 1993757722402951913L;

	static int first[][];
	static int second[][];
	static int result[][];

	int ci, cf, di, df, p;
	int threshold;

	FjMatrixMult(int ci, int cf, int di, int df, int p, int threshold) {
		this.ci = ci;
		this.cf = cf;
		this.di = di;
		this.df = df;
		this.p = p;
		this.threshold = threshold;
	}

	@Override
	protected void compute() {
		if (cf - ci > threshold) {
			int half = (cf - ci) / 2 + ci;
			FjMatrixMult p1 = new FjMatrixMult(ci, half, di, df, p, threshold);
			FjMatrixMult p2 = new FjMatrixMult(half, cf, di, df, p, threshold);
			invokeAll(p1, p2);
		} else if (df - di > threshold) {
			int half = (df - di) / 2 + di;
			FjMatrixMult p1 = new FjMatrixMult(ci, cf, di, half, p, threshold);
			FjMatrixMult p2 = new FjMatrixMult(ci, cf, half, df, p, threshold);
			invokeAll(p1, p2);
		} else {
			// Sequential Version
			for (int c = ci; c < cf; c++) {
				for (int d = di; d < df; d++) {
					int sum = 0;
					for (int k = 0; k < p; k++) {
						sum += first[c][k] * second[k][d];
					}
					result[c][d] = sum;
				}
			}
		}

	}

	public static void main(String args[]) {
		Benchmark be = new Benchmark(args);
		int m = Matrix.DEFAULT_M;
		if (be.args.length > 0) m = Integer.parseInt(be.args[0]);
		int n = Matrix.DEFAULT_N;
		if (be.args.length > 1) n = Integer.parseInt(be.args[1]);
		int p = n;
		int q = Matrix.DEFAULT_Q;
		if (be.args.length > 2) q = Integer.parseInt(be.args[2]);
		int threshold = Matrix.DEFAULT_THRESHOLD;
		if (be.args.length > 3) threshold = Integer.parseInt(be.args[3]);

		first = Matrix.createMatrix(m, n);
		second = Matrix.createMatrix(p, q);
		result = new int[m][q];

		ForkJoinPool pool = new ForkJoinPool();

		while (!be.stop()) {
			be.start();
			FjMatrixMult fj = new FjMatrixMult(0, m, 0, q, p, threshold);
			pool.invoke(fj);
			be.end();
		}
	}

}
