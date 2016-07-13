package aeminium.runtime.benchmarks.nhknapsack;

import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeNH {
	
	
	public static Runtime rt;
	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		String fname = null;
		if (be.args.length > 0) {
			fname = be.args[0];
		}
		int dim = NH.NDIM;
		if (be.args.length > 1) dim = Integer.parseInt(be.args[1]);
		NH.NDIM = dim;
		
		int threshold = NH.threshold;
		if (be.args.length > 2) threshold = Integer.parseInt(be.args[2]);
		
		int lookahead = NH.lookahead;
		if (be.args.length > 3) lookahead = Integer.parseInt(be.args[3]);
		
		int th = NH.lookahead_threshold;
		if (be.args.length > 4) th = Integer.parseInt(be.args[4]);

		rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		while (!be.stop()) {
			int[][] objects = NH.importDataObjects(fname, dim);
			DominanceMethod dom = new AeminiumDominance(threshold);
			be.start();
			rt.init();
			int[] paretoFront = NH.computeParetoNHWithLookAhead(objects, dom, lookahead, th);
			rt.shutdown();
			be.end();
			if (be.verbose) NH.printPareto(paretoFront);
		}

	}
	
	static class AeminiumDominance implements DominanceMethod {

		int threshold;
		public AeminiumDominance(int threshold) {
			this.threshold = threshold;
		}
		@Override
		public int[] getNonDominated(int[] evals) {
			int[] next = new int[evals.length];
			NHBody b1 = new NHBody(evals, next, 0, evals.length, threshold);
			Task t1 = AeNH.rt.createNonBlockingTask(b1, Hints.RECURSION);
			AeNH.rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			t1.getResult();
			return Arrays.copyOf(next, b1.paretoSize);
		}
		
	}
	
	public static class NHBody implements Body {
		int[] evals;
		int[] next;
		int start, size;
		public int paretoSize = 0;
		public int threshold;
		public NHBody(int[] evals, int[] next, int st, int size, int threshold) {
			this.evals = evals;
			this.next = next;
			this.start = st;
			this.size = size;
			this.threshold = threshold;
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if (Benchmark.useThreshold ? size >= threshold * NH.NDIM : rt.parallelize(current) && size >= 4 * NH.NDIM) {
				int half1 = (size/NH.NDIM)/2 * NH.NDIM;
				int half2 = size - half1;
				NHBody b1 = new NHBody(evals, next, start, half1, threshold);
				Task t1 = AeNH.rt.createNonBlockingTask(b1, Hints.RECURSION);
				AeNH.rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
				NHBody b2 = new NHBody(evals, next, start + half1, half2, threshold);
				Task t2 = AeNH.rt.createNonBlockingTask(b2, Hints.RECURSION);
				AeNH.rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);
				t1.getResult();
				t2.getResult();
				if (b1.paretoSize != half1) System.arraycopy(next, start + half1, next, start + b1.paretoSize, b2.paretoSize);
				paretoSize = b1.paretoSize + b2.paretoSize;
			} else { 
				for (int i = start; i<start+size; i+=NH.NDIM) {
					boolean isDominated = false;
					for (int j=0; j<evals.length; j+=NH.NDIM) {
						isDominated = true;
						for (int k=0;k<NH.NDIM; k++) {
							boolean cond = (k == 0) ? evals[i+k] > evals[j+k] : evals[i+k] < evals[j+k];
							if (!cond) {
								isDominated = false;
								break;
							}
						}
						if (isDominated) break;
					}
					if (!isDominated) {
						for (int k=0; k<NH.NDIM; k++) {
							next[start + paretoSize + k] = evals[i+k];
						}
						paretoSize += NH.NDIM;
					}
				}
			}
			
		}
	}
}
