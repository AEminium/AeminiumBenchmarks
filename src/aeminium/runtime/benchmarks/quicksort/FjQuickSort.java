package aeminium.runtime.benchmarks.quicksort;

import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.ArrayHelper;
import aeminium.runtime.benchmarks.helpers.Benchmark;

@SuppressWarnings("serial")
public class FjQuickSort extends RecursiveAction {

	long[] data;
	int threshold;
	int left;
	int right;

	public FjQuickSort(long[] arrayToDivide, int left, int right, int threshold) {
		this.data = arrayToDivide;
		this.threshold = threshold;
		this.left = left;
		this.right = right;
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = QuickSort.DEFAULT_SIZE;
		int threshold = QuickSort.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}
		long[] original = ArrayHelper.generateRandomArray(size);
		ForkJoinPool pool = new ForkJoinPool();
		while (!be.stop()) {
			be.start();
			FjQuickSort t = new FjQuickSort(original, 0, size - 1, threshold);
			pool.invoke(t);
			be.end();
			if (be.verbose) {
				System.out.println("Sorted: " + ArrayHelper.checkArray(t.data));
			}
		}
	}

	@Override
	protected void compute() {
		if (Benchmark.useThreshold ? data.length < threshold : !this.shouldFork()) {
			SeqQuickSort.sort(data);
			return;
		}

		final int index = QuickSort.partition(this.data, this.left, this.right);
		FjQuickSort s1 = null, s2 = null;
		if (this.left < index - 1) {
			s1 = new FjQuickSort(this.data, this.left, index - 1, threshold);
			s1.fork();
		}

		if (index < this.right) {
			s2 = new FjQuickSort(this.data, index, this.right, threshold);
			s2.compute();
		}

		if (this.left < index - 1) {
			s1.join();
		}
	}
}
