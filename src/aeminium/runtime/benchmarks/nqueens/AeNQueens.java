package aeminium.runtime.benchmarks.nqueens;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;

public class AeNQueens {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		
		int minSize = NQueens.DEFAULT_MIN_SIZE;
	    if (be.args.length > 0) {
	    	minSize = Integer.parseInt(be.args[0]);
	    }
	    
	    int maxSize = NQueens.DEFAULT_MAX_SIZE;
	    if (be.args.length > 1) {
	    	maxSize = Integer.parseInt(be.args[1]);
	    }
		
	    AtomicInteger[] solutions = new AtomicInteger[maxSize - minSize + 1];
	    
	    Runtime rt = Factory.getRuntime();
	    
		be.start();
		
		rt.init();
		
		int c = 0;
		Task prev = Runtime.NO_PARENT;
		for (int size = minSize; size <= maxSize; size++) {
			final AtomicInteger sols = new AtomicInteger(0);
			solutions[c++] = sols;
			final int bs = size;
			Task t = rt.createBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					solve(rt, bs, sols);
				}
			}, (short) (Hints.RECURSION | Hints.LARGE | Hints.LOOPS));
			rt.schedule(t, prev, Runtime.NO_DEPS);
			prev = t;
		}
		rt.shutdown();
		be.end();
	    if (be.verbose) {
	    	for (int i=0; i< (maxSize - minSize + 1); i++) {
	    		int given = solutions[i].get();
	    		int expected = NQueens.expectedSolutions[minSize + i]; 
	    		if ( given != expected ) {
	    			System.out.println( "Failed:" + (minSize + i) + ", given: " + given + " when expected " + expected);
	    		}
	    	}
	    }
	}

	public static void solve(Runtime rt, int size, AtomicInteger sol) {
		solve(rt, null, sol, size, new int[0]);
	}
	
	public static void solve(final Runtime rt, final Task parent, final AtomicInteger sol, final int bs, final int[] array) {
		if (array.length >= bs) {
			sol.getAndIncrement();
		} else {
			
			if (parent == null || rt.parallelize(parent)) {
				Task t = ForTask.createFor(rt, new Range(bs), new ForBody<Integer>() {
	
					@Override
					public void iterate(Integer q, Runtime rt, Task current) {
						int row = array.length;
			            for (int i = 0; i < row; i++) {
			                int p = array[i];
			                if (q == p || q == p - (row - i) || q == p + (row - i)) {
			                	return;
			                }
			            }
			            int[] next = Arrays.copyOf(array, row+1);
			            next[row] = q;
			            solve(rt, current, sol, bs, next);
					}
				}, Hints.RECURSION);
				rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
			} else {
				int row = array.length;
		        outer:
		        for (int q = 0; q < bs; ++q) {
		            for (int i = 0; i < row; i++) {
		                int p = array[i];
		                if (q == p || q == p - (row - i) || q == p + (row - i))
		                    continue outer; // attacked
		            }
		            
		            int[] next = Arrays.copyOf(array, row+1);
		            next[row] = q;
		            solve(rt, parent, sol, bs, next);
		        }
			}
		}
	}
}