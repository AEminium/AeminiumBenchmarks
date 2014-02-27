package aeminium.runtime.benchmarks.doall;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjDoAll {

	static class MakeAB extends RecursiveAction {
		private static final long serialVersionUID = -2276522791388601840L;

		double[] target;
		int st;
		int end;
		int threshold;
		boolean isA; // true for A, false for B

		public MakeAB(double[] target, int st, int end, boolean a, int threshold) {
			this.target = target;
			this.st = st;
			this.end = end;
			this.threshold = threshold;
			this.isA = a;
		}

		@Override
		protected void compute() {
			if (st == end) {
				target[st] = (this.isA) ? Math.sqrt(st) : Math.sin(st);
			} else if (end - st < threshold) {
				for (int i = st; i < end; i++) {
					target[i] = (this.isA) ? Math.sqrt(i) : Math.sin(i);
				}
			} else {
				int h = (end - st) / 2 + st;
				MakeAB a = new MakeAB(target, st, h, isA, threshold);
				MakeAB b = new MakeAB(target, h, end, isA, threshold);
				invokeAll(a, b);
			}

		}

	}

	static class MakeC extends RecursiveAction {
		private static final long serialVersionUID = -2276522791388601840L;

		double[] target;
		double[] src1;
		double[] src2;
		int st;
		int end;
		int threshold;

		public MakeC(double[] target, double[] src1, double[] src2, int st, int end, int threshold) {
			this.target = target;
			this.src1 = src1;
			this.src2 = src2;
			this.st = st;
			this.end = end;
			this.threshold = threshold;
		}

		@Override
		protected void compute() {
			if (st == end) {
				target[st] = src1[st] / src2[st];
			} else if (end - st < threshold) {
				for (int i = st; i < end; i++) {
					target[i] = src1[i] / src2[i];
				}
			} else {
				int h = (end - st) / 2 + st;
				MakeC a = new MakeC(target, src1, src2, st, h, threshold);
				MakeC b = new MakeC(target, src1, src2, h, end, threshold);
				invokeAll(a, b);
			}

		}

	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = SeqDoAll.DEFAULT_SIZE;
		if (be.args.length > 0) size = Integer.parseInt(be.args[0]);
		int threshold = SeqDoAll.DEFAULT_THRESHOLD;
		if (be.args.length > 1) size = Integer.parseInt(be.args[1]);

		double[] a = new double[size];
		double[] b = new double[size];
		double[] c = new double[size];

		ForkJoinPool pool = new ForkJoinPool();

		while (!be.stop()) {
			be.start();

			pool.invoke(new MakeAB(a, 0, size, true, threshold));
			pool.invoke(new MakeAB(b, 0, size, false, threshold));
			pool.invoke(new MakeC(c, a, b, 0, size, threshold));

			be.end();
		}
	}
}
