package aeminium.runtime.benchmarks.logcounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjLogCounter {
	
	public static void main(String[] args) throws Exception {
		Benchmark be = new Benchmark(args);
		File[] fs = LogCounter.finder(be.args[0]);
		be.start();
		ForkJoinPool pool = new ForkJoinPool();
		int r = forkjoinCounter(fs, pool);
		be.end();
		if (be.verbose) {
			System.out.println(r + " visits");
		}
	}
	
	static class FJCounter implements Callable<Integer> {

		File f;

		public FJCounter(File f) {
			this.f = f;
		}
		
		@Override
		public Integer call() {
			int result = 0;
			String d;
			try {
				d = LogCounter.uncompressGZip(f);
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
			
			try {
				result = LogCounter.countAccesses(d);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public static int forkjoinCounter(File[] files, ForkJoinPool pool) {
		int n = 0;
		Collection<FJCounter> futures = new ArrayList<FJCounter>();
		
		for (File logfile : files) {
			futures.add(new FJCounter(logfile));
		}
		List<Future<Integer>> results = pool.invokeAll(futures);
		
		for (Future<Integer> result : results) {
			try {
				if (result.get() == null) {
					System.out.println("null!");
				}
				n += result.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (ExecutionException e) {
				e.printStackTrace();
				System.exit(1);	
			}
		}
		for (File logfile : files) {
			LogCounter.deleteFile(logfile);
		}
		return n;
	}

}
