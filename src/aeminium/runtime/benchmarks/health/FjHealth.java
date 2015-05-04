package aeminium.runtime.benchmarks.health;

import java.util.ArrayList;
import java.util.List;
import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

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
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}

		int threshold = Health.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			threshold = Integer.parseInt(be.args[0]);
		}

		ForkJoinPool pool = new ForkJoinPool();

		Village village = Health.allocate_village(Health.sim_level, 0, null);

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
