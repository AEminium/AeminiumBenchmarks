package aeminium.runtime.benchmarks.pi;

import java.util.concurrent.ThreadLocalRandom;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqPi {

	public static final int DEFAULT_DART_SIZE = 100000000;
	public static final int DEFAULT_THRESHOLD = 100000;
	public static final int DEFAULT_BLOCK = 128;

	public static ThreadLocalRandom random = ThreadLocalRandom.current();
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		long dartsc = DEFAULT_DART_SIZE;
		if (be.args.length > 0) {
			dartsc = Integer.parseInt(be.args[0]);
		}
		while (!be.stop()) {
			be.start();
			double x_coord, y_coord, r;
			long score = 0;
			for (long n = 1; n <= dartsc; n++) {
				/* generate random numbers for x and y coordinates */
				r = random.nextDouble();
				x_coord = (2.0 * r) - 1.0;
				r = random.nextDouble();
				y_coord = (2.0 * r) - 1.0;

				/* if dart lands in circle, increment score */
				if ((x_coord * x_coord + y_coord * y_coord) <= 1.0) score++;
			}
			double d = 4.0 * (double) score / (double) dartsc;
			be.end();
			if (be.verbose) {
				System.out.println("PI = " + d);
			}
		}
	}
}
