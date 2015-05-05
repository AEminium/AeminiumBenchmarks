package aeminium.runtime.benchmarks.blackscholes;

import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;
import jsr166e.RejectedExecutionException;
import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjBlackScholes {

	public static double callPrice(double S, double X, double r, double sigma, double T) {
		double d1 = (Math.log(S / X) + (r + sigma * sigma / 2) * T) / (sigma * Math.sqrt(T));
		double d2 = d1 - sigma * Math.sqrt(T);
		return S * Gaussian.Phi(d1) - X * Math.exp(-r * T) * Gaussian.Phi(d2);
	}

	public static double call(double S, double X, double r, double sigma, double T, long N, ForkJoinPool g, int th) {
		double sum = DCall.computeSum(g, N, S, X, r, sigma, T, th);
		double mean = sum / N;
		return Math.exp(-r * T) * mean;
	}

	public static final class DCall extends RecursiveAction {
		private static final long serialVersionUID = -4117817369633015698L;

		static double computeSum(ForkJoinPool pool, long N, double S, double X, double r, double sigma, double T, int th) {
			DCall q = new DCall(N, S, X, r, sigma, T, th);
			try {
				pool.invoke(q);
			} catch (RejectedExecutionException ex) {
				ex.printStackTrace();
			}
			return q.sum;
		}

		final long top;
		double sum;
		double S;
		double X;
		double r;
		double sigma;
		double T;
		private int threshold;

		DCall(long top, double S, double X, double r, double sigma, double T, int t) {
			this.top = top;
			this.S = S;
			this.X = X;
			this.r = r;
			this.sigma = sigma;
			this.T = T;
			this.threshold = t;
		}

		public final void compute() {
			if (top == 1) {
				double eps = StdRandom.gaussian();
				double price = S * Math.exp(r * T - 0.5 * sigma * sigma * T + sigma * eps * Math.sqrt(T));
				double value = Math.max(price - X, 0);
				sum = value;
			} else if (Benchmark.useThreshold ? top < threshold : !this.shouldFork()) {
				double s = 0;
				for (int i = 0; i < top; i++) {
					double price = S;
					double dt = T / 10000.0;
					for (double t = 0; t <= T; t = t + dt) {
						price += r * price * dt + sigma * price * Math.sqrt(dt) * StdRandom.gaussian();
					}
					double value = Math.max(price - X, 0);
					s += value;
				}
				sum = s;
			} else {
				DCall half1 = new DCall(top / 2, S, X, r, sigma, T, threshold);
				DCall half2 = new DCall(top / 2, S, X, r, sigma, T, threshold);
				invokeAll(half1, half2);
				sum = half1.sum + half2.sum;
			}

		}
	}

	// estimate by Monte Carlo simulation
	public static double call2(double S, double X, double r, double sigma, double T, long N, ForkJoinPool g, int th) {
		double sum = DCall2.computeSum(g, N, S, X, r, sigma, T, th);
		double mean = sum / N;
		return Math.exp(-r * T) * mean;
	}

	public static final class DCall2 extends RecursiveAction {
		private static final long serialVersionUID = -4117817369633015698L;

		static double computeSum(ForkJoinPool pool, long N, double S, double X, double r, double sigma, double T, int th) {
			DCall2 q = new DCall2(N, S, X, r, sigma, T, th);
			pool.invoke(q);
			return q.sum;
		}

		final long top;
		double sum;
		double S;
		double X;
		double r;
		double sigma;
		double T;
		private int threshold;

		DCall2(long top, double S, double X, double r, double sigma, double T, int t) {
			this.top = top;
			this.S = S;
			this.X = X;
			this.r = r;
			this.sigma = sigma;
			this.T = T;
			this.threshold = t;
		}

		public final void compute() {
			if (top == 1) {
				double price = S;
				double dt = T / 10000.0;
				for (double t = 0; t <= T; t = t + dt) {
					price += r * price * dt + sigma * price * Math.sqrt(dt) * StdRandom.gaussian();
				}
				double value = Math.max(price - X, 0);
				sum = value;
			} else if (top < threshold) {
				double sum = 0.0;
				for (int i = 0; i < top; i++) {
					double price = S;
					double dt = T / 10000.0;
					for (double t = 0; t <= T; t = t + dt) {
						price += r * price * dt + sigma * price * Math.sqrt(dt) * StdRandom.gaussian();
					}
					double value = Math.max(price - X, 0);
					sum += value;
				}
				this.sum = sum;
			} else {
				DCall2 half1 = new DCall2(top / 2, S, X, r, sigma, T, threshold);
				DCall2 half2 = new DCall2(top / 2, S, X, r, sigma, T, threshold);
				invokeAll(half1, half2);
				sum = half1.sum + half2.sum;
			}

		}
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		long N = 1000;
		if (be.args.length > 0) {
			N = Long.parseLong(be.args[0]);
		}

		int threshold = BlackScholes.DEFAULT_THRESHOLD;
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}

		ForkJoinPool g = new ForkJoinPool();
		while (!be.stop()) {
			StdRandom.setSeed(1L);
			be.start();

			double cP = callPrice(BlackScholes.S, BlackScholes.X, BlackScholes.r, BlackScholes.sigma, BlackScholes.T);
			double ca = call(BlackScholes.S, BlackScholes.X, BlackScholes.r, BlackScholes.sigma, BlackScholes.T, N, g, threshold);
			double c2 = call2(BlackScholes.S, BlackScholes.X, BlackScholes.r, BlackScholes.sigma, BlackScholes.T, N, g, threshold);
			be.end();
			if (be.verbose) {
				System.out.println(cP);
				System.out.println(ca);
				System.out.println(c2);
			}
		}
		g.shutdown();
	}
}
