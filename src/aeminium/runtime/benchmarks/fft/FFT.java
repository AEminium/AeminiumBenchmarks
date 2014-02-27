package aeminium.runtime.benchmarks.fft;

import java.util.Random;

public class FFT {

	public static int DEFAULT_SIZE = 524288;
	public static int DEFAULT_THRESHOLD = 1024;

	public static Complex[] createRandomComplexArray(int n) {
		Complex[] x = new Complex[n];
		for (int i = 0; i < n; i++) {
			x[i] = new Complex(i, 0);
			x[i] = new Complex(-2 * Math.random() + 1, 0);
		}
		return x;
	}

	public static Complex[] createRandomComplexArray(int n, Random r) {
		Complex[] x = new Complex[n];
		for (int i = 0; i < n; i++) {
			x[i] = new Complex(i, 0);
			x[i] = new Complex(-2 * r.nextDouble() + 1, 0);
		}
		return x;
	}

	public static void show(Complex[] x, String title) {
		System.out.println(title);
		System.out.println("-------------------");
		for (int i = 0; i < x.length; i++) {
			System.out.println(x[i]);
		}
		System.out.println();
	}
}
