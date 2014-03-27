package aeminium.runtime.benchmarks.helpers;

import java.util.ArrayList;
import java.util.List;

import aeminium.runtime.implementations.Configuration;

public class Benchmark {
	public String[] args;
	public boolean verbose = true;
	public boolean debug = false;
	public int maxRepetitions = 30;

	private int si = 0;
	private int k = 3;
	private double last;
	private double[] reps;
	private long start;
	private long end;

	private static final long NPS = (1000L * 1000 * 1000);

	public static boolean useThreshold = Configuration.getProperty(Benchmark.class, "useThreshold", false);

	public Benchmark(String[] old) {
		List<String> nargs = new ArrayList<String>();
		for (String a : old) {
			if (a.equals("--quiet") || a.equals("-q")) {
				verbose = false;
			} else if (a.startsWith("-r")) {
				String b = a.substring(2);
				maxRepetitions = Integer.parseInt(b);
			} else if (a.startsWith("-k")) {
				String b = a.substring(2);
				maxRepetitions = Integer.parseInt(b);
			} else if (a.equals("-d")) {
				debug = true;
			} else {
				nargs.add(a);
			}
		}
		if (nargs.size() > 0) {
			args = new String[nargs.size()];
			for (int i = 0; i < nargs.size(); i++) {
				args[i] = nargs.get(i);
			}
		} else {
			args = new String[0];
		}
		reps = new double[k];
	}

	public void start() {
		start = System.nanoTime();
	}

	public void end() {
		end = System.nanoTime();
		last = (((end - start) * 1.0) / NPS);
		if (debug) System.out.println(last);
		reps[si++ % k] = last;
	}

	public boolean stop() {
		if (si < k) return false;
		double cov = Stats.doubleCoV(reps);
		if (debug) System.out.println("COV: " + cov);
		if (cov < 0.02 || si > maxRepetitions) {
			System.out.println(Stats.doubleMean(reps));
			if (this.debug) {
				return true;
			} else {
				System.exit(0);
			}
		}
		return false;
	}
}
