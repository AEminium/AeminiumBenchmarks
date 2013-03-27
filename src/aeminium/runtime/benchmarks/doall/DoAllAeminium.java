package aeminium.runtime.benchmarks.doall;

import java.util.Collection;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class DoAllAeminium {

	public static Runtime rt;

	// CONFIGURATIONS
	public static int numberOfTasks;
	public static int n;
	public static long taskSize;
	public static int[] a;
	public static int[] b;
	public static int[] c;

	public static void main(String args[]) {
		n = Integer.parseInt(args[0]);
		taskSize = Integer.parseInt(args[1]);
		numberOfTasks = Integer.parseInt(args[2]);

		a = new int[n];
		b = new int[n];
		c = new int[n];

		a[n - 1] = 10;
		b[n - 1] = 10;

		long initialTime = System.currentTimeMillis();
		rt = Factory.getRuntime();
		rt.init();

		int i = 0;
		while (i < numberOfTasks) {
			doallTask(Runtime.NO_PARENT, Runtime.NO_DEPS, i);
			i++;
		}

		rt.shutdown();

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);

	}

	private static Task doallTask(Task current, Collection<Task> prev, final int tid) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				int i = tid * (n / numberOfTasks);
				while (i < (tid + 1) * (n / numberOfTasks)) {
					for (long s = 0; s < taskSize; s++) {
						c[i] = a[i] + b[i];
					}
					i++;
				}
			}

		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

}
