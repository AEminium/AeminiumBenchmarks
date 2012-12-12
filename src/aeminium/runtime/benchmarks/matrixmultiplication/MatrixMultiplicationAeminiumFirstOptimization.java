package aeminium.runtime.benchmarks.matrixmultiplication;

import java.util.ArrayList;
import java.util.Collection;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class MatrixMultiplicationAeminiumFirstOptimization {

	public static Runtime rt;

	// CONFIGURATIONS
	public static int numberOfTasks;

	public static int first[][];
	public static int second[][];
	public static int multiply[][];
	// first matrix
	public static int m;
	public static int n;
	// second matrix
	public static int p;
	public static int q;

	public static void main(String args[]) {

		long initialTime = System.currentTimeMillis();

		m = Integer.parseInt(args[0]);
		n = Integer.parseInt(args[1]);

		p = Integer.parseInt(args[2]);
		q = Integer.parseInt(args[3]);

		numberOfTasks = Integer.parseInt(args[4]);

		rt = Factory.getRuntime();
		rt.init();

		if (n != p) {
			System.out.println("Matrices with entered orders can't be multiplied with each other.");
		} else {
			first = new int[m][n];
			second = new int[p][q];
			multiply = new int[m][q];

			// System.out.println("Enter the elements of first matrix");
			Collection<Task> prev1 = new ArrayList<Task>();
			Collection<Task> prev2 = new ArrayList<Task>();
			Collection<Task> prev3 = new ArrayList<Task>();

			int step1 = m / numberOfTasks;
			for (int c = 0; c < m; c = c + step1) {
				Task init1 = createFirstMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, c, (c + step1));
				prev1.add(init1);
			}

			// System.out.println("Enter the elements of second matrix");

			int step2 = p / numberOfTasks;
			for (int c = 0; c < p; c = c + step2) {
				Task init2 = createSecondMatrixTask(Runtime.NO_PARENT, prev1, c, (c + step2));
				prev2.add(init2);
			}

			// second[c][d] = in.nextInt();

			/*
			 * int step3 = m / numberOfTasks; for (int c = 0; c < m; c = c +
			 * step3) { multiplyMatrixTask(Runtime.NO_PARENT, prev, c, (c +
			 * step3)); }
			 */
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 0, 240);
			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 240, 480);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 480, 720);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 720, 960);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 960, 1200);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 1200, 1440);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 1440, 1680);

			multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, 1680, 1920);

			rt.shutdown();

			long finalTime = System.currentTimeMillis();
			System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);

			/*
			 * System.out.println("First matrices:-");
			 * 
			 * for (int c = 0; c < n; c++) { for (int d = 0; d < m; d++)
			 * System.out.print(first[c][d] + "\t");
			 * 
			 * System.out.print("\n"); }
			 * 
			 * System.out.println("Second matrices:-"); for (int c = 0; c < q;
			 * c++) { for (int d = 0; d < p; d++) System.out.print(second[c][d]
			 * + "\t");
			 * 
			 * System.out.print("\n"); }
			 * 
			 * System.out.println("Product of entered matrices:-"); for (int c =
			 * 0; c < m; c++) { for (int d = 0; d < q; d++)
			 * System.out.print(multiply[c][d] + "\t");
			 * 
			 * System.out.print("\n"); }
			 */

		}
	}

	private static Task createFirstMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < n; d++)
						first[c][d] = d * c;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task createSecondMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < q; d++)
						second[c][d] = d * c;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task multiplyMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				int sum = 0;
				for (int c = paramI; c < paramP; c++) {
					for (int d = 0; d < q; d++) {
						for (int k = 0; k < p; k++) {
							sum = sum + first[c][k] * second[k][d];
						}
						multiply[c][d] = sum;
						sum = 0;
					}
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

}
