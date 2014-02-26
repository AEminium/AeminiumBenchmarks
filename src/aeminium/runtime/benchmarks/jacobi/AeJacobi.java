package aeminium.runtime.benchmarks.jacobi;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeJacobi {

	MatrixTree mat;
	double[][] A; double[][] B;
	int firstRow; int lastRow;
	int firstCol; int lastCol;
	final int steps;
	final int leafs;
	int nleaf;
	double md;

	public AeJacobi(double[][] A, double[][] B,
			int firstRow, int lastRow,
			int firstCol, int lastCol,
			int steps, int leafs) {
		this.A = A;
		this.B = B;
		this.firstRow = firstRow;
		this.firstCol = firstCol;
		this.lastRow = lastRow;
		this.lastCol = lastCol;
		this.steps = steps;
		this.leafs = leafs;
		mat = build(A, B, firstRow, lastRow, firstCol, lastCol, leafs);
	}

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
		AeJacobi jac = new AeJacobi(a, b, 1, size, 1, size, steps, granularity);
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		double df = jac.jacobi(rt);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Total: " + df);
		}
	}

	private double jacobi(Runtime rt) {
		double md = 0;
		for (int i = 0; i < steps; ++i) {
			try {
				Task t = rt.createNonBlockingTask(mat, Runtime.NO_HINTS);
				rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return md;
	}

	MatrixTree build(double[][] a, double[][] b,
			int lr, int hr, int lc, int hc, int leafs) {
		int rows = (hr - lr + 1);
		int cols = (hc - lc + 1);

		int mr = (lr + hr) >>> 1; // midpoints
		int mc = (lc + hc) >>> 1;

		int hrows = (mr - lr + 1);
		int hcols = (mc - lc + 1);

		if (rows * cols <= leafs) {
			++nleaf;
			return new LeafNode(a, b, lr, hr, lc, hc);
		}
		else if (hrows * hcols >= leafs) {
			return new FourNode(build(a, b, lr,   mr, lc,   mc, leafs),
					build(a, b, lr,   mr, mc+1, hc, leafs),
					build(a, b, mr+1, hr, lc,   mc, leafs),
					build(a, b, mr+1, hr, mc+1, hc, leafs));
		}
		else if (cols >= rows) {
			return new TwoNode(build(a, b, lr, hr, lc,   mc, leafs),
					build(a, b, lr, hr, mc+1, hc, leafs));
		}
		else {
			return new TwoNode(build(a, b, lr,   mr, lc, hc, leafs),
					build(a, b, mr+1, hr, lc, hc, leafs));

		}
	}


	abstract static class MatrixTree implements Body {
		double maxDiff;

		public final double save(double md) {
			maxDiff = (md > maxDiff) ? md : maxDiff;
			return maxDiff;
		}
	}

	static final class LeafNode extends MatrixTree {
		final double[][] A; // matrix to get old values from
		final double[][] B; // matrix to put new values into

		// indices of current submatrix
		final int loRow;    final int hiRow;
		final int loCol;    final int hiCol;

		int steps = 0; // track even/odd steps

		LeafNode(double[][] A, double[][] B,
				int loRow, int hiRow,
				int loCol, int hiCol) {
			this.A = A;   this.B = B;
			this.loRow = loRow; this.hiRow = hiRow;
			this.loCol = loCol; this.hiCol = hiCol;
		}

		@Override
		public void execute(Runtime rt, Task current) throws Exception {
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
			maxDiff = md;
		}


	}

	static final class FourNode extends MatrixTree {
		MatrixTree q1;
		MatrixTree q2;
		MatrixTree q3;
		MatrixTree q4;
		FourNode(MatrixTree q1, MatrixTree q2,
				MatrixTree q3, MatrixTree q4) {
			this.q1 = q1; this.q2 = q2; this.q3 = q3; this.q4 = q4;
		}

		public void execute(Runtime rt, Task current) throws Exception {
			Task t1 = null , t2 = null, t3 = null;
			if (rt.parallelize(current)) {
				t1 = rt.createNonBlockingTask(q1, Hints.LOOPS);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}
			if (rt.parallelize(current)) {
				t2 = rt.createNonBlockingTask(q2, Hints.LOOPS);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}
			if (rt.parallelize(current)) {
				t3 = rt.createNonBlockingTask(q3, Hints.LOOPS);
				rt.schedule(t3, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}

			q4.execute(rt, current);

			if (t1 != null)
				t1.getResult();
			else
				q1.execute(rt, current);

			if (t2 != null)
				t2.getResult();
			else
				q2.execute(rt, current);

			if (t3 != null)
				t3.getResult();
			else
				q3.execute(rt, current);

			double md = q1.maxDiff;
			md = q2.save(maxDiff);
			md = q3.save(maxDiff);
			md = q4.save(maxDiff);
			maxDiff = md;
		}
	}

	static final class TwoNode extends MatrixTree {
		MatrixTree q1;
		MatrixTree q2;
		TwoNode(MatrixTree q1, MatrixTree q2) {
			this.q1 = q1; this.q2 = q2;
		}

		public void execute(Runtime rt, Task current) throws Exception {
			Task t1 = null;
			if (rt.parallelize(current)) {
				t1 = rt.createNonBlockingTask(q1, Hints.LOOPS);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			} else {
				q1.execute(rt, current);
			}
			q2.execute(rt, current);
			if (t1 != null) t1.getResult();
			
			double md = q1.maxDiff;
			md = q2.save(maxDiff);
			maxDiff = md;
		}
	}
}
