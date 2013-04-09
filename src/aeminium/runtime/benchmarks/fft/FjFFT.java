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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;


public class FjFFT extends RecursiveAction { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public volatile Complex[] result;
	private Complex[] even;
	private Complex[] odd;
	private int threshold;
	private int n;
	
	public FjFFT(Complex[] input, int thre) {
		result = input;
		n = input.length;
		threshold = thre;
		if (n != 1 && n % 2 != 0) { throw new RuntimeException("Size of array is not a power of 2."); }
		
		odd = new Complex[n/2];
		even = new Complex[n/2];
	}

	protected void compute() {
		if (n == 1) return;
		if (n <= threshold) {
			result = AeFFT.sequentialFFT(result);
			return;
		}
		
		for (int k=0; k < n/2; k++) {
			even[k] = result[2*k];
			odd[k] = result[2*k+1];
		}
		
		FjFFT f1 = new FjFFT(even, threshold);	
		FjFFT f2 = new FjFFT(odd, threshold);
		invokeAll(f1,f2);
		
		for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            result[k]       = f1.result[k].plus(wk.times(f2.result[k]));
            result[k + n/2] = f1.result[k].minus(wk.times(f2.result[k]));
		}
		
	}
	
	public static void main(String[] args) {
		Complex[] input = AeFFT.createRandomComplexArray(524288);
		
		ForkJoinPool pool = new ForkJoinPool();
		FjFFT t = new FjFFT(input, 1024);
		pool.invoke(t);
		if (args.length == 0) {
			AeFFT.show(t.result, "Result");
		}
	}
	
}
