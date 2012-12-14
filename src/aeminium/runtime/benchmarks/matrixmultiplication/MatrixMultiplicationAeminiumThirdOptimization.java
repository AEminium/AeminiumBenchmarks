package aeminium.runtime.benchmarks.matrixmultiplication;

import java.util.ArrayList;
import java.util.Collection;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class MatrixMultiplicationAeminiumThirdOptimization {

	public static Runtime rt;

	// CONFIGURATIONS
	public static int numberOfTasks;

	public static void main(String args[]) {

		long initialTime = System.currentTimeMillis();

		int m = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);

		int p = Integer.parseInt(args[2]);
		int q = Integer.parseInt(args[3]);

		numberOfTasks = Integer.parseInt(args[4]);

		rt = Factory.getRuntime();
		rt.init();
		
		Matrix matrix=new Matrix(n, m, p, q);

		if (n != p) {
			System.out.println("Matrices with entered orders can't be multiplied with each other.");
		} else {

			Collection<Task> prev1 = new ArrayList<Task>();
			Task init1 = createMatrix(Runtime.NO_PARENT, Runtime.NO_DEPS,matrix);
			prev1.add(init1);
			// second[c][d] = in.nextInt();

			multiplyTask(Runtime.NO_PARENT, prev1,matrix);

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

	private static Task createFirstMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP,final Matrix matrix) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < matrix.n; d++)
						matrix.first[c][d] = d * c;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task createSecondMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP,final Matrix matrix) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				for (int c = paramI; c < paramP; c++)
					for (int d = 0; d < matrix.q; d++)
						matrix.second[c][d] = d * c;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task multiplyMatrixTask(Task current, Collection<Task> prev, final int paramI, final int paramP,final Matrix matrix) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				int sum = 0;
				for (int c = paramI; c < paramP; c++) {
					for (int d = 0; d < matrix.q; d++) {
						for (int k = 0; k < matrix.p; k++) {
							sum = sum + matrix.first[c][k] * matrix.second[k][d];
						}
						matrix.multiply[c][d] = sum;
						sum = 0;
					}
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task createMatrix(Task current, Collection<Task> prev,final Matrix matrix) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				// System.out.println("Enter the elements of first matrix");
				Collection<Task> prev1 = new ArrayList<Task>();
				Collection<Task> prev2 = new ArrayList<Task>();

				int step1 = matrix.m / numberOfTasks;
				for (int c = 0; c < matrix.m; c = c + step1) {
					Task init1 = createFirstMatrixTask(current, Runtime.NO_DEPS, c, (c + step1),matrix);
					prev1.add(init1);
				}

				// System.out.println("Enter the elements of second matrix");

				int step2 = matrix.p / numberOfTasks;
				for (int c = 0; c < matrix.p; c = c + step2) {
					Task init2 = createSecondMatrixTask(current, prev1, c, (c + step2),matrix);
					prev2.add(init2);
				}

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task multiplyTask(Task current, Collection<Task> prev,final Matrix matrix) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 0, 240);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 240, 480);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 480, 720);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 720, 960);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 960, 1200);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 1200, 1440);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 1440, 1680);
				// multiplyMatrixTask(current, Runtime.NO_DEPS, 1680, 1920);

				int step3 = matrix.m / numberOfTasks;
				for (int c = 0; c < matrix.m; c = c + step3) {
					multiplyMatrixTask(Runtime.NO_PARENT, Runtime.NO_DEPS, c, (c + step3),matrix);
				}

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

}

class Matrix {
	public int first[][];
	public int second[][];
	public int multiply[][];
	// first matrix
	public int m;
	public int n;
	// second matrix
	public int p;
	public int q;

	Matrix(int n, int m, int p, int q) {
		this.n = n;
		this.m = m;
		this.p = p;
		this.q = q;

		first = new int[m][n];
		second = new int[p][q];
		multiply = new int[m][q];

	}
}
