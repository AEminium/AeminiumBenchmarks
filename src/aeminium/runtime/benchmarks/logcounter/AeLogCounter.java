package aeminium.runtime.benchmarks.logcounter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class AeLogCounter {
	public static void main(String[] args) throws Exception {
		Runtime rt = Factory.getRuntime();
		File[] fs = LogCounter.finder(args[0]);
		int r = aeminiumCounter(fs, rt);
		System.out.println(r + " visits");
	}
	
	public static int aeminiumCounter(File[] files, aeminium.runtime.Runtime rt) {
		rt.init();
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
		
			}, aeminium.runtime.Runtime.NO_HINTS);
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
		
			}, aeminium.runtime.Runtime.NO_HINTS);
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
	
		}, aeminium.runtime.Runtime.NO_HINTS);
		rt.schedule(merge, Runtime.NO_PARENT, counterTasks);
		
		rt.shutdown();
		return (Integer) merge.getResult();
	}
}