package aeminium.runtime.benchmarks.mergesort;

import aeminium.runtime.benchmarks.helpers.ArrayHelper;
import aeminium.runtime.benchmarks.helpers.Benchmark;


public class SeqMergeSort {
	private long[] numbers;
	private long[] helper;

	private int number;

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = MergeSort.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		long[] original = ArrayHelper.generateRandomArray(size);
		be.start();
		SeqMergeSort sorter = new SeqMergeSort();
		sorter.sort(original);
		be.end();
		if (be.verbose) {
			System.out.println("Sorted: " + ArrayHelper.checkArray(original));
		}
	}

	public void sort(long[] values) {
		this.numbers = values;
		number = values.length;
		this.helper = new long[number];
		mergesort(0, number - 1);
	}

	private void mergesort(int low, int high) {
		// Check if low is smaller then high, if not then the array is sorted
		if (low < high) {
			// Get the index of the element which is in the middle
			int middle = low + (high - low) / 2;
			// Sort the left side of the array
			mergesort(low, middle);
			// Sort the right side of the array
			mergesort(middle + 1, high);
			// Combine them both
			merge(low, middle, high);
		}
	}

	private void merge(int low, int middle, int high) {

		// Copy both parts into the helper array
		for (int i = low; i <= high; i++) {
			helper[i] = numbers[i];
		}

		int i = low;
		int j = middle + 1;
		int k = low;
		// Copy the smallest values from either the left or the right side back
		// to the original array
		while (i <= middle && j <= high) {
			if (helper[i] <= helper[j]) {
				numbers[k] = helper[i];
				i++;
			} else {
				numbers[k] = helper[j];
				j++;
			}
			k++;
		}
		// Copy the rest of the left side of the array into the target array
		while (i <= middle) {
			numbers[k] = helper[i];
			k++;
			i++;
		}

	}
}
