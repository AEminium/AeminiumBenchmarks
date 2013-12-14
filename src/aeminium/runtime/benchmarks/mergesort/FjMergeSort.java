package aeminium.runtime.benchmarks.mergesort;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import aeminium.runtime.benchmarks.helpers.ArrayHelper;
import aeminium.runtime.benchmarks.helpers.Benchmark;

@SuppressWarnings("serial")
public class FjMergeSort extends RecursiveTask<long[]> {

	long[] arrayToDivide;
	int threshold;

	public FjMergeSort(long[] arrayToDivide, int threshold) {
		this.arrayToDivide = arrayToDivide;
		this.threshold = threshold;
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = MergeSort.DEFAULT_SIZE;
		int threshold = MergeSort.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}
		long[] original = ArrayHelper.generateRandomArray(size);
		be.start();
		ForkJoinPool pool = new ForkJoinPool();
		FjMergeSort t = new FjMergeSort(original, threshold);
		pool.invoke(t);
		be.end();
		if (be.verbose) {
			System.out.println("Sorted: " + ArrayHelper.checkArray(t.join()));
		}
	}

	@Override
	protected long[] compute() {
		/*
		 * We divide the array till it has only 1 element. We can also custom
		 * define this value to say some 5 elements. In which case the return
		 * would be Arrays.sort(arrayToDivide) instead.
		 */

		if (arrayToDivide.length < threshold) {
			sequentialSort();
			return arrayToDivide;
		}

		if (arrayToDivide.length > 1) {

			List<long[]> partitionedArray = partitionArray();

			FjMergeSort task1 = new FjMergeSort(partitionedArray.get(0),
					threshold);
			FjMergeSort task2 = new FjMergeSort(partitionedArray.get(1),
					threshold);
			invokeAll(task1, task2);

			// Wait for results from both the tasks
			long[] array1 = task1.join();
			long[] array2 = task2.join();

			// Initialize a merged array
			long[] mergedArray = new long[array1.length + array2.length];

			mergeArrays(task1.join(), task2.join(), mergedArray);
			return mergedArray;
		}
		return arrayToDivide;
	}

	private List<long[]> partitionArray() {

		int mid = arrayToDivide.length / 2;
		long[] partition1 = Arrays.copyOfRange(arrayToDivide, 0, mid);

		long[] partition2 = Arrays.copyOfRange(arrayToDivide, mid,
				arrayToDivide.length);
		return Arrays.asList(partition1, partition2);

	}

	private void mergeArrays(long[] array1, long[] array2, long[] mergedArray) {
		int i = 0, j = 0, k = 0;
		while ((i < array1.length) && (j < array2.length)) {
			if (array1[i] < array2[j]) {
				mergedArray[k] = array1[i++];
			} else {
				mergedArray[k] = array2[j++];
			}
			k++;
		}

		if (i == array1.length) {
			for (int a = j; a < array2.length; a++) {
				mergedArray[k++] = array2[a];
			}
		} else {
			for (int a = i; a < array1.length; a++) {
				mergedArray[k++] = array1[a];
			}
		}
	}

	public void sequentialSort() {
		Arrays.sort(arrayToDivide);
	}
}