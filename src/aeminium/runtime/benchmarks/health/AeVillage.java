package aeminium.runtime.benchmarks.health;

import java.util.ArrayList;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;

public class AeVillage extends Village implements Body {

	int threshold;
	
	public AeVillage(int threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public void execute(Runtime rt, Task current) throws Exception {
		if (Benchmark.useThreshold ? this.level > threshold : rt.parallelize(current)) {
			ArrayList<Task> waits = new ArrayList<Task>();
			for (Village child : this.children) {
				AeVillage c2 = (AeVillage) child;
				Task t2 = rt.createNonBlockingTask(c2, Hints.RECURSION);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);
				waits.add(t2);
			}
			for (Task t : waits) t.getResult();
		} else {
			for (Village child : this.children) {
				AeVillage c2 = (AeVillage) child;
				c2.execute(rt, current);
			}
		}
		this.tick();
	}

	
	protected static void sim_village(Runtime rt, Task current, final Village village, final int threshold) {
		Task t = ForTask.createFor(rt, village.children, new ForBody<Village>() {

			@Override
			public void iterate(Village i, Runtime rt, Task current) {
				sim_village(rt, current, i, threshold);
			}

		}, Runtime.NO_HINTS);
		rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
		t.getResult();
		village.tick();
	}
}
