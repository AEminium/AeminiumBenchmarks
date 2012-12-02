package aeminium.runtime.benchmarks.blackscholes;

import java.util.ArrayList;
import java.util.Collection;
import aeminium.runtime.Body;
import aeminium.runtime.DataGroup;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
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

public class BlackScholesAeminium {
	public static Runtime rt;
	public static double callPriceValue;
	public static double callValue;
	public static double call2Value;

	public static int NCalc2 = 10000;
	public static double sumCalc2 = 0.0;
	public static double[] sumCalc2Array;
	public static DataGroup dg;
	public static int numberOfTasks;

	public static void main(String[] args) {
		long initialTime = System.currentTimeMillis();

		rt = Factory.getRuntime();
		rt.init();
		dg = rt.createDataGroup();

		double S = Double.parseDouble(args[0]);
		double X = Double.parseDouble(args[1]);
		double r = Double.parseDouble(args[2]);
		double sigma = Double.parseDouble(args[3]);
		double T = Double.parseDouble(args[4]);

		numberOfTasks = Integer.parseInt(args[5]);
		sumCalc2Array = new double[numberOfTasks];

		Collection<Task> prev = new ArrayList<Task>();

		Task init1 = callPriceTask(Runtime.NO_PARENT, Runtime.NO_DEPS, S, X, r, sigma, T);
		Task init2 = callTask(Runtime.NO_PARENT, Runtime.NO_DEPS, S, X, r, sigma, T);
		Task init3 = call2Task(Runtime.NO_PARENT, Runtime.NO_DEPS, S, X, r, sigma, T);
		prev.add(init1);
		prev.add(init2);
		prev.add(init3);

		print(Runtime.NO_PARENT, prev);

		rt.shutdown();

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}

	// Black-Scholes formula
	private static Task callPriceTask(Task current, Collection<Task> prev, final double S, final double X, final double r, final double sigma, final double T) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				double d1 = (Math.log(S / X) + (r + sigma * sigma / 2) * T) / (sigma * Math.sqrt(T));
				double d2 = d1 - sigma * Math.sqrt(T);
				callPriceValue = S * Gaussian.Phi(d1) - X * Math.exp(-r * T) * Gaussian.Phi(d2);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	// estimate by Monte Carlo simulation
	private static Task callTask(Task current, Collection<Task> prev, final double S, final double X, final double r, final double sigma, final double T) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				int N = 10000;
				double sum = 0.0;
				for (int i = 0; i < N; i++) {
					double eps = StdRandom.gaussian();
					double price = S * Math.exp(r * T - 0.5 * sigma * sigma * T + sigma * eps * Math.sqrt(T));
					double value = Math.max(price - X, 0);
					sum += value;
				}
				double mean = sum / N;

				callValue = Math.exp(-r * T) * mean;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	// estimate by Monte Carlo simulation
	private static Task call2Task(Task current, Collection<Task> prev, final double S, final double X, final double r, final double sigma, final double T) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				Collection<Task> prev = new ArrayList<Task>();

				int step = NCalc2 / numberOfTasks;
				int position = 0;

				for (int taskNumber = 0; taskNumber < numberOfTasks; taskNumber++) {
					if (taskNumber == numberOfTasks - 1) {
						Task init1 = call2TaskFor(current, Runtime.NO_DEPS, position, NCalc2, S, X, r, sigma, T, taskNumber);
						prev.add(init1);
					} else {
						Task init1 = call2TaskFor(current, Runtime.NO_DEPS, position, position += step, S, X, r, sigma, T, taskNumber);
						prev.add(init1);
					}
				}
				call2TaskCalc(current, prev, S, X, r, sigma, T);

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task call2TaskFor(Task current, Collection<Task> prev, final int paramI, final int paramP, final double S, final double X, final double r, final double sigma, final double T,
			final int taskNumber) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				sumCalc2Array[taskNumber] = 0.0;
				for (int i = paramI; i < paramP; i++) {
					double price = S;
					double dt = T / 10000.0;
					for (double t = 0; t <= T; t = t + dt) {
						price += r * price * dt + sigma * price * Math.sqrt(dt) * StdRandom.gaussian();
					}
					double value = Math.max(price - X, 0);
					sumCalc2Array[taskNumber] += value;
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task call2TaskCalc(Task current, Collection<Task> prev, final double S, final double X, final double r, final double sigma, final double T) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				for (int i = 0; i < numberOfTasks; i++)
					sumCalc2 += sumCalc2Array[i];

				double mean = sumCalc2 / NCalc2;
				call2Value = Math.exp(-r * T) * mean;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task print(Task current, Collection<Task> prev) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				System.out.println(callPriceValue);
				System.out.println(callValue);
				System.out.println(call2Value);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}
}
