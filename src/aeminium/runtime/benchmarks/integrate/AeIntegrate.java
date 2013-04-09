package aeminium.runtime.benchmarks.integrate;

import aeminium.runtime.Body;
import aeminium.runtime.NonBlockingTask;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class AeIntegrate {

	static final double errorTolerance = 1.0e-12;
	static final int threshold = 100;
	static double start = -2101.0;
	static double end = 1036.0;
	static final long NPS = (1000L * 1000 * 1000);

	static double computeFunction(double x) {
		return (x * x + 1.0) * x;
	}

	static final double recEval(double l, double r, double fl, double fr,
			double a) {
		double h = (r - l) * 0.5;
		double c = l + h;
		double fc = (c * c + 1.0) * c;
		double hh = h * 0.5;
		double al = (fl + fc) * hh;
		double ar = (fr + fc) * hh;
		double alr = al + ar;
		if (Math.abs(alr - a) <= errorTolerance)
			return alr;
		else
			return recEval(c, r, fc, fr, ar) + recEval(l, c, fl, fc, al);
	}

	public static void main(String[] args) {
		long tstart = System.nanoTime();
		Runtime rt = Factory.getRuntime();
		rt.init();

		Task main = startCall(rt, start, end, 0);
		rt.schedule(main, Runtime.NO_PARENT, Runtime.NO_DEPS);

		rt.shutdown();
		long tend = System.nanoTime();
		System.out.println((double) (tend - tstart) / NPS);
	}

	public static NonBlockingTask startCall(Runtime rt, final double start,
			final double end, final int a) {
		return rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				double fstart = computeFunction(start);
				double fend = computeFunction(end);
				IntegralBody intBody = new IntegralBody(start, end, fstart,
						fend, a);
				Task intTask = rt.createNonBlockingTask(intBody,
						Runtime.NO_HINTS);
				rt.schedule(intTask, current, Runtime.NO_DEPS);
				intTask.getResult();
				double integral = intBody.ret;
				System.out.println("Integral: " + integral);
			}

		}, Runtime.NO_HINTS);
	}

	public static class IntegralBody implements Body {

		final double l; // lower bound
		final double r; // upper bound
		final double fr;
		final double fl;
		double area;
		public volatile double ret;

		public IntegralBody(double l, double r, double fl, double fr, double a) {
			this.l = l;
			this.r = r;
			this.fl = fl;
			this.fr = fr;
			this.area = a;
		}

		@Override
		public void execute(Runtime rt, Task current) throws Exception {
			double h = (r - l) * 0.5;
			double c = l + h;
			double fc = (c * c + 1.0) * c;
			double hh = h * 0.5;
			double al = (fl + fc) * hh;
			double ar = (fr + fc) * hh;
			double alr = al + ar;
			if (Math.abs(alr - area) <= errorTolerance) {
				ret = alr;
				return;
			}
			if (Math.abs(alr - area) <= threshold || !rt.parallelize()) {
				try {
					ret = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r,
							area);
				} catch (StackOverflowError e) {
					e.printStackTrace();
				}
				return;
			}

			IntegralBody leftBody = new IntegralBody(l, c, fl, fc, al);
			Task leftSide = rt
					.createNonBlockingTask(leftBody, Runtime.NO_HINTS);
			rt.schedule(leftSide, current, Runtime.NO_DEPS);
			IntegralBody rightBody = new IntegralBody(c, r, fc, fl, ar);
			Task rightSide = rt.createNonBlockingTask(rightBody,
					Runtime.NO_HINTS);
			rt.schedule(rightSide, current, Runtime.NO_DEPS);

			leftSide.getResult();
			rightSide.getResult();
			ret = rightBody.ret + leftBody.ret;
		}
	}
}
