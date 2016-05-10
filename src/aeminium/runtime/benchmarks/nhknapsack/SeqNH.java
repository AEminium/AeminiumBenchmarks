package aeminium.runtime.benchmarks.nhknapsack;

import java.util.Arrays;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqNH {
	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		String fname = null;
		if (be.args.length > 0) {
			fname = be.args[0];
		}
		int dim = NH.NDIM;
		if (be.args.length > 1) dim = Integer.parseInt(be.args[1]);
		NH.NDIM = dim;

		while (!be.stop()) {
			int[][] objects = NH.importDataObjects(fname, dim);
			DominanceMethod dom = new SequentialDominance();
			be.start();
			int[] paretoFront = NH.computeParetoNH(objects, dom);
			be.end();
			if (be.verbose) NH.printPareto(paretoFront);
		}

	}
	
	static class SequentialDominance implements DominanceMethod {

		@Override
		public int[] getNonDominated(int[] evals) {
			int c = 0;
			int[] next = new int[evals.length];
			for (int i = 0; i< evals.length; i+=NH.NDIM) {
				boolean isDominated = false;
				for (int j=i; j<evals.length; j+=NH.NDIM) {
					if (evals[i] < evals[j] && evals[i+1] > evals[j+1]) {
						isDominated = true;
						break;
					}
				}
				if (!isDominated) {
					for (int k=0; k<NH.NDIM; k++) {
						next[c++] = evals[i+k];
					}
				}
			}
			return Arrays.copyOf(next, c);
		}
		
	}
}
