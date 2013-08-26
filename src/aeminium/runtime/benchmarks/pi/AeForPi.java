package aeminium.runtime.benchmarks.pi;

import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.LongRange;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;
import aeminium.utils.random.MersenneTwisterFast;

public class AeForPi {
	
	static volatile int score = 0;
	final static MersenneTwisterFast random = new MersenneTwisterFast(1L);
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
	    long dartsc = SeqPi.DEFAULT_DART_SIZE;
	    if (be.args.length > 0) {
	        dartsc = Integer.parseInt(be.args[0]);
	    }
	    final long darts = dartsc;
	    
	    be.start();
		Runtime rt = Factory.getRuntime();
		
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();

		Task iterations = ForTask.createFor(rt, new LongRange(darts), new ForBody<Long>() {
			@Override
			public void iterate(Long o, Runtime rt, Task current) {
				double x_coord, y_coord;
				x_coord = (2.0 * random.nextDouble()) - 1.0;
				y_coord = (2.0 * random.nextDouble()) - 1.0;
				if ((x_coord*x_coord + y_coord*y_coord) <= 1.0) {
					score++;
				}
			}		
		 }, (short) (Hints.LARGE | Hints.NO_CHILDREN));
		 rt.schedule(iterations, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		rt.shutdown();
		double pi = 4.0 * (double)score/(double)darts;
		be.end();
		if (be.verbose) {
			System.out.println("PI = " + pi);
		}
	}
}
