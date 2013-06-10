package aeminium.runtime.benchmarks.tests;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;

public class TestMerge {
	public static int count = 0;

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		be.start();
		
		Runtime rt = Factory.getRuntime();
		rt.init();
		int size = Integer.parseInt(args[0]);


		Body testBody = new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				synchronized (this) {
					count++;
					double temp=count*2;
					temp=Math.pow(temp, temp);
				}
			}
		};

		for (int i = 0; i < size; i++) {
			Task testTask = rt
					.createNonBlockingTask(testBody, (short)(Hints.SMALL | Hints.LOOPS | Hints.NO_CHILDREN));
			rt.schedule(testTask, Runtime.NO_PARENT, Runtime.NO_DEPS);
		}

		
		rt.shutdown();
		be.end();
		
		System.out.println("Count: " + count);
	}
}