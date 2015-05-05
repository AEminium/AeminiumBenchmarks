package aeminium.runtime.benchmarks.nbody;

import java.util.Queue;
import jsr166e.ForkJoinPool;
import jsr166e.LinkedBlockingDeque;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjNBody {
	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		int n = NBody.DEFAULT_ITERATIONS;
		if (be.args.length > 0) {
			n = Integer.parseInt(be.args[0]);
		}
		int size = NBody.DEFAULT_SIZE;
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		}
		int advance_t = NBody.ADVANCE_THRESHOLD;
		if (be.args.length > 2) {
			advance_t = Integer.parseInt(be.args[2]);
		}
		int apply_t = NBody.APPLY_THRESHOLD;
		if (be.args.length > 3) {
			apply_t = Integer.parseInt(be.args[3]);
		}

		while (!be.stop()) {
			FJNBodySystem bodies = new FJNBodySystem(NBody.generateRandomBodies(size, 1L), new ForkJoinPool());
			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
			be.start();

			for (int i = 0; i < n; ++i)
				bodies.advance(0.01, advance_t, apply_t);
			be.end();

			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
		}

	}
}

class Advancer extends RecursiveAction {
	private static final long serialVersionUID = -922639424070654902L;
	private static Queue<Advancer> queue = new LinkedBlockingDeque<Advancer>();

	NBody[] bodies;
	int start;
	int end;
	double dt;
	int threshold;

	public Advancer(NBody[] bodies, int start, int end, double dt, int threshold) {
		this.bodies = bodies;
		this.start = start;
		this.end = end;
		this.dt = dt;
		this.threshold = threshold;
	}

	private Advancer getAdvancer(int st, int e) {
		Advancer a = queue.poll();
		if (a == null) return new Advancer(bodies, st, e, dt, threshold);
		a.reInit(st, e);
		return a;
	}

	@Override
	protected void compute() {
		if (Benchmark.useThreshold ? end - start < threshold : !this.shouldFork()) {
			advance();
		} else {
			int mid = (end - start) / 4 + start;
			Advancer task1 = getAdvancer(start, mid);
			Advancer task2 = getAdvancer(mid, end);
			invokeAll(task1, task2);
			queue.add(task1);
			queue.add(task2);
		}
	}

	public void reInit(int st, int e) {
		this.start = st;
		this.end = e;
		this.reinitialize();
	}

	private void advance() {
		for (int i = start; i < end; ++i) {
			NBody iBody = bodies[i];
			for (int j = i + 1; j < bodies.length; ++j) {
				final NBody body = bodies[j];
				double dx = iBody.x - body.x;
				double dy = iBody.y - body.y;
				double dz = iBody.z - body.z;

				double dSquared = dx * dx + dy * dy + dz * dz;
				double distance = Math.sqrt(dSquared);
				double mag = dt / (dSquared * distance);

				iBody.vx -= dx * body.mass * mag;
				iBody.vy -= dy * body.mass * mag;
				iBody.vz -= dz * body.mass * mag;

				synchronized (body) {
					body.vx += dx * iBody.mass * mag;
					body.vy += dy * iBody.mass * mag;
					body.vz += dz * iBody.mass * mag;
				}
			}
		}
	}
}

class Applier extends RecursiveAction {
	private static final long serialVersionUID = -922639424070654902L;

	NBody[] bodies;
	int start;
	int end;
	double dt;
	int threshold;

	public Applier(NBody[] bodies, int start, int end, double dt, int threshold) {
		this.bodies = bodies;
		this.start = start;
		this.end = end;
		this.dt = dt;
		this.threshold = threshold;
	}

	@Override
	protected void compute() {
		if (Benchmark.useThreshold ? end - start < threshold : !this.shouldFork()) {
			advance();
		} else {
			int mid = (end - start) / 2 + start;
			Applier task1 = new Applier(bodies, start, mid, dt, threshold);
			Applier task2 = new Applier(bodies, mid, end, dt, threshold);
			invokeAll(task1, task2);
		}
	}

	private void advance() {
		for (int i = start; i < end; i++) {
			NBody body = bodies[i];
			body.x += dt * body.vx;
			body.y += dt * body.vy;
			body.z += dt * body.vz;
		}
	}
}

class FJNBodySystem extends NBodySystem {

	protected ForkJoinPool pool;

	public FJNBodySystem(NBody[] data, ForkJoinPool p) {
		super(data);
		pool = p;
	}

	public void advance(double dt, int advance_t, int apply_t) {

		Advancer t = new Advancer(bodies, 0, bodies.length, dt, advance_t);
		pool.execute(t);

		Applier t2 = new Applier(bodies, 0, bodies.length, dt, apply_t);
		pool.invoke(t2);
		pool.invoke(t);
	}
}