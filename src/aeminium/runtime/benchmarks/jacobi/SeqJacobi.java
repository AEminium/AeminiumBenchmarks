package aeminium.runtime.benchmarks.jacobi;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqJacobi {
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = Jacobi.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}

		int steps = Jacobi.DEFAULT_STEPS;
		if (be.args.length > 1) {
			steps = Integer.parseInt(be.args[1]);
		}

		int granularity = Jacobi.DEFAULT_GRANULARITY;
		if (be.args.length > 2) {
			granularity = Integer.parseInt(be.args[2]);
		}


		int dim = size+2;
		double[][] a = new double[dim][dim];
		double[][] b = new double[dim][dim];

		Jacobi.setup(size, a, b);
		
		while (!be.stop()) {
			be.start();
		
			double df = seqJacobi(size, steps, granularity, a, b);
			be.end();
			if (be.verbose) {
				System.out.println("Total: " + df);
			}
		}
	}

	private static double seqJacobi(int n, int steps, int granularity, double[][] a, double[][] b) {
		double df = 0.0;
		for (int x = 0; x < steps; ++x) {
			df = buildNode_seq(a, b, 1, n, 1, n, granularity, x);
		}
		return df;
	}

	public static double buildNode_seq(double[][] a, double[][] b,
			int lr, int hr, int lc, int hc, int leafs, int steps) {

		int rows = (hr - lr + 1);
		int cols = (hc - lc + 1);

		int mr = (lr + hr) >>> 1; // midpoints
			int mc = (lc + hc) >>> 1;

			int hrows = (mr - lr + 1);
			int hcols = (mc - lc + 1);

			if (rows * cols <= leafs) {
				++leafs;
				return processLeafNode(a, b, lr, hr, lc, hc, steps);
			}
			else if (hrows * hcols >= leafs) {
				final double df1 = buildNode_seq(a, b, lr,   mr, lc,   mc, leafs, steps);
				final double df2 = buildNode_seq(a, b, lr,   mr, mc+1, hc, leafs, steps);
				final double df3 = buildNode_seq(a, b, mr+1, hr, lc,   mc, leafs, steps);
				final double df4 = buildNode_seq(a, b, mr+1, hr, mc+1, hc, leafs, steps);
				return ((((df1>df2)?df1:df2)>df3?((df1>df2)?df1:df2):df3)>df4)?(((df1>df2)?df1:df2)>df3?((df1>df2)?df1:df2):df3):df4;
			}
			else if (cols >= rows) {
				final double df1 = buildNode_seq(a, b, lr, hr, lc,   mc, leafs, steps);
				final double df2 = buildNode_seq(a, b, lr, hr, mc+1, hc, leafs, steps);
				return ((df1 > df2) ? df1 : df2);
			}
			else {
				final double df1 = buildNode_seq(a, b, lr, mr, lc, hc, leafs, steps);
				final double df2 = buildNode_seq(a, b, mr+1, hr, lc, hc, leafs, steps);
				return ((df1 > df2) ? df1 : df2);
			}
	}

	public static double processLeafNode(double[][] A, double[][] B,
			int loRow, int hiRow,
			int loCol, int hiCol, int steps) {

		boolean AtoB = (steps++ & 1) == 0;
		double[][] a = AtoB ? A : B;
		double[][] b = AtoB ? B : A;

		double md = 0.0; // local for computing max diff

		for (int i = loRow; i <= hiRow; ++i) {
			for (int j = loCol; j <= hiCol; ++j) {
				double v = 0.25 * (a[i-1][j] + a[i][j-1] +
						a[i+1][j] + a[i][j+1]);
				b[i][j] = v;

				double diff = v - a[i][j];
				if (diff < 0) diff = -diff;
				if (diff > md) md = diff;
			}
		}
		return md;
	}

}
