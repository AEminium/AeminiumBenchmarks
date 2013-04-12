package aeminium.runtime.benchmarks.bfs;

import java.util.Random;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqBFS {
	public static void main(String[] args) {
		Benchmark b = new Benchmark(args);
		int target = Graph.DEFAULT_TARGET;
		int depth = Graph.DEFAULT_DEPTH;
		if (args.length > 0) depth = Integer.parseInt(args[0]);
		
		Graph g = Graph.randomIntGraph(depth, Graph.DEFAULT_WIDTH, new Random(1L));
		
		b.start();
		int f = seqCount(target, g);
		b.end();
		if (b.verbose) {
			System.out.println("Found " + f + " occurrences of " + target);
		}
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
