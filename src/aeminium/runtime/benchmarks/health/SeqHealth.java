package aeminium.runtime.benchmarks.health;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqHealth {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);

		int size = Health.sim_time;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}

		Village village = Health.allocate_village(Health.sim_level, 0, null);

		while (!be.stop()) {
			be.start();
			for (int i=0; i<size; i++) {
				sim_village(village);
			}
			be.end();		
		}
	}

	protected static void sim_village(Village village) {
		for (Village child : village.children) {
			sim_village(child);
		}
		village.tick();
	}
}
