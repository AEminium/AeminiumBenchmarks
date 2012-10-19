package aeminium.runtime.benchmarks.fjtests;

import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.gaknapsack.AeGA;
import aeminium.runtime.benchmarks.gaknapsack.SeqGA;

public class GABenchmarkSuite implements BenchmarkSuite {
	
	Benchmark[] tests;
	
	protected int PARAMETER = 100000000;
	protected int THRESHOLD = 48;
	
	public GABenchmarkSuite() {
		tests = new Benchmark[3];
		
		tests[0] = new Benchmark() {
			
			@Override
			public String getName() {
				return "Sequential Knapsack GA";
			}
			
			@Override
			public long run() {
				SeqGA.debug = false;
				long t = System.nanoTime();
				SeqGA.main(new String[0]);
				return System.nanoTime() - t;
			}
		};
		tests[1] = new Benchmark() {
			
			@Override
			public String getName() {
				return "Unavailable";
			}
			
			@Override
			public long run() {
				return 0;
			}
		};;
		
		tests[2] = new Benchmark() {
			
			@Override
			public String getName() {
				return "Aeminium Knapsack GA";
			}
			
			@Override
			public long run() {
				AeGA.debug = false;
				long t = System.nanoTime();
				AeGA.main(new String[0]);
				return System.nanoTime() - t;
			}
		};
		
	}
	
	public static void main(String[] args) {
		GABenchmarkSuite suite = new GABenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}
	
	public Benchmark[] getTests() {
		return tests;
	}

}
