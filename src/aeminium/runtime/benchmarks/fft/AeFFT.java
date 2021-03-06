/**
 * Copyright (c) 2010-11 The AEminium Project (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package aeminium.runtime.benchmarks.fft;

/*
 * Parallel Version of the Cooley-Tukey FFT for series of size N, being N a power of two.
 */

import java.util.Random;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeFFT {

	public static class FFTBody implements Body {
		/* It reuses the result array for both input and result */
		public Complex[] result;
		private Complex[] odd;
		private Complex[] even;
		private int n;
		private int threshold;

		public FFTBody(Complex[] input, int t) {
			this.result = input;
			n = input.length;
			threshold = t;
			if (n != 1 && n % 2 != 0) {
				throw new RuntimeException("Size of array is not a power of 2.");
			}

			odd = new Complex[n / 2];
			even = new Complex[n / 2];
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if (n == 1) {
				return;
			}
			if (Benchmark.useThreshold ? n <= threshold : !rt.parallelize(current)) {
				result = SeqFFT.sequentialFFT(result);
				return;
			}

			for (int k = 0; k < n / 2; k++) {
				even[k] = result[2 * k];
				odd[k] = result[2 * k + 1];
			}

			FFTBody b1 = new FFTBody(even, threshold);
			Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
			rt.schedule(t1, current, Runtime.NO_DEPS);

			FFTBody b2 = new FFTBody(odd, threshold);
			Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
			rt.schedule(t2, current, Runtime.NO_DEPS);

			t1.getResult();
			t2.getResult();

			for (int k = 0; k < n / 2; k++) {
				double kth = -2 * k * Math.PI / n;
				Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
				result[k] = b1.result[k].plus(wk.times(b2.result[k]));
				result[k + n / 2] = b1.result[k].minus(wk.times(b2.result[k]));
			}
		}
	}

	public static FFTBody createFFTBody(final Runtime rt, final Complex[] input, int threshold) {
		Complex[] in = new Complex[input.length];
		System.arraycopy(input, 0, in, 0, input.length);
		return new FFTBody(in, threshold);
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = FFT.DEFAULT_SIZE;
		if (be.args.length > 0) size = Integer.parseInt(be.args[0]);
		int threshold = FFT.DEFAULT_THRESHOLD;
		if (be.args.length > 1) threshold = Integer.parseInt(be.args[1]);

		Complex[] input = FFT.createRandomComplexArray(size, new Random(1L));

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		while (!be.stop()) {
			be.start();

			rt.init();
			FFTBody body = createFFTBody(rt, input, threshold);
			Task t1 = rt.createNonBlockingTask(body, Hints.RECURSION);
			rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
			if (be.verbose) {
				System.out.println(body.result[0]);
				// FFT.show(body.result, "Result");
			}
		}
	}
}
