package aeminium.runtime.benchmarks.fjtests;

import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkSuite;

public class MakeSureEverythingRuns {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeSuite(BFSBenchmarkSuite.class);
		executeSuite(FFTBenchmarkSuite.class);
		executeSuite(FibonacciBenchmarkSuite.class);
		executeSuite(IntegrateBenchmarkSuite.class);
		executeSuite(IntegrateBenchmarkSuite.class);
		executeSuite(LCSBenchmarkSuite.class);
		executeSuite(LogCounterBenchmarkSuite.class);
		executeSuite(MergeSortBenchmarkSuite.class);

	}

	public static void executeSuite(Class<?> c) {
		BenchmarkSuite i;
		try {
			i = (BenchmarkSuite) c.newInstance();
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
