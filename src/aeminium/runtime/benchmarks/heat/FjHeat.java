package aeminium.runtime.benchmarks.heat;

import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjHeat extends Heat {

	static double dx;
	static double dy;
	static double dt;
	static double dtdxsq;
	static double dtdysq;

	static int nx;
	static int ny;
	static int nt;
	static int threshold;

	static double[][] oldm;
	static double[][] newm;

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int nx_p = Heat.DEFAULT_NX;
		if (be.args.length > 0) {
			nx_p = Integer.parseInt(be.args[0]);
		}

		int ny_p = Heat.DEFAULT_NY;
		if (be.args.length > 1) {
			ny_p = Integer.parseInt(be.args[1]);
		}

		int iterations = Heat.DEFAULT_ITERATIONS;
		if (be.args.length > 2) {
			iterations = Integer.parseInt(be.args[2]);
		}

		int threshold_p = Heat.DEFAULT_THRESHOLD;
		if (be.args.length > 3) {
			threshold_p = Integer.parseInt(be.args[3]);
		}

		nx = nx_p;
		ny = ny_p;
		nt = iterations;
		threshold = threshold_p;

		dx = (xo - xu) / (nx - 1);
		dy = (yo - yu) / (ny - 1);
		dt = (to - tu) / nt;
		dtdxsq = dt / (dx * dx);
		dtdysq = dt / (dy * dy);

		oldm = new double[nx][ny];
		newm = new double[nx][ny];

		ForkJoinPool g = new ForkJoinPool();

		while (!be.stop()) {
			be.start();

			@SuppressWarnings("serial")
			RecursiveAction main = new RecursiveAction() {
				public void compute() {
					for (int timestep = 0; timestep <= nt; timestep++) {
						(new Compute(0, nx, timestep)).invoke();
					}
				}
			};
			g.invoke(main);
			be.end();
		}
	}

	// the function being applied across the cells
	static final double f(double x, double y) {
		return Math.sin(x) * Math.sin(y);
	}

	// random starting values

	static final double randa(double x, double t) {
		return 0.0;
	}

	static final double randb(double x, double t) {
		return Math.exp(-2 * t) * Math.sin(x);
	}

	static final double randc(double y, double t) {
		return 0.0;
	}

	static final double randd(double y, double t) {
		return Math.exp(-2 * t) * Math.sin(y);
	}

	static final double solu(double x, double y, double t) {
		return Math.exp(-2 * t) * Math.sin(x) * Math.sin(y);
	}

	@SuppressWarnings("serial")
	static final class Compute extends RecursiveAction {
		final int lb;
		final int ub;
		final int time;

		Compute(int lowerBound, int upperBound, int timestep) {
			lb = lowerBound;
			ub = upperBound;
			time = timestep;
		}

		public void compute() {
			if (ub - lb > threshold) {
				int mid = (lb + ub) >>> 1;
				Compute left = new Compute(lb, mid, time);
				left.fork();
				new Compute(mid, ub, time).compute();
				left.join();
			} else if (time == 0) // if first pass, initialize cells
			init();
			else if (time % 2 != 0) // alternate new/old
			compstripe(newm, oldm);
			else compstripe(oldm, newm);
		}

		/** Updates all cells. */
		final void compstripe(double[][] newMat, double[][] oldMat) {

			// manually mangled to reduce array indexing

			final int llb = (lb == 0) ? 1 : lb;
			final int lub = (ub == nx) ? nx - 1 : ub;

			double[] west;
			double[] row = oldMat[llb - 1];
			double[] east = oldMat[llb];

			for (int a = llb; a < lub; a++) {

				west = row;
				row = east;
				east = oldMat[a + 1];

				double prev;
				double cell = row[0];
				double next = row[1];

				double[] nv = newMat[a];

				for (int b = 1; b < ny - 1; b++) {

					prev = cell;
					cell = next;
					double twoc = 2 * cell;
					next = row[b + 1];

					nv[b] = cell + dtdysq * (prev - twoc + next) + dtdxsq * (east[b] - twoc + west[b]);

				}
			}

			edges(newMat, llb, lub, tu + time * dt);
		}

		// the original version from cilk
		final void origcompstripe(double[][] newMat, double[][] oldMat) {

			final int llb = (lb == 0) ? 1 : lb;
			final int lub = (ub == nx) ? nx - 1 : ub;

			for (int a = llb; a < lub; a++) {
				for (int b = 1; b < ny - 1; b++) {
					double cell = oldMat[a][b];
					double twoc = 2 * cell;
					newMat[a][b] = cell + dtdxsq * (oldMat[a + 1][b] - twoc + oldMat[a - 1][b]) + dtdysq * (oldMat[a][b + 1] - twoc + oldMat[a][b - 1]);

				}
			}

			edges(newMat, llb, lub, tu + time * dt);
		}

		/** Initializes all cells. */
		final void init() {
			final int llb = (lb == 0) ? 1 : lb;
			final int lub = (ub == nx) ? nx - 1 : ub;

			for (int a = llb; a < lub; a++) { /* inner nodes */
				double[] ov = oldm[a];
				double x = xu + a * dx;
				double y = yu;
				for (int b = 1; b < ny - 1; b++) {
					y += dy;
					ov[b] = f(x, y);
				}
			}

			edges(oldm, llb, lub, 0);

		}

		/** Fills in edges with boundary values. */
		final void edges(double[][] m, int llb, int lub, double t) {

			for (int a = llb; a < lub; a++) {
				double[] v = m[a];
				double x = xu + a * dx;
				v[0] = randa(x, t);
				v[ny - 1] = randb(x, t);
			}

			if (lb == 0) {
				double[] v = m[0];
				double y = yu;
				for (int b = 0; b < ny; b++) {
					y += dy;
					v[b] = randc(y, t);
				}
			}

			if (ub == nx) {
				double[] v = m[nx - 1];
				double y = yu;
				for (int b = 0; b < ny; b++) {
					y += dy;
					v[b] = randd(y, t);
				}
			}
		}
	}

}
