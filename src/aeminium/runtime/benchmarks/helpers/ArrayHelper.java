package aeminium.runtime.benchmarks.helpers;

import java.util.Random;

public class ArrayHelper {
	public static boolean checkArray(long[] c) {
		boolean st = true;
		for (int i = 0; i < c.length - 1; i++) {
			st = st && (c[i] <= c[i + 1]);
		}
		return st;
	}

	public static long[] generateRandomArray(int size) {
		Random r = new Random();
		r.setSeed(1234567890);
		long[] ar = new long[size];
		for (int i = 0; i < size; i++) {
			ar[i] = r.nextLong();
		}
		return ar;
	}
}
