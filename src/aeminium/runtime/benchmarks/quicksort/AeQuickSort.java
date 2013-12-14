package aeminium.runtime.benchmarks.quicksort;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.ArrayHelper;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeQuickSort {
	long[] array;
	int threshold;
	
	public AeQuickSort(long[] original, int threshold) {
		this.array = original;
		this.threshold = threshold;
	}
	
	public class QuickSortBody implements Body {
		public long[] data;
		int left;
		int right;

		public QuickSortBody(long[] original, int left, int right) {
			this.data = original;
			this.left = left;
			this.right = right;
		}
		
		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			if (data.length <= 1)
				return;
			if (Benchmark.useThreshold ? data.length < threshold : !rt.parallelize(current)) {
				SeqQuickSort.sort(data);
				return;
			}
			
			final int index = QuickSort.partition(this.data, this.left, this.right);
			
			Task leftT = null;
			Task rightT = null;
			if (this.left < index - 1) {
				QuickSortBody lb = new QuickSortBody(data, left, index-1);
				leftT = rt.createNonBlockingTask(lb, Hints.RECURSION);
				rt.schedule(leftT, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}
			if (index < this.right) {
				QuickSortBody lr = new QuickSortBody(data, index, right);
				rightT = rt.createNonBlockingTask(lr, Hints.RECURSION);
				rt.schedule(rightT, Runtime.NO_PARENT, Runtime.NO_DEPS);
			}
			
			if (leftT != null) leftT.getResult();
			if (rightT != null) rightT.getResult();
		}
		
	}
	
	public void doSort(Runtime rt) {
		final QuickSortBody sorter = new QuickSortBody(array, 0, array.length-1);
		Task sorterT = rt.createNonBlockingTask(sorter, (short)(Hints.RECURSION));
		rt.schedule(sorterT, Runtime.NO_PARENT, Runtime.NO_DEPS);
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = QuickSort.DEFAULT_SIZE;
		int threshold = QuickSort.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}

		long[] original =  ArrayHelper.generateRandomArray(size);
		be.start();
		AeQuickSort merger = new AeQuickSort(original, threshold);
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		merger.doSort(rt);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Sorted: " +  ArrayHelper.checkArray(merger.array));
		}
	}
}
