package aeminium.runtime.benchmarks.helpers;

public class Benchmark {
	static final long NPS = (1000L * 1000 * 1000);
	
	long start;
	long end;
	
	public boolean verbose = true;
	
	public Benchmark(String[] args){
		for (String a : args) {
			if (a.equals("--quiet") || a.equals("-q")) {
				verbose = false;
			}
		}
	}
	
	public void start() {
		start = System.nanoTime();
	}
	
	public void end() {
		end = System.nanoTime();
		System.out.println((((end - start) * 1.0)/NPS));
	}
}
