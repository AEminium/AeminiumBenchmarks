package aeminium.runtime.benchmarks.blackscholes;

import java.util.ArrayList;

import aeminium.runtime.Body;
import aeminium.runtime.DataGroup;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.LongRange;
import aeminium.runtime.implementations.Factory;

/*************************************************************************
 * Compilation: javac BlackScholes.java MyMath.java Execution: java BlackScholes
 * S X r sigma T
 * 
 * Reads in five command line inputs and calculates the option price according
 * to the Black-Scholes formula.
 * 
 * % java BlackScholes 23.75 15.00 0.01 0.35 0.5 8.879159279691955 (actual =
 * 9.10)
 * 
 * % java BlackScholes 30.14 15.0 0.01 0.332 0.25 15.177462481562186 (actual =
 * 14.50)
 * 
 * 
 * Information calculated based on closing data on Monday, June 9th 2003.
 * 
 * Microsoft: share price: 23.75 strike price: 15.00 risk-free interest rate: 1%
 * volatility: 35% (historical estimate) time until expiration: 0.5 years
 * 
 * GE: share price: 30.14 strike price: 15.00 risk-free interest rate 1%
 * volatility: 33.2% (historical estimate) time until expiration 0.25 years
 * 
 * 
 * Reference: http://www.hoadley.net/options/develtoolsvolcalc.htm
 * 
 *************************************************************************/

public class AeBlackScholes {
	public static Runtime rt;

	private static double saveCallPrice = 0.0;
	private static double saveCall = 0.0;
	private static double saveCall2 = 0.0;
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
    	be.start();
    	
		rt = Factory.getRuntime();
		rt.init();

		final double S = Double.parseDouble(be.args[0]);
		final double X = Double.parseDouble(be.args[1]);
		final double r = Double.parseDouble(be.args[2]);
		final double sigma = Double.parseDouble(be.args[3]);
		final double T = Double.parseDouble(be.args[4]);
		final long N   = Long.parseLong(be.args[5]);
		
		final DataGroup pCall = rt.createDataGroup();
		
		
		Task callPrice = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				double d1 = (Math.log(S/X) + (r + sigma * sigma/2) * T) / (sigma * Math.sqrt(T));
				double d2 = d1 - sigma * Math.sqrt(T);
				saveCallPrice = S * Gaussian.Phi(d1) - X * Math.exp(-r * T) * Gaussian.Phi(d2);
			}
			
		}, Runtime.NO_HINTS);
		rt.schedule(callPrice, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		
		Task call = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(final Runtime rt, final Task current) throws Exception {
				
				Task iterations = ForTask.createFor(rt, new LongRange(N), new ForBody<Long>() {
					@Override
					public void iterate(final Long o, Runtime rt, Task current) {
						double eps = StdRandom.gaussian();
			            double price = S * Math.exp(r*T - 0.5*sigma*sigma*T + sigma*eps*Math.sqrt(T));
			            final double value = Math.max(price - X, 0);
			            Task s = rt.createAtomicTask(new Body() {

							@Override
							public void execute(Runtime rt, Task current)
									throws Exception {
								saveCall += value / N;
							}
			            }, pCall, Runtime.NO_HINTS);
			            rt.schedule(s, current, Runtime.NO_DEPS);
					}		
				 });
				rt.schedule(iterations, current, Runtime.NO_DEPS);
				Task save = rt.createNonBlockingTask(new Body() {
					@Override
					public void execute(Runtime rt, Task current)
							throws Exception {
						saveCall = Math.exp(-r*T) * saveCall;
					}
				}, Runtime.NO_HINTS);
				ArrayList<Task> ts = new ArrayList<Task>();
				ts.add(iterations);
				rt.schedule(save, current, ts);
			}
			
		}, Runtime.NO_HINTS);
		rt.schedule(call, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		
		Task call2 = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(final Runtime rt, final Task current) throws Exception {
				
				Task iterations = ForTask.createFor(rt, new LongRange(N), new ForBody<Long>() {
					@Override
					public void iterate(final Long o, Runtime rt, Task current) {
						double price = S;
			            double dt = T/10000.0;
			            for (double t = 0; t <= T; t = t + dt) {
			                price += r*price*dt +sigma*price*Math.sqrt(dt)*StdRandom.gaussian();
			            }
			            final double value = Math.max(price - X, 0);
			            Task s = rt.createAtomicTask(new Body() {

							@Override
							public void execute(Runtime rt, Task current)
									throws Exception {
								saveCall2 += value / N;
							}
			            }, pCall, Runtime.NO_HINTS);
			            rt.schedule(s, current, Runtime.NO_DEPS);
					}		
				 });
				rt.schedule(iterations, current, Runtime.NO_DEPS);
				Task save = rt.createNonBlockingTask(new Body() {
					@Override
					public void execute(Runtime rt, Task current)
							throws Exception {
						saveCall = Math.exp(-r*T) * saveCall2;
					}
				}, Runtime.NO_HINTS);
				ArrayList<Task> ts = new ArrayList<Task>();
				ts.add(iterations);
				rt.schedule(save, current, ts);
			}
			
		}, Runtime.NO_HINTS);
		rt.schedule(call2, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		
		rt.shutdown();
		
        be.end();
        if (be.verbose) {
    		System.out.println(saveCallPrice);
    		System.out.println(saveCall);
    		System.out.println(saveCall2);
        }
	}

}
