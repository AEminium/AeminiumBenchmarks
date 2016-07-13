package aeminium.runtime.benchmarks.nhknapsack;

import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjNH {
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
		if (be.args.length > 2) threshold = Integer.parseInt(args[2]);

		while (!be.stop()) {
			int[][] objects = NH.importDataObjects(fname, dim);
			DominanceMethod dom = new ForkJoinDominance(threshold);
			be.start();
			int[] paretoFront = NH.computeParetoNH(objects, dom);
			be.end();
			if (be.verbose) NH.printPareto(paretoFront);
		}

	}
	
	static class ForkJoinDominance implements DominanceMethod {

		int threshold;
		public ForkJoinDominance(int threshold) {
			this.threshold = threshold;
		}
		@Override
		public int[] getNonDominated(int[] evals) {
			int[] next = new int[evals.length];
			RecursiveNonDominated r1 = new RecursiveNonDominated(evals, next, 0, evals.length, threshold);
			r1.invoke();
			return Arrays.copyOf(next, r1.paretoSize);
		}
		
	}
	
	@SuppressWarnings("serial")
	static class RecursiveNonDominated extends RecursiveAction {
		int[] evals;
		int[] next;
		int start, size;
		public int paretoSize = 0;
		public int threshold;
		public RecursiveNonDominated(int[] evals, int[] next, int st, int size, int threshold) {
			this.evals = evals;
			this.next = next;
			this.start = st;
			this.size = size;
			this.threshold = threshold;
		}
		@Override
		protected void compute() {
			if (size >= threshold * NH.NDIM) {
				// Subdivide
				int half1 = size/2;
				int half2 = size - half1;
				RecursiveNonDominated r1 = new RecursiveNonDominated(evals, next, start, half1, threshold);
				RecursiveNonDominated r2 = new RecursiveNonDominated(evals, next, start + half1, half2, threshold);
				r1.fork();
				r2.invoke();
				r1.join();
				if (r1.paretoSize != half1) System.arraycopy(next, start + half1, next, start + r1.paretoSize, r2.paretoSize);
				paretoSize = r1.paretoSize + r2.paretoSize;
				return;
			}
			for (int i = start; i<start+size; i+=NH.NDIM) {
				boolean isDominated = false;
				for (int j=i; j<evals.length; j+=NH.NDIM) {
					isDominated = false;
					for (int k=0;k<NH.NDIM; k++) {
						boolean cond = (k == 0) ? evals[i+k] > evals[j+k] : evals[i+k] < evals[j+k];
						if (cond) {
							isDominated = true;
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
