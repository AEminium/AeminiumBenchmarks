package aeminium.runtime.benchmarks.nbody;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqNBody {
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

		NBodySystem bodies = new NBodySystem(NBody.generateRandomBodies(size, 1L));
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

final class NBodySystem {
	private NBody[] bodies;

	public NBodySystem(NBody[] data) {
		bodies = data;

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

		for (int i = 0; i < bodies.length; ++i) {
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

				body.vx += dx * iBody.mass * mag;
				body.vy += dy * iBody.mass * mag;
				body.vz += dz * iBody.mass * mag;
			}
		}

		for (NBody body : bodies) {
			body.x += dt * body.vx;
			body.y += dt * body.vy;
			body.z += dt * body.vz;
		}
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