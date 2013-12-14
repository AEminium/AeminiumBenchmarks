package aeminium.runtime.benchmarks.quicksort;

import aeminium.runtime.benchmarks.helpers.ArrayHelper;
import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqQuickSort {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = QuickSort.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		long[] original = ArrayHelper.generateRandomArray(size);
		be.start();
		SeqQuickSort.sort(original);
		be.end();
		if (be.verbose) {
			System.out.println("Sorted: " + ArrayHelper.checkArray(original));
		}
	}

	public static void sort(long[] values) {
		qsort_seq(values, 0, values.length-1);
	}
	
	public static void qsort_seq(long[] data, int left, int right) {
		int index = QuickSort.partition(data, left, right);
		if (left < index - 1)
			qsort_seq(data, left, index - 1);

		if (index < right)
			qsort_seq(data, index, right);
	}

}
