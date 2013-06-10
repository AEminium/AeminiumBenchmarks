package aeminium.runtime.benchmarks.pi;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.utils.random.MersenneTwisterFast;

public class FjPi extends RecursiveAction {
	private static final long serialVersionUID = 175230925631318519L;
	
	public long count;
	public long n;
	public long threshold;
	
	public FjPi(long n, long th) {
		this.n = n;
		this.threshold = th;
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
	    long darts = SeqPi.DEFAULT_DART_SIZE;
	    int threshold = SeqPi.DEFAULT_THRESHOLD;
	    if (be.args.length > 1) {
	        darts = Integer.parseInt(be.args[0]);
	    }
	    if (be.args.length > 2) {
	        threshold = Integer.parseInt(be.args[1]);
	    }
	    be.start();
	    ForkJoinPool pool = new ForkJoinPool();
		
	    FjPi pi = new FjPi(darts, threshold);
	    pool.invoke(pi);
	    double pivalue = 4.0 * (double) pi.count/(double)darts;
		be.end();
		if (be.verbose) {
			System.out.println("PI = " + pivalue);
		}
	}

	@Override
	protected void compute() {
		if (n < threshold) {
			computeSeq();
		} else {
			FjPi f1 = new FjPi(n/2, threshold);
			FjPi f2 = new FjPi(n/2, threshold);
			invokeAll(f1, f2);
			this.count = f1.count + f2.count;
		}
	}
	
	protected void computeSeq() {
		double x_coord, y_coord, r; 
		MersenneTwisterFast random = new MersenneTwisterFast(1L);
		for (long i = 1; i <= n; i++)
		{
			/* generate random numbers for x and y coordinates */
			r = random.nextDouble();
			x_coord = (2.0 * r) - 1.0;
			r = random.nextDouble();
			y_coord = (2.0 * r) - 1.0;

			/* if dart lands in circle, increment score */
			if ((x_coord*x_coord + y_coord*y_coord) <= 1.0)
				count++;
		}
	}
}
