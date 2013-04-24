package aeminium.runtime.benchmarks.pi;

import java.util.concurrent.atomic.AtomicInteger;

import aeminium.runtime.Body;
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
	
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
	    long dartsc = SeqPi.DEFAULT_DART_SIZE;
	    if (be.args.length > 1) {
	        dartsc = Integer.parseInt(be.args[0]);
	    }
	    final long darts = dartsc;
	    
	    be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();

		final MersenneTwisterFast random = new MersenneTwisterFast(1L);
		
		final AtomicInteger score = new AtomicInteger(0);
		
		Body compute = new Body() {

			@Override
			public void execute(final Runtime rt, final Task current) throws Exception {
				 Task iterations = ForTask.createFor(rt, new LongRange(darts), new ForBody<Long>() {
					@Override
					public void iterate(Long o, Runtime rt, Task current) {
						double x_coord, y_coord, r; 
						/* generate random numbers for x and y coordinates */
						r = random.nextDouble();
						x_coord = (2.0 * r) - 1.0;
						r = random.nextDouble();
						y_coord = (2.0 * r) - 1.0;

						/* if dart lands in circle, increment score */
						if ((x_coord*x_coord + y_coord*y_coord) <= 1.0) {
							score.getAndIncrement();
						}
					}		
				 });
				 rt.schedule(iterations, current, Runtime.NO_DEPS);
			}
		};
		
		Task controller = rt.createNonBlockingTask(compute, Runtime.NO_HINTS);
		rt.schedule(controller, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		rt.shutdown();
		double pi = 4.0 * (double)score.get()/(double)darts;
		be.end();
		if (be.verbose) {
			System.out.println("PI = " + pi);
		}
	}
}
