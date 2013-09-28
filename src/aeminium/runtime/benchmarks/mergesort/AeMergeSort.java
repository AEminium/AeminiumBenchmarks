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

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeMergeSort {

	long[] array;
	int threshold;
	
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
			if (Benchmark.useThreshold ? array.length < threshold : !rt.parallelize(current)) {
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
		Task sorterT = rt.createNonBlockingTask(sorter, (short)(Hints.RECURSION));
		rt.schedule(sorterT, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		Task saverT = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				array = sorter.array;
			}
		},(short)(Hints.SMALL | Hints.NO_CHILDREN));
		rt.schedule(saverT, Runtime.NO_PARENT, Arrays.asList(sorterT));
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

		long[] original =  MergeSort.generateRandomArray(size);
		be.start();
		AeMergeSort merger = new AeMergeSort(original, threshold);
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		merger.doSort(rt);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Sorted: " +  MergeSort.checkArray(merger.array));
		}
	}
	
	
	
}
