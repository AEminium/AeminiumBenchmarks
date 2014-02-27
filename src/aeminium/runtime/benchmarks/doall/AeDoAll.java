package aeminium.runtime.benchmarks.doall;

import java.util.Arrays;

import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeDoAll {
	
	static double[] a;
	static double[] b;
	static double[] c;
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = SeqDoAll.DEFAULT_SIZE;
		if (be.args.length > 0)
			size = Integer.parseInt(be.args[0]);

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		a = new double[size];
		b = new double[size];
		c = new double[size];
		
		while (!be.stop()) {
			be.start();
			
			rt.init();
			
			Task as = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					a[i] = Math.sqrt(i);
				}
			}, Hints.SMALL);
			rt.schedule(as, Runtime.NO_PARENT, Runtime.NO_DEPS);
			
			Task bs = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					b[i] = Math.sin(i);
				}
			}, Hints.SMALL);
			rt.schedule(bs, Runtime.NO_PARENT, Runtime.NO_DEPS);
			
			
			Task cs = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					c[i] = a[i] / b[i];
				}
			}, Hints.SMALL);
			rt.schedule(cs, Runtime.NO_PARENT, Arrays.asList(as, bs));
			rt.shutdown();
			be.end();
		}
	}
}
