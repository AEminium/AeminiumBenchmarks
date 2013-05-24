package aeminium.runtime.benchmarks.tests;

import java.util.ArrayList;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;

public class TestMergeDependence {
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
				count++;
				//System.out.println(count);
				double temp = count * 2;
				temp = Math.pow(temp, temp);
			}
		};

		ArrayList<Task> prev1 = new ArrayList<Task>();
		Task t1 = rt.createNonBlockingTask(testBody, (short)(Hints.SMALL | Hints.NO_CHILDREN));
		rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
		prev1.add(t1);
		
		for(int i=0;i<size;i++){
			Task t2 = rt.createNonBlockingTask(testBody, (short)(Hints.SMALL | Hints.NO_CHILDREN));
			rt.schedule(t2, Runtime.NO_PARENT, prev1);
			prev1 = new ArrayList<Task>();
			prev1.add(t2);
		}
		

		rt.shutdown();
		be.end();

		System.out.println("Count: " + count);
	}
}