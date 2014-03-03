package aeminium.runtime.benchmarks.raytracer;

/**************************************************************************
 * * Java Grande Forum Benchmark Suite - Version 2.0 * * produced by * * Java
 * Grande Benchmarking Project * * at * * Edinburgh Parallel Computing Centre *
 * * email: epcc-javagrande@epcc.ed.ac.uk * * Original version of this code by *
 * Florian Doyon (Florian.Doyon@sophia.inria.fr) * and Wilfried Klauser
 * (wklauser@acm.org) * * This version copyright (c) The University of
 * Edinburgh, 1999. * All rights reserved. * *
 **************************************************************************/

@SuppressWarnings("serial")
public class View implements java.io.Serializable {
	public final Vec from;
	public final Vec at;
	public final Vec up;
	public final double dist;
	public final double angle;
	public final double aspect;

	public View(Vec from, Vec at, Vec up, double dist, double angle, double aspect) {
		this.from = from;
		this.at = at;
		this.up = up;
		this.dist = dist;
		this.angle = angle;
		this.aspect = aspect;
	}
}
