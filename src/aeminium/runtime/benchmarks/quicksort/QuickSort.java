package aeminium.runtime.benchmarks.quicksort;

public class QuickSort {
	public final static int DEFAULT_SIZE = 10000000;
	public final static int DEFAULT_THRESHOLD = 10000;

	public static int partition(long[] data, int left, int right) {
		int i = left;
		int j = right;
		long tmp;
		long pivot = data[(left + right) / 2];

		while (i <= j) {
			while (data[i] < pivot)
				i++;
			while (data[j] > pivot)
				j--;
			if (i <= j) {
				tmp = data[i];
				data[i] = data[j];
				data[j] = tmp;
				i++;
				j--;
			}
		}

		return i;
	}
}
