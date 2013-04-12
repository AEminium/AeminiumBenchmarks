package aeminium.runtime.benchmarks.doall;

import java.util.Arrays;

import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;

public class AeDoAll {
	
	static double[] a;
	static double[] b;
	static double[] c;
	
	public static void main(String[] args) {
		int size = 1000000;
		if (args.length > 0)
			size = Integer.parseInt(args[0]);

		Benchmark be = new Benchmark(args);
		be.start();
		Runtime rt = Factory.getRuntime();
		
		a = new double[size];
		b = new double[size];
		c = new double[size];
		rt.init();
		
		Task as = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				a[i] = Math.sqrt(i);
			}
		});
		rt.schedule(as, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		Task bs = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				b[i] = Math.sin(i);
			}
		});
		rt.schedule(bs, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		
		Task cs = ForTask.createFor(rt, new Range(size), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				c[i] = a[i] / b[i];
			}
		});
		rt.schedule(cs, Runtime.NO_PARENT, Arrays.asList(as, bs));
		rt.shutdown();
		be.end();
	}
}
