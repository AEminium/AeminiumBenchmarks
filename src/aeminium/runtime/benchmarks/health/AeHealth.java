package aeminium.runtime.benchmarks.health;

import java.util.Arrays;
import java.util.Collection;

import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeHealth {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int size = Health.sim_time;
		int level = Health.sim_level;
		
		
		if (be.args.length > 0) {
			level = Integer.parseInt(be.args[0]);
		}
		
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		}

		final int threshold;
		
		if (be.args.length > 2) {
			threshold = Integer.parseInt(be.args[2]);
		} else {
			threshold = Health.DEFAULT_THRESHOLD;
		}

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		final AeVillage village = (AeVillage) Health.allocate_village(level, 0, null, true, threshold);

		while (!be.stop()) {
			be.start();
			rt.init();
			Collection<Task> previous = Runtime.NO_DEPS;
			for (int i=0; i<size; i++) {
				Task t = rt.createNonBlockingTask(village, Runtime.NO_HINTS);
				rt.schedule(t, Runtime.NO_PARENT, previous);
				previous = Arrays.asList(t);
			}
			rt.shutdown();
			be.end();		
		}
	}
}
