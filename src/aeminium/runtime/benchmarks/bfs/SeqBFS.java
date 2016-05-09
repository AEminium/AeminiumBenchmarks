package aeminium.runtime.benchmarks.bfs;

import java.util.Random;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqBFS {
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int target = Graph.DEFAULT_TARGET;
		int depth = Graph.DEFAULT_DEPTH;
		if (be.args.length > 0) depth = Integer.parseInt(be.args[0]);

		Graph g = Graph.randomIntGraph(depth, Graph.DEFAULT_WIDTH, new Random(1L));
		while (!be.stop()) {
			be.start();
			int f = seqCount(target, g);
			be.end();
			if (be.verbose) {
				System.out.println("Found " + f + " occurrences of " + target);
			}
		}
	}

	public static int seqCount(int target, Graph graph) {
		int t;
		if (target == graph.value) t = 1;
		else t = 0;

		Graph[] children = graph.children;
		for (int i = 0; i < children.length; i++) {
			t += seqCount(target, children[i]);
		}
		return t;
	}
}
