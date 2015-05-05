package aeminium.runtime.benchmarks.raytracer;

import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjRayTracer extends RayTracer {

	public int threshold;
	public ForkJoinPool pool;

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
		ForkJoinPool pool = new ForkJoinPool();

		while (!be.stop()) {
			be.start();
			FjRayTracer rt = new FjRayTracer();
			rt.threshold = threshold;
			rt.pool = pool;
			rt.scene = rt.createScene();
			rt.width = size;
			rt.height = size;
			rt.setScene(rt.scene);
			Interval interval = new Interval(0, rt.width, rt.height, 0, rt.height, 1);
			rt.render(interval);
			be.end();
		}
	}

	protected void iterateLines(Interval interval, Vec viewVec, Vec upVec, Vec leftVec, Ray r, Vec col) {
		Splitter t1 = new Splitter(interval.yfrom, interval.yto, interval, r, col, viewVec, leftVec, upVec, threshold);
		pool.invoke(t1);
	}

	@SuppressWarnings("serial")
	class Splitter extends RecursiveAction {
		int fro, to, threshold;
		Ray r;
		Interval interval;
		Vec col, viewVec, leftVec, upVec;

		public Splitter(int fro, int to, Interval interval, Ray r, Vec col, Vec viewVec, Vec leftVec, Vec upVec, int threshold) {
			this.fro = fro;
			this.to = to;
			this.r = r;
			this.interval = interval;
			this.col = col;
			this.viewVec = viewVec;
			this.leftVec = leftVec;
			this.upVec = upVec;
			this.threshold = threshold;
		}

		@Override
		protected void compute() {
			if (Benchmark.useThreshold ? to - fro < threshold : !this.shouldFork()) {
				for (int y = fro; y < to; y++) {
					renderLine(y, interval, r, col, viewVec, leftVec, upVec);
				} // end for (y)
			} else {
				int mid = (to - fro) / 2 + fro;
				Splitter t1 = new Splitter(fro, mid, interval, r, col, viewVec, leftVec, upVec, threshold);
				Splitter t2 = new Splitter(mid, to, interval, r, col, viewVec, leftVec, upVec, threshold);
				invokeAll(t1, t2);
			}

		}

	}
}
