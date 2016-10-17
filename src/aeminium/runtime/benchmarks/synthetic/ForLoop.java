package aeminium.runtime.benchmarks.synthetic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;

public class ForLoop {
	
	public static boolean VERBOSE = false;
	
	public static int MAX_DEPTH = 10;
	
	public static int ALLOCATION_BEFORE = 10000000;
	
	public static int BEFORE = 1;
	private static int BEFORE_STATIC_FACTOR = 1;
	private static double BEFORE_SIDE_FACTOR = 0;
	
	private static double LEAFS = 0;
	private static double LEAFS_STATIC_FACTOR = 0;
	private static double LEAFS_SIDE_FACTOR = 0;
	
	private static double KERNELS = 0;
	
	public static Task createForLoop(Runtime r, final int depth) {
		Task t = ForTask.createFor(r, new Range(0,1000), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				work((int) (BEFORE * (BEFORE_STATIC_FACTOR + i * BEFORE_SIDE_FACTOR)));
				if (depth == 0) {
					work((int) (LEAFS * (LEAFS_STATIC_FACTOR + i * LEAFS_SIDE_FACTOR)));	
				}
				if (depth > 0) createForLoop(r, depth-1);
			}}, Hints.LOOPS);
		return t;
		
	}
	
	
	public static int work(int its) {
		int r = 1;
		try {
			String text = "This is useless work";
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			for (int i=0; i<its; i++) {
				byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
				r *= hash.length;
			}
			return r;
		} catch (NoSuchAlgorithmException e) {
			
		}
		return 0;
	}
	
	public static int[] allocate(int size) {
		if (size == 0) return null;
		int[] a =  new int[size];
		a[size-1] = 1;
		return a;
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		if (be.verbose) {
			VERBOSE = true;
		}
		
		if (be.args.length < 10) {
			System.out.println("Not enough arguments");
			System.exit(1);
		}
		
		boolean parallel = be.args[0].equals("-p");
		MAX_DEPTH = Integer.parseInt(be.args[1]);
		ALLOCATION_BEFORE = Integer.parseInt(be.args[2]);
		
		BEFORE = Integer.parseInt(be.args[3]);
		BEFORE_STATIC_FACTOR = Integer.parseInt(be.args[4]);
		BEFORE_SIDE_FACTOR = Integer.parseInt(be.args[5]);
		
		LEAFS = Integer.parseInt(be.args[6]);
		LEAFS_STATIC_FACTOR = Integer.parseInt(be.args[7]);
		LEAFS_SIDE_FACTOR = Integer.parseInt(be.args[8]);
		
		KERNELS = Integer.parseInt(be.args[9]);
		
		if (parallel) {
			Runtime r = Factory.getRuntime();
			while (!be.stop()) {
				be.start();
				r.init();
				allocate(ALLOCATION_BEFORE);
				for (int i=0; i<KERNELS; i++)
					createForLoop(r, MAX_DEPTH).getResult();
				r.shutdown();
				be.end();
			}	
		} else {
			while (!be.stop()) {
				be.start();
				// TODO
				be.end();
			}
		}
		
	}
}
