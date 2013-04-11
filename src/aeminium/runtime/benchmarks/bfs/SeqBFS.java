package aeminium.runtime.benchmarks.bfs;

import java.util.Random;

public class SeqBFS {
	public static void main(String[] args) {
		int target = Graph.DEFAULT_TARGET;
		int depth = Graph.DEFAULT_DEPTH;
		if (args.length > 0) depth = Integer.parseInt(args[0]);
		
		Graph g = Graph.randomIntGraph(depth, Graph.DEFAULT_WIDTH, new Random(1L));
		
		long start = System.nanoTime();
		int f = seqCount(target, g);
		long end = System.nanoTime();
		System.out.println("Found " + f + " occurrences of " + target
				+ " and took " + (end - start) + " nanoseconds.");
	}
	
	public static int seqCount(int target, Graph graph) {
		int t;
		if (target == graph.value)
			t = 1;
		else
			t = 0;

		for (int i = 0; i < graph.children.length; i++) {
			t += seqCount(target, graph.children[i]);
		}
		return t;
	}
}
