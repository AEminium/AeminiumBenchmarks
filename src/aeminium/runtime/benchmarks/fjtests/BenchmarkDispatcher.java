package aeminium.runtime.benchmarks.fjtests;

import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkSuite;

public class BenchmarkDispatcher {
	public static void main(String[] args) {
		if (args.length == 0 || args[0].equals("-l")) {
			listBenchmarkSuites();
		} else {
			executeBenchmarkSuite(args[0]);
		}
		
	}
	
	
	private static void listBenchmarkSuites() {
		Class<?>[] suites = new Class<?>[] {
				BFSBenchmarkSuite.class,
				FFTBenchmarkSuite.class,
				FibonacciBenchmarkSuite.class,
				IntegrateBenchmarkSuite.class,
				LCSBenchmarkSuite.class,
				LogCounterBenchmarkSuite.class,
				MergeSortBenchmarkSuite.class
		};
		
		for (Class<?> klass : suites) {
			System.out.println(klass.getSimpleName());
		}
	}


	public static void executeBenchmarkSuite(String name) {

		BenchmarkSuite i;
		try {
			i = (BenchmarkSuite) Class.forName("aeminium.runtime.benchmarks.fjtests." + name).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		for (Benchmark test : i.getTests()) {
			long cold = test.run();
			System.gc();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			long warm = test.run();
			System.out.println(String.format("%30s: %18d %18d", test.getName(), cold,
					warm));
		}
	}
}
