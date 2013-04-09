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

package aeminium.runtime.benchmarks.mergesort;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class AeMergeSort {

	long[] array;
	final int threshold;
	
	public AeMergeSort(long[] original) {
		this(original, 10);
	}
	
	public AeMergeSort(long[] original, int threshold) {
		this.array = original;
		this.threshold = threshold;
	}
	
	public class MergeSortBody implements Body {
		public long[] array;
		
		public MergeSortBody(long[] original) {
			this.array = original;
		}
		
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			if (array.length <= 1)
				return;
			if (array.length <= threshold) {
				Arrays.sort(array);
				return;
			}
			List<long[]> partitionedArray = partitionArray();
			final MergeSortBody left = new MergeSortBody(partitionedArray.get(0));
			Task leftT = rt.createNonBlockingTask(left, Runtime.NO_HINTS);
			rt.schedule(leftT, Runtime.NO_PARENT, Runtime.NO_DEPS);
			
			final MergeSortBody right = new MergeSortBody(partitionedArray.get(1));
			Task rightT = rt.createNonBlockingTask(right, Runtime.NO_HINTS);
			rt.schedule(rightT, Runtime.NO_PARENT, Runtime.NO_DEPS);
			
			leftT.getResult();
			rightT.getResult();
			
			long[] mergedArray = new long[right.array.length + left.array.length];
			mergeArrays(left.array, right.array, mergedArray);
			array = mergedArray;
		}
		
		private List<long[]> partitionArray() {
			int mid = array.length / 2;
			long[] partition1 = Arrays.copyOfRange(array, 0, mid);
			long[] partition2 = Arrays.copyOfRange(array, mid,array.length);
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

	}
	
	public void doSort(Runtime rt) {
		final MergeSortBody sorter = new MergeSortBody(array);
		Task sorterT = rt.createNonBlockingTask(sorter, Runtime.NO_HINTS);
		rt.schedule(sorterT, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		Task saverT = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				array = sorter.array;
			}
		},Runtime.NO_HINTS);
		rt.schedule(saverT, Runtime.NO_PARENT, Arrays.asList(sorterT));
	}
	
	
	/* Auxiliary Stuff for standalone running */
	
	public static void main(String ...args) {
		
		int size = 10000000;
		int threshold = 100;
		if (args.length >= 1) {
			size = Integer.parseInt(args[0]);
		}
		if (args.length >= 2) {
			threshold = Integer.parseInt(args[1]);
		}
		
		long[] original = generateRandomArray(size);
		AeMergeSort merger = new AeMergeSort(original, threshold);
		
		Runtime rt = Factory.getRuntime();
		rt.init();
		merger.doSort(rt);
		rt.shutdown();
		if (args.length >= 3) {
			System.out.println("Sorted: " + checkArray(merger.array));
		}
	}
	
	public static boolean checkArray(long[] c) {
		boolean st = true;
		for (int i=0; i<c.length-1; i++) {
			st = st && (c[i] <= c[i+1]);
		}
		return st;
	}
	
	public static long[] generateRandomArray(int size) {
		Random r = new Random();
		r.setSeed(1234567890);
		long[] ar = new long[size];
		for (int i=0; i<size; i++) {
			ar[i] = r.nextLong();
		}
		return ar;
	}
	
}
