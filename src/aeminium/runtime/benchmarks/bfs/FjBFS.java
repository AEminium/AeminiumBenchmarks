/**
 * Copyright (c) 2010-11 The AEminium Project (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package aeminium.runtime.benchmarks.bfs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjBFS extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private int found;
	private int target;
	private Graph graph;
	private int threshold;

	public FjBFS(int target, Graph graph, int threshold) {
		this.target = target;
		this.graph = graph;
		this.threshold = threshold;
	}

	public int seqCount() {
		return SeqBFS.seqCount(target, graph);
	}
	
	public int parCount() {
		compute();
		return found;
	}

	@Override
	protected void compute() {

		if (Graph.probe(graph, threshold)) {
			found = seqCount();
		} else {
			if (target == graph.value) found = 1; else found = 0;
			Collection<FjBFS> futures = new ArrayList<FjBFS>();
			FjBFS tmp;
			for (int i=0;i<graph.children.length;i++) {
				tmp = new FjBFS(target, graph.children[i], threshold);
				invokeAll(tmp);
				futures.add(tmp);
			}
			for (FjBFS finder : futures) {
				try {
					finder.get();
					found += finder.found;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int target = Graph.DEFAULT_TARGET;
		int depth = Graph.DEFAULT_DEPTH;
		if (be.args.length > 0) depth = Integer.parseInt(be.args[0]);
		int threshold = Graph.DEFAULT_DEPTH - 8;
		if (be.args.length > 1) threshold = Integer.parseInt(be.args[1]);
		
		Graph g = Graph.randomIntGraph(depth, Graph.DEFAULT_WIDTH, new Random(1L));
		
		be.start();
		FjBFS searcher = new FjBFS(target, g, threshold);
		int f = searcher.parCount();
		be.end();
		if (be.verbose) {
			System.out.println("Found " + f + " occurrences of " + target);
		}
	}

}
