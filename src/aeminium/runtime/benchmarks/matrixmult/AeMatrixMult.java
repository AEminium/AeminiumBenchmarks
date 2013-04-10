package aeminium.runtime.benchmarks.matrixmult;

import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;

public class AeMatrixMult {

	static int first[][];
	static int second[][];
	static int result[][];
	
	public static int[][] createMatrix(int m, int n) {
		int[][] t = new int[m][n];
		for (int c=0;c<m;c++)
			for (int d=0;d<n;d++)
				t[c][d] = d * c;
		return t;
	}
	
	public static void main(String args[]) {
		long initialTime = System.currentTimeMillis();
		
		Runtime rt = Factory.getRuntime();
		rt.init();
		
		int m1 = 1000;
		if (args.length > 0) m1 = Integer.parseInt(args[0]);
		int n1 = 1000;
		if (args.length > 1) n1 = Integer.parseInt(args[1]);
		final int p = n1;
		int q1 = 1000;
		if (args.length > 2) q1 = Integer.parseInt(args[2]);
		final int m = m1;
		final int n = n1;
		final int q = q1;
		
		Task f = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				result = new int[m][q];
				first = createMatrix(m,n);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(f, Runtime.NO_PARENT, Runtime.NO_DEPS);
		Task s = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				second = createMatrix(p,q);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(s, Runtime.NO_PARENT, Runtime.NO_DEPS);

		
		
		Task outerFor = ForTask.createFor(rt, new Range(m), new ForBody<Integer>() {

			@Override
			public void iterate(final Integer c, Runtime rt, Task current) {
				Task innerFor = ForTask.createFor(rt, new Range(q), new ForBody<Integer>() {

					@Override
					public void iterate(Integer d, Runtime rt, Task inner) {
						int sum = 0;
						for (int k = 0; k < p; k++) {
							sum += first[c][k] * second[k][d];
						}
						result[c][d] = sum;
					}
				});
				rt.schedule(innerFor, current, Runtime.NO_DEPS);
				
			}
		});
		rt.schedule(outerFor, Runtime.NO_PARENT, Arrays.asList(f, s));
		

		if (args.length > 3) {
			Task echo = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					System.out.println("Product of entered matrices:-");
					for (int c = 0; c < m; c++) {
						for (int d = 0; d < q; d++)
							System.out.print(result[c][d] + "\t");
						System.out.print("\n");
					}
				}
			}, Runtime.NO_HINTS);
			rt.schedule(echo, Runtime.NO_PARENT, Arrays.asList(outerFor));
		}
		rt.shutdown();
		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}
}
