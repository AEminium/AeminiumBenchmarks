package aeminium.runtime.benchmarks.fjtests;

import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.Benchmark;
import aeminium.runtime.benchmarks.BenchmarkExecutor;
import aeminium.runtime.benchmarks.BenchmarkSuite;
import aeminium.runtime.benchmarks.pi.AeminiumPI;
import aeminium.runtime.benchmarks.pi.AeminiumPI.MainBody;
import aeminium.runtime.implementations.Factory;

public class PiBenchmarkSuite implements BenchmarkSuite {
	
	Benchmark[] tests;
	
	protected int PARAMETER = 100000000;
	protected int THRESHOLD = 48;
	
	public PiBenchmarkSuite() {
		tests = new Benchmark[3];
		
		tests[0] = null;
		tests[1] = null;
		
		tests[2] = new Benchmark() {
			
			Runtime rt = Factory.getRuntime();
			
			@Override
			public String getName() {
				return "Aeminium Fibonacci";
			}
			
			@Override
			public long run() {

				rt.init();
				
				long start = System.nanoTime();
				MainBody body = AeminiumPI.createController(rt, THRESHOLD, PARAMETER);
				Task controller = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
				rt.schedule(controller, Runtime.NO_PARENT, Runtime.NO_DEPS);
				
				rt.shutdown();
				long end = System.nanoTime();
				return end-start;
			}
		};
		
	}
	
	public static void main(String[] args) {
		PiBenchmarkSuite suite = new PiBenchmarkSuite();
		new BenchmarkExecutor(suite.getTests()).run(args);
	}
	
	public Benchmark[] getTests() {
		return tests;
	}

}
