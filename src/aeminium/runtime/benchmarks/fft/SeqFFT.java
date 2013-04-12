package aeminium.runtime.benchmarks.fft;

import java.util.Random;

import aeminium.runtime.benchmarks.helpers.Benchmark;


public class SeqFFT {
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = FFT.DEFAULT_SIZE;
		if (be.args.length > 0) size = Integer.parseInt(be.args[0]);
		
		Complex[] input = FFT.createRandomComplexArray(size, new Random(1L));
		
		be.start();
		Complex[] result = sequentialFFT(input);
		be.end();
		
		if (be.verbose) {
			FFT.show(result, "Result");
		}
	}
	
	
	/* Linear Version */
    public static Complex[] sequentialFFT(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) return new Complex[] { x[0] };

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) { throw new RuntimeException("N is not a power of 2"); }

        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = sequentialFFT(even);

        // fft of odd terms
        Complex[] odd  = even;  // reuse the array
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = sequentialFFT(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
}
