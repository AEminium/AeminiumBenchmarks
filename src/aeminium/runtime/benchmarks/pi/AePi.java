package aeminium.runtime.benchmarks.pi;

import java.util.ArrayList;
import java.util.List;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;
import aeminium.utils.random.MersenneTwisterFast;

public class AePi {

	public static class PiBody implements Body {
		private long darts = 0;
		public long score = 0;
		
		public PiBody(long darts)
		{
			this.darts = darts;
		}

		@Override
		public void execute(Runtime rt, Task current) throws Exception
		{
			double x_coord, y_coord, r; 
			MersenneTwisterFast random = new MersenneTwisterFast(1L);

			/* "throw darts at board" */
			for (long n = 1; n <= darts; n++)
			{
				/* generate random numbers for x and y coordinates */
				r = random.nextDouble();
				x_coord = (2.0 * r) - 1.0;
				r = random.nextDouble();
				y_coord = (2.0 * r) - 1.0;

				/* if dart lands in circle, increment score */
				if ((x_coord*x_coord + y_coord*y_coord) <= 1.0)
					score++;
			}
		}
	}
	
	public static class PiMerger implements Body {

		List<PiBody> workers;
		long darts;
		MainBody parent;
		
		public PiMerger(List<PiBody> workers, long darts, MainBody parent) {
			this.workers = workers;
			this.darts = darts;
			this.parent = parent;
		}
		
		@Override
		public void execute(Runtime rt, Task current)
				throws Exception {
			long score = 0;
			for (PiBody worker: workers) {
				score += worker.score;
			}
			parent.value = 4.0 * (double)score/(double)darts;
			
		}
		
	}
	
	public static class MainBody implements Body {
		public double value;
		long darts;
		int threshold;
		Runtime rt;
		
		public MainBody(final Runtime rt, final int threshold, final long darts) {
			this.darts = darts;
			this.threshold = threshold;
			this.rt = rt;
		}
		
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			final List<Task> workers = new ArrayList<Task>();
			final List<PiBody> workerBodies = new ArrayList<PiBody>();
			for (int i=0; i<threshold; i++) {
				PiBody body = new AePi.PiBody(darts/threshold);
				Task worker = rt.createNonBlockingTask(body, (short)(Hints.LARGE | Hints.LOOPS | Hints.NO_CHILDREN));
				workers.add(worker);
				workerBodies.add(body);
				rt.schedule(worker, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}
			
			Task merger = rt.createNonBlockingTask(new PiMerger(workerBodies, darts, this), (short)(Hints.SMALL | Hints.LOOPS | Hints.NO_CHILDREN));
			rt.schedule(merger, Runtime.NO_PARENT, workers);
		}
	};
	

	public static Body createPiBody(final Runtime rt, final int n) {
		return new AePi.PiBody(n);
	}
	
	public static MainBody createController(final Runtime rt, final int threshold, final long darts) {
		return new MainBody(rt, threshold, darts);
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
	    long darts = SeqPi.DEFAULT_DART_SIZE;
	    int threshold = 64;
	    if (be.args.length > 1) {
	        darts = Integer.parseInt(be.args[0]);
	    }
	    if (be.args.length > 2) {
	        threshold = Integer.parseInt(be.args[1]);
	    }
	    be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		MainBody body = AePi.createController(rt, threshold, darts);
		Task controller = rt.createNonBlockingTask(body, (short)(Hints.LOOPS));
		rt.schedule(controller, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("PI = " + body.value);
		}
	}
}
