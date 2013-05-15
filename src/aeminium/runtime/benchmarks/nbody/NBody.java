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

public final class NBody {
	public static final int DEFAULT_ITERATIONS = 5000;
	public static final int DEFAULT_SIZE = 1000;

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