package aeminium.runtime.benchmarks.raytracer;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqRayTracer extends RayTracer {
	public static void main(String args[]) {
		Benchmark be = new Benchmark(args);
		int size = RayTracer.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}

		while (!be.stop()) {
			be.start();
			SeqRayTracer rt = new SeqRayTracer();
			rt.scene = rt.createScene();
			rt.width = size;
			rt.height = size;
			rt.setScene(rt.scene);
			Interval interval = new Interval(0, rt.width, rt.height, 0, rt.height, 1);
			rt.render(interval);
			be.end();
		}
	}
}
