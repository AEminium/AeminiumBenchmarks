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

public class FjLogCounter {
	
	public static void main(String[] args) throws Exception {
		File[] fs = SeqLogCounter.finder(args[0]);
		
		ForkJoinPool pool = new ForkJoinPool();
		int r = forkjoinCounter(fs, pool);
		System.out.println(r + " visits");
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
				d = SeqLogCounter.uncompressGZip(f);
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
			
			try {
				result = SeqLogCounter.countAccesses(d);
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
			SeqLogCounter.deleteFile(logfile);
		}
		return n;
	}

}
