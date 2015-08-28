package aeminium.runtime.benchmarks.pi;

import java.util.concurrent.ThreadLocalRandom;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;
import aeminium.utils.random.MersenneTwisterFast;

public class AePi {
	
	public static ThreadLocalRandom random = ThreadLocalRandom.current();
	
	public static class AePiBody implements Body {
		public volatile long value;
		private int threshold;

		public AePiBody(long n, int threshold) {
			this.value = n;
			this.threshold = threshold;
		}

		public long seqPi(long n) {
			MersenneTwisterFast random = new MersenneTwisterFast(1L);
			double x_coord, y_coord;
			long score = 0;
			for (long i = 0; i < n; i++) {
				x_coord = (2.0 * random.nextDouble()) - 1.0;
				y_coord = (2.0 * random.nextDouble()) - 1.0;

				if ((x_coord * x_coord + y_coord * y_coord) <= 1.0) score++;
			}
			return score;
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if (value < 1 || (Benchmark.useThreshold ? value < threshold : !rt.parallelize(current))) {
				value = seqPi(value);
			} else {
				AePiBody b1 = new AePiBody(value / 2, threshold);
				Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);

				AePiBody b2 = new AePiBody(value / 2, threshold);
				Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);

				t1.getResult();
				t2.getResult();

				value = b1.value + b2.value;
			}
		}
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		long darts = SeqPi.DEFAULT_DART_SIZE;
		int threshold = SeqPi.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			darts = Integer.parseInt(be.args[0]);
		}
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		while (!be.stop()) {
			be.start();
			rt.init();
			AePiBody body = new AePi.AePiBody(darts, threshold);
			Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
			rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
			if (be.verbose) {
				System.out.println("PI = " + (4.0 * (double) body.value / (double) darts));
			}
		}

	}
}
