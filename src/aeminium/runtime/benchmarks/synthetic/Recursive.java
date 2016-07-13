package aeminium.runtime.benchmarks.synthetic;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;


public class Recursive {
	
	public static boolean VERBOSE = false;
	public static int MAX_DEPTH = 10;
	
	public static int ALLOCATION_BEFORE = 10000000;
	
	public static int BEFORE = 1;
	private static int BEFORE_STATIC_FACTOR = 1;
	private static double BEFORE_SIDE_FACTOR = 0;
	private static long BEFORE_DEPTH_FACTOR = 0;
	
	private static double LEAFS = 0;
	private static double LEAFS_STATIC_FACTOR = 0;
	private static double LEAFS_SIDE_FACTOR = 0;
	
	
	public static int BRANCHING = 2;
	private static long BRANCHING_DEPTH_FACTOR = 0;
	private static int BRANCHING_STATIC_FACTOR = 0;
	private static double BRANCHING_SIDE_FACTOR = 0;
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		if (be.verbose) {
			VERBOSE = true;
		}
		
		if (be.args.length < 14) {
			System.out.println("Not enough arguments");
			System.exit(1);
		}
		
		boolean parallel = be.args[0].equals("-p");
		MAX_DEPTH = Integer.parseInt(be.args[1]);
		ALLOCATION_BEFORE = Integer.parseInt(be.args[2]);
		
		BEFORE = Integer.parseInt(be.args[3]);
		BEFORE_STATIC_FACTOR = Integer.parseInt(be.args[4]);
		BEFORE_SIDE_FACTOR = Integer.parseInt(be.args[5]);
		BEFORE_DEPTH_FACTOR = Integer.parseInt(be.args[6]);
		
		LEAFS = Integer.parseInt(be.args[7]);
		LEAFS_STATIC_FACTOR = Integer.parseInt(be.args[8]);
		LEAFS_SIDE_FACTOR = Integer.parseInt(be.args[9]);
		
		BRANCHING = Integer.parseInt(be.args[10]);
		BRANCHING_DEPTH_FACTOR = Integer.parseInt(be.args[11]);
		BRANCHING_STATIC_FACTOR = Integer.parseInt(be.args[12]);
		BRANCHING_SIDE_FACTOR = Integer.parseInt(be.args[13]);
		
		
		if (parallel) {
			Runtime r = Factory.getRuntime();
			while (!be.stop()) {
				be.start();
				r.init();
				Task t = r.createNonBlockingTask(new TBody(0, 0), Runtime.NO_HINTS);
				r.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
				r.shutdown();
				be.end();
			}	
		} else {
			while (!be.stop()) {
				be.start();
				TBody.seq(0,0);
				be.end();
			}
		}
		
	}
	
	public static class TBody implements Body {
		
		public volatile long depth;
		public volatile double side;

		public TBody(long n, double side) {
			this.depth = n;
			this.side = side;
		}
		
		public static int[] allocate(int size) {
			int[] a =  new int[size];
			a[size-1] = 1;
			return a;
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
		
		
		public static void seq(long depth, double side) {
			if (VERBOSE) {
				System.out.println("Depth: " + depth + ", side: " + side);
			}
			allocate(ALLOCATION_BEFORE);
			work((int) (BEFORE * side));
			if (depth < MAX_DEPTH) {
				for (int i=0; i< BRANCHING; i++) {
					seq(depth+1, side + (i * 100.0 )*(depth+1) );
				}
			}
		}
		
		@Override
		public void execute(Runtime rt, Task current) {
			if (VERBOSE) {
				System.out.println("Depth: " + depth + ", side: " + side);
			}
			if (!rt.parallelize(current)) {
				seq(depth, side);
			} else {
				allocate(ALLOCATION_BEFORE);
				work((int) (BEFORE * (BEFORE_STATIC_FACTOR + side * BEFORE_SIDE_FACTOR + depth * BEFORE_DEPTH_FACTOR)));
				Task[] ts = new Task[BRANCHING];
				if (depth < MAX_DEPTH) {
					for (int i=0; i< (BRANCHING * (BRANCHING_STATIC_FACTOR + side * BRANCHING_SIDE_FACTOR + depth * BRANCHING_DEPTH_FACTOR) ); i++) {
						Body b = new TBody(depth+1, side + (i * 100.0 )*(depth+1));
						ts[i] = rt.createNonBlockingTask(b, Hints.RECURSION);
						rt.schedule(ts[i], Runtime.NO_PARENT, Runtime.NO_DEPS);
					}
					for (int i=0; i< BRANCHING; i++) {
						ts[i].getResult();
					}
				} else {
					work((int) (LEAFS * (LEAFS_STATIC_FACTOR + side * LEAFS_SIDE_FACTOR)));	
				}
			}
		}
	}
}
