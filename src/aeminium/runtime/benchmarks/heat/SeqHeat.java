package aeminium.runtime.benchmarks.heat;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqHeat extends Heat {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int nx = Heat.DEFAULT_NX;
		if (be.args.length > 0) {
			nx = Integer.parseInt(be.args[0]);
		}

		int ny = Heat.DEFAULT_NY;
		if (be.args.length > 1) {
			ny = Integer.parseInt(be.args[1]);
		}

		int iterations = Heat.DEFAULT_ITERATIONS;
		if (be.args.length > 2) {
			iterations = Integer.parseInt(be.args[2]);
		}
		
		int threshold = Heat.DEFAULT_THRESHOLD;
		if (be.args.length > 3) {
			threshold = Integer.parseInt(be.args[3]);
		}

		double dx = (xo - xu) / (nx - 1);
		double dy = (yo - yu) / (ny - 1);
		double dt = (to - tu) / iterations;
		double dtdxsq = dt / (dx * dx);
		double dtdysq = dt / (dy * dy);

		double[][] oldm = new double[nx][ny];
		double[][] newm = new double[nx][ny];

		be.start();
		for (int timestep = 0; timestep <= iterations; timestep++) {
			compute(0, nx, nx, ny, dx, dy, dt, dtdxsq, dtdysq, threshold,
					timestep, oldm, newm);
		}
		be.end();
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

	public static void compute(int lb, int ub, int nx, int ny, double dx,
			double dy, double dt, double dtdxsq, double dtdysq, int leafmaxcol,
			int time, double[][] oldm, double[][] newm) {
		if (ub - lb > leafmaxcol) {
			int mid = (lb + ub) >>> 1;
			compute(lb, mid, nx, ny, dx, dy, dt, dtdxsq, dtdysq, leafmaxcol,
					time, oldm, newm);
			compute(mid, ub, nx, ny, dx, dy, dt, dtdxsq, dtdysq, leafmaxcol,
					time, oldm, newm);
		} else if (time == 0) // if first pass, initialize cells
			init(oldm, lb, ub, nx, ny, dx, dy);
		else if (time % 2 != 0) // alternate new/old
			comstripe(newm, oldm, lb, ub, nx, ny, dx, dy, dt, dtdxsq, dtdysq,
					time);
		else
			comstripe(oldm, newm, lb, ub, nx, ny, dx, dy, dt, dtdxsq, dtdysq,
					time);
	}

	// nx, ny, dtdxsq, dtdysq, dt
	public static void comstripe(double[][] newMat, double[][] oldMat, int lb,
			int ub, int nx, int ny, double dx, double dy, double dt,
			double dtdxsq, double dtdysq, int time) {
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

				nv[b] = cell + dtdysq * (prev - twoc + next) + dtdxsq
						* (east[b] - twoc + west[b]);

			}
		}

		edges(newMat, llb, lub, lb, ub, nx, ny, dx, dy, tu + time * dt);
	}

	// xu, dx, ny, yu, dy, nx

	/** Initializes all cells. */
	public static void init(double[][] oldm, int lb, int ub, int nx, int ny,
			double dx, double dy) {
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

		edges(oldm, llb, lub, lb, ub, nx, ny, dx, dy, 0);
	}

	// xu, dx, ny, yu, dy, nx
	/** Fills in edges with boundary values. */
	public static void edges(double[][] m, int llb, int lub, int lb, int ub,
			int nx, int ny, double dx, double dy, double t) {

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
