package aeminium.runtime.benchmarks.logcounter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeLogCounter {
	public static void main(String[] args) throws Exception {
		Benchmark be = new Benchmark(args);
		File[] fs = LogCounter.finder(be.args[0]);
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		int r = aeminiumCounter(fs, rt);
		be.end();
		if (be.verbose) {
			System.out.println(r + " visits");
		}
	}
	
	public static int aeminiumCounter(File[] files, aeminium.runtime.Runtime rt) {
		final ArrayList<Task> counterTasks = new ArrayList<Task>();
		
		for (final File logfile : files) {
			final Task uncompress = rt.createBlockingTask(new Body() {
		
				@Override
				public void execute(Runtime rt, Task current) {
					try {
						current.setResult(LogCounter.uncompressGZip(logfile));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		
			}, (short)(Hints.LARGE | Hints.NO_CHILDREN));
			rt.schedule(uncompress, Runtime.NO_PARENT, Runtime.NO_DEPS);
			
			Task count = rt.createBlockingTask(new Body() {
				
				@Override
				public void execute(Runtime rt, Task current) {
					try {
						current.setResult(LogCounter.countAccesses((String) uncompress.getResult()));
						LogCounter.deleteFile(logfile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		
			}, (short)(Hints.LARGE | Hints.NO_CHILDREN));
			rt.schedule(count, Runtime.NO_PARENT, Arrays.asList(uncompress));
			
			counterTasks.add(count);
		}
		
		Task merge = rt.createBlockingTask(new Body() {
			
			@Override
			public void execute(Runtime rt, Task current) {
				int n = 0;
				for (Task t : counterTasks) {
					Integer r = (Integer) t.getResult();
					if (r != null) {
						n += r;
					}
				}
				current.setResult(n);
				
			}
	
		}, (short)(Hints.LOOPS | Hints.NO_CHILDREN | Hints.NO_DEPENDENTS));
		rt.schedule(merge, Runtime.NO_PARENT, counterTasks);
		
		rt.shutdown();
		return (Integer) merge.getResult();
	}
}