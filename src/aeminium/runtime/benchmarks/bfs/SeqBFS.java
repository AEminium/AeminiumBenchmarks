package aeminium.runtime.benchmarks.bfs;

import java.util.Random;

public class SeqBFS {
	public static void main(String[] args) {
		int target = 23;
		int depth = 23;
		if (args.length > 0) depth = Integer.parseInt(args[0]);
		
		FjBFS searcher = new FjBFS(target, Graph.randomIntGraph(depth, 2, new Random(1L)), 21);
		long start = System.nanoTime();
		int f = searcher.seqCount();
		long end = System.nanoTime();
		System.out.println("Found " + f + " occurrences of " + target
				+ " and took " + (end - start) + " nanoseconds.");
	}
}
