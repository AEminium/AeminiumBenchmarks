package aeminium.runtime.benchmarks.integrate;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.NonBlockingTask;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeIntegrate {

	public static double integral;
	public static double threshold;

	public static void main(String[] args) {
        Benchmark be = new Benchmark(args);
        
        if (be.args.length > 0) {
	    	double exp = Double.parseDouble(be.args[0]);
	    	Integrate.errorTolerance = Math.pow(10, -exp);
	    }
        
        be.start();
        Runtime rt = Factory.getRuntime();
        rt.addErrorHandler(new PrintErrorHandler());
		rt.init();

		Task main = startCall(rt, Integrate.start, Integrate.end, 0);
		rt.schedule(main, Runtime.NO_PARENT, Runtime.NO_DEPS);

		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Integral: " + integral);
		}
	
	}

	public static NonBlockingTask startCall(Runtime rt, final double start,
			final double end, final int a) {
		return rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				double fstart = Integrate.computeFunction(start);
				double fend = Integrate.computeFunction(end);
				IntegralBody intBody = new IntegralBody(start, end, fstart,
						fend, a);
				Task intTask = rt.createNonBlockingTask(intBody,
						Runtime.NO_HINTS);
				rt.schedule(intTask, current, Runtime.NO_DEPS);
				intTask.getResult();
				integral = intBody.ret;
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
			if (Math.abs(alr - area) <= Integrate.errorTolerance) {
				ret = alr;
				return;
			}
			if (Benchmark.useThreshold ? Math.abs(alr - area) <= Integrate.threshold : rt.parallelize(current)) {
				try {
					ret = SeqIntegrate.recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r,
							area);
				} catch (StackOverflowError e) {
					e.printStackTrace();
				}
				return;
			}

			IntegralBody leftBody = new IntegralBody(l, c, fl, fc, al);
			Task leftSide = rt
					.createNonBlockingTask(leftBody, (short)(Hints.RECURSION));
			rt.schedule(leftSide, current, Runtime.NO_DEPS);
			IntegralBody rightBody = new IntegralBody(c, r, fc, fl, ar);
			Task rightSide = rt.createNonBlockingTask(rightBody,
					(short)(Hints.RECURSION));
			rt.schedule(rightSide, current, Runtime.NO_DEPS);

			leftSide.getResult();
			rightSide.getResult();
			ret = rightBody.ret + leftBody.ret;
		}
	}
}
