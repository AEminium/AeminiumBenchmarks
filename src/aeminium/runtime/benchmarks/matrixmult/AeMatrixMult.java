package aeminium.runtime.benchmarks.matrixmult;

import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeMatrixMult {

	static int first[][];
	static int second[][];
	static int result[][];
	
	public static void main(String args[]) {
		Benchmark be = new Benchmark(args);
		
		int m1 = Matrix.DEFAULT_M;
		if (be.args.length > 0) m1 = Integer.parseInt(be.args[0]);
		int n1 = Matrix.DEFAULT_N;
		if (be.args.length > 1) n1 = Integer.parseInt(be.args[1]);
		final int p = n1;
		int q1 = Matrix.DEFAULT_Q;
		if (be.args.length > 2) q1 = Integer.parseInt(be.args[2]);
		final int m = m1;
		final int n = n1;
		final int q = q1;
		first = Matrix.createMatrix(m,n);
		second = Matrix.createMatrix(p,q);
		result = new int[m][q];
		
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		
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
		rt.schedule(outerFor, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
		be.end();

		if (be.verbose) {
			System.out.println("Product of entered matrices:-");
			for (int c = 0; c < m; c++) {
				for (int d = 0; d < q; d++)
					System.out.print(result[c][d] + "\t");
				System.out.print("\n");
			}
		}
	}
}
