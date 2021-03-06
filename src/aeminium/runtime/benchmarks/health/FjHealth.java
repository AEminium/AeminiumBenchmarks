package aeminium.runtime.benchmarks.health;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

@SuppressWarnings("serial")
public class FjHealth extends RecursiveAction {
	
	private Village village;
	private int threshold;

	public FjHealth(Village v, int t) {
		this.village = v;
		this.threshold = t;
	}

	@Override
	protected void compute() {
		explore(this.village);
	}

	protected void explore(Village v) {
		if (v.level > threshold) {
			List<FjHealth> exp = new ArrayList<FjHealth>();
			for (Village child : v.children) {
				FjHealth e = new FjHealth(child,threshold);
				exp.add(e);
			}
			invokeAll(exp);
		} else {
			for (Village child : v.children) {
				explore(child);
			}
		}
		v.tick();
	}


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

		int threshold = Health.DEFAULT_THRESHOLD;
		if (be.args.length > 2) {
			threshold = Integer.parseInt(be.args[2]);
		}

		ForkJoinPool pool = new ForkJoinPool();

		Village village = Health.allocate_village(level, 0, null);

		while (!be.stop()) {
			be.start();
			for (int i=0; i<size; i++) {
				FjHealth e = new FjHealth(village, threshold);
				pool.invoke(e);
			}
			be.end();		
		}
	}

}
