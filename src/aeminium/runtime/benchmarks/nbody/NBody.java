package aeminium.runtime.benchmarks.nbody;

import aeminium.utils.random.MersenneTwisterFast;

/*
 * Copyright (c) 2011.  Peter Lawrey
 *
 * "THE BEER-WARE LICENSE" (Revision 128)
 * As long as you retain this notice you can do whatever you want with this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return
 * There is no warranty.
 */

/* The Computer Language Benchmarks Game

 http://shootout.alioth.debian.org/

 contributed by Mark C. Lewis
 modified slightly by Chad Whipkey
 */
// run with: java  -server -XX:+TieredCompilation -XX:+AggressiveOpts nbody 50000000

public class NBody {
	public static final int DEFAULT_ITERATIONS = 5;
	public static final int DEFAULT_SIZE = 2000;

	public static final int ADVANCE_THRESHOLD = 1000;
	public static final int APPLY_THRESHOLD = 100;

	static final double PI = 3.141592653589793;
	static final double SOLAR_MASS = 4 * PI * PI;

	public double x;
	public double y;
	public double z;
	public double vx;
	public double vy;
	public double vz;
	public double mass;

	public NBody(MersenneTwisterFast r) {
		x = r.nextDouble();
		y = r.nextDouble();
		z = r.nextDouble();
		vx = r.nextDouble();
		vy = r.nextDouble();
		vz = r.nextDouble();
		mass = r.nextDouble();

	}

	public static NBody[] generateRandomBodies(int n, long seed) {
		MersenneTwisterFast random = new MersenneTwisterFast(seed);
		NBody[] r = new NBody[n];
		for (int i = 0; i < n; i++)
			r[i] = new NBody(random);
		return r;
	}

	NBody offsetMomentum(double px, double py, double pz) {
		vx = -px / SOLAR_MASS;
		vy = -py / SOLAR_MASS;
		vz = -pz / SOLAR_MASS;
		return this;
	}

}

abstract class NBodySystem {
	protected NBody[] bodies;

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

	public double energy() {
		double dx, dy, dz, distance;
		double e = 0.0;

		for (int i = 0; i < bodies.length; ++i) {
			NBody iBody = bodies[i];
			e += 0.5 * iBody.mass * (iBody.vx * iBody.vx + iBody.vy * iBody.vy + iBody.vz * iBody.vz);

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