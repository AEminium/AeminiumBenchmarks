package aeminium.runtime.benchmarks.matrixmultiplication;

import java.util.ArrayList;
import java.util.Collection;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class MatrixMultiplicationAeminium {

	public static Runtime rt;

	// CONFIGURATIONS
	public static int numberOfTasks;

	public static int first[][];
	public static int second[][];
	public static int multiply[][];
	// first matrix
	public static int m = 2000;
	public static int n = 2000;
	// second matrix
	public static int p = 2000;
	public static int q = 2000;

	public static void main(String args[]) {

		long initialTime = System.currentTimeMillis();
		
		numberOfTasks=Integer.parseInt(args[0]);

		rt = Factory.getRuntime();
		rt.init();

		if (n != p) {
			System.out.println("Matrices with entered orders can't be multiplied with each other.");
		} else {
			first = new int[m][n];
			second = new int[p][q];
			multiply = new int[m][q];

			// System.out.println("Enter the elements of first matrix");
			Collection<Task> prev = new ArrayList<Task>();

			int step1 = m / numberOfTasks;
			for (int c = 0; c < m; c = c + step1) {
				Task init1 = createFirstMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, c, (c + step1));
				prev.add(init1);
			}

			// System.out.println("Enter the elements of second matrix");

			int step2 = p / numberOfTasks;
			for (int c = 0; c < p; c = c + step2) {
				Task init2 = createSecondMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, c, (c + step2));
				prev.add(init2);
			}

			// second[c][d] = in.nextInt();

			int step3 = m / numberOfTasks;
			for (int c = 0; c < m; c = c + step3) {
				multiplyMatrixTask(Runtime.NO_PARENT, prev, c, (c + step3));
			}

			rt.shutdown();

			long finalTime = System.currentTimeMillis();
			System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);

			/*
			System.out.println("First matrices:-");

			for (int c = 0; c < n; c++) {
				for (int d = 0; d < m; d++)
					System.out.print(first[c][d] + "\t");

				System.out.print("\n");
			}

			System.out.println("Second matrices:-");
			for (int c = 0; c < q; c++) {
				for (int d = 0; d < p; d++)
					System.out.print(second[c][d] + "\t");

				System.out.print("\n");
			}

			System.out.println("Product of entered matrices:-");
			for (int c = 0; c < m; c++) {
				for (int d = 0; d < q; d++)
					System.out.print(multiply[c][d] + "\t");

				System.out.print("\n");
			}
			*/

		}
	}

	private static Task createFirstMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				System.out.println("createFirstMatrix " + paramI + " " + paramP);
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < n; d++)
						first[c][d] = d * c;
				System.out.println("final createFirstMatrix");
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task createSecondMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				System.out.println("createSecondMatrix " + paramI + " " + paramP);
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < q; d++)
						second[c][d] = d * c;
				System.out.println("final createSecondMatrix");
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task multiplyMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				System.out.println("multiply");
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
				System.out.println("final multiply");
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

}
