package aeminium.runtime.benchmarks.helpers;

import java.util.ArrayList;
import java.util.List;

public class Benchmark {
	public String[] args;
	public boolean verbose = true;
	
	private long start;
	private long end;
	
	private static final long NPS = (1000L * 1000 * 1000);
	
	
	
	public Benchmark(String[] old){
		List<String> nargs = new ArrayList<String>();
		for (String a : old) {
			if (a.equals("--quiet") || a.equals("-q")) {
				verbose = false;
			} else {
				nargs.add(a);
			}
		}
		if (nargs.size() > 0) {
			args = new String[nargs.size()];
			for (int i=0; i < nargs.size(); i++) {
				args[i] = nargs.get(i);
			}
		} else {
			args = new String[0];
		}
	}
	
	public void start() {
		start = System.nanoTime();
	}
	
	public void end() {
		end = System.nanoTime();
		System.out.println((((end - start) * 1.0)/NPS));
		System.gc();
	}
}
