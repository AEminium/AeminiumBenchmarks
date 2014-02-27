package aeminium.runtime.benchmarks.jacobi;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjJacobi {

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
		ForkJoinPool fjp = new ForkJoinPool();
		
		while (!be.stop()) {
			be.start();
		
			Driver driver = new Driver(a, b, 1, size, 1, size, steps, granularity);
			fjp.invoke(driver);
       
			be.end();
			if (be.verbose) {
				System.out.println("Total: " + driver.md);
			}
		}
	}
	
	@SuppressWarnings("serial")
    abstract static class MatrixTree extends RecursiveAction {
        // maximum difference between old and new values
        double maxDiff;
        public final double directCompute() {
            compute();
            return maxDiff;
        }
        public final double joinAndReinitialize(double md) {
            if (tryUnfork())
                compute();
            else {
                quietlyJoin();
                reinitialize();
            }
            double m = maxDiff;
            return (md > m) ? md : m;
        }

    }

    @SuppressWarnings("serial")
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

        public void compute() {
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
    
    @SuppressWarnings("serial")
    static final class FourNode extends MatrixTree {
        final MatrixTree q1;
        final MatrixTree q2;
        final MatrixTree q3;
        final MatrixTree q4;
        FourNode(MatrixTree q1, MatrixTree q2,
                 MatrixTree q3, MatrixTree q4) {
            this.q1 = q1; this.q2 = q2; this.q3 = q3; this.q4 = q4;
        }

        public void compute() {
            q4.fork();
            q3.fork();
            q2.fork();
            double md = q1.directCompute();
            md = q2.joinAndReinitialize(md);
            md = q3.joinAndReinitialize(md);
            md = q4.joinAndReinitialize(md);
            maxDiff = md;
        }
    }

    @SuppressWarnings("serial")
    static final class TwoNode extends MatrixTree {
        final MatrixTree q1;
        final MatrixTree q2;

        TwoNode(MatrixTree q1, MatrixTree q2) {
            this.q1 = q1; this.q2 = q2;
        }

        public void compute() {
            q2.fork();
            maxDiff = q2.joinAndReinitialize(q1.directCompute());
        }

    }


    @SuppressWarnings("serial")
	static final class Driver extends RecursiveAction {
        MatrixTree mat;
        double[][] A; double[][] B;
        int firstRow; int lastRow;
        int firstCol; int lastCol;
        final int steps;
        final int leafs;
        int nleaf;
        double md;

        Driver(double[][] A, double[][] B,
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

        static void doCompute(MatrixTree m, int s) {
            for (int i = 0; i < s; ++i) {
                m.invoke();
                m.reinitialize();
            }
        }

        public void compute() {
            doCompute(mat, steps);
            md = mat.maxDiff;
        }
    }


	
}
