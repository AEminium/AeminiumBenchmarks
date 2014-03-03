package aeminium.runtime.benchmarks.raytracer;

import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeRayTracer extends RayTracer {

	public int threshold;
	public Runtime ae;

	public static void main(String args[]) {
		Benchmark be = new Benchmark(args);
		int size = RayTracer.DEFAULT_SIZE;
		int threshold = RayTracer.DEFAULT_THRESHOLD;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}
		Runtime ae = Factory.getRuntime();
		ae.addErrorHandler(new PrintErrorHandler());

		while (!be.stop()) {
			be.start();
			ae.init();
			AeRayTracer rt = new AeRayTracer();
			rt.threshold = threshold;
			rt.ae = ae;
			rt.scene = rt.createScene();
			rt.width = size;
			rt.height = size;
			rt.setScene(rt.scene);
			Interval interval = new Interval(0, rt.width, rt.height, 0, rt.height, 1);
			rt.render(interval);
			ae.shutdown();
			be.end();
		}
	}

	protected void iterateLines(final Interval interval, final Vec viewVec, final Vec upVec, final Vec leftVec, final Ray r, final Vec col) {
		Task tfor = ForTask.createFor(ae, new Range(interval.yfrom, interval.yto), new ForBody<Integer>() {

			@Override
			public void iterate(Integer y, Runtime rt, Task current) {
				renderLine(y, interval, r, col, viewVec, leftVec, upVec);
			}
		}, Runtime.NO_HINTS);
		ae.schedule(tfor, Runtime.NO_PARENT, Runtime.NO_DEPS);
	}
}
