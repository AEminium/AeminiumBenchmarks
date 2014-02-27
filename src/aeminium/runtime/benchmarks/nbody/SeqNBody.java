package aeminium.runtime.benchmarks.nbody;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqNBody {
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

		while (!be.stop()) {
			SeqNBodySystem bodies = new SeqNBodySystem(NBody.generateRandomBodies(size, 1L));
			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
			be.start();
			for (int i = 0; i < n; ++i)
				bodies.advance(0.01);
			be.end();
			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
		}

	}
}

final class SeqNBodySystem extends NBodySystem {

	public SeqNBodySystem(NBody[] data) {
		super(data);
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

}