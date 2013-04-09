package aeminium.runtime.benchmarks.bfs;

import java.util.Random;

public class SeqBFS {
	public static void main(String[] args) {
		int target = 23;

		FjBFS searcher = new FjBFS(target, Graph.randomIntGraph(target, 2, new Random(1234567890)), 21);
		long start = System.nanoTime();
		int f = searcher.seqCount();
		long end = System.nanoTime();
		System.out.println("Found " + f + " occurrences of " + target
				+ " and took " + (end - start) + " nanoseconds.");
	}
}
