package aeminium.runtime.benchmarks.nbody;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FJNBody {
	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		int n = NBody.DEFAULT_ITERATIONS;
		if (be.args.length >= 1) {
			n = Integer.parseInt(be.args[0]);
		}
		int size = NBody.DEFAULT_SIZE;
		if (be.args.length >= 2) {
			size = Integer.parseInt(be.args[1]);
		}

		FJNBodySystem bodies = new FJNBodySystem(NBody.generateRandomBodies(size, 1L), new ForkJoinPool());		
		if (be.verbose)
			System.out.printf("%.9f\n", bodies.energy());
		
		be.start();
		for (int i = 0; i < n; ++i)
			bodies.advance(0.01);
		be.end();
		
		if (be.verbose)
			System.out.printf("%.9f\n", bodies.energy());
		
	}
}


class Advancer extends RecursiveAction {
	private static final long serialVersionUID = -922639424070654902L;
	
	NBody[] bodies;
	int start;
	int end;
	double dt;
	public Advancer(NBody[] bodies, int start, int end, double dt) {
		this.bodies = bodies;
		this.start = start;
		this.end = end;
		this.dt = dt;
	}
	
	@Override
	protected void compute() {
		if (end-start < 200) {
			advance();
		} else {
			int mid = (end - start) / 2 + start;
			Advancer task1 = new Advancer(bodies, start, mid, dt);
			Advancer task2 = new Advancer(bodies, mid, end, dt);
			invokeAll(task1, task2);
		}
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
	public Applier(NBody[] bodies, int start, int end, double dt) {
		this.bodies = bodies;
		this.start = start;
		this.end = end;
		this.dt = dt;
	}
	
	@Override
	protected void compute() {
		if (end-start < 100) {
			advance();
		} else {
			int mid = (end - start) / 2 + start;
			Applier task1 = new Applier(bodies, start, mid, dt);
			Applier task2 = new Applier(bodies, mid, end, dt);
			invokeAll(task1, task2);
		}
	}

	private void advance() {
		for (int i = start; i < end;i++) {
			NBody body = bodies[i];
			body.x += dt * body.vx;
			body.y += dt * body.vy;
			body.z += dt * body.vz;
		}
	}
}

final class FJNBodySystem {
	private NBody[] bodies;
	protected ForkJoinPool pool;

	public FJNBodySystem(NBody[] data, ForkJoinPool p) {
		bodies = data;
		pool = p;

		double px = 0.0;
		double py = 0.0;
		double pz = 0.0;
		for (NBody body : bodies) {
			px += body.vx * body.mass;
			py += body.vy * body.mass;
			pz += body.vz * body.mass;
		}
		bodies[0].offsetMomentum(px, py, pz);
	}

	public void advance(double dt) {
		
		Advancer t = new Advancer(bodies, 0, bodies.length, dt);
		pool.invoke(t);
		
		Applier t2 = new Applier(bodies, 0, bodies.length, dt);
		pool.invoke(t2);
	}

	public double energy() {
		double dx, dy, dz, distance;
		double e = 0.0;

		for (int i = 0; i < bodies.length; ++i) {
			NBody iBody = bodies[i];
			e += 0.5
					* iBody.mass
					* (iBody.vx * iBody.vx + iBody.vy * iBody.vy + iBody.vz
							* iBody.vz);

			for (int j = i + 1; j < bodies.length; ++j) {
				NBody jBody = bodies[j];
				dx = iBody.x - jBody.x;
				dy = iBody.y - jBody.y;
				dz = iBody.z - jBody.z;

				distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				e -= (iBody.mass * jBody.mass) / distance;
			}
		}
		return e;
	}
}