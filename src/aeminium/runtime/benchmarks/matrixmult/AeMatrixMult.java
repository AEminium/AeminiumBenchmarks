package aeminium.runtime.benchmarks.matrixmult;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
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
		
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		while (!be.stop()) {
			be.start();
			rt.init();
			Task tmain = rt.createNonBlockingTask(new Body() {

				@Override
				public void execute(Runtime rt, Task current) throws Exception {
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
							}, (short)(Hints.LOOPS | Hints.LARGE));
							rt.schedule(innerFor, current, Runtime.NO_DEPS);
							
						}
					}, Hints.LOOPS);
					rt.schedule(outerFor, current, Runtime.NO_DEPS);
				}
				
			}, Hints.LOOPS);
			rt.schedule(tmain, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
		}
	}
}
