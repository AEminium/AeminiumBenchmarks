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

import java.util.Random;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeBFS {

	public static class SearchBody implements Body {
		public volatile int value;
		private Graph graph;
		private SearchBody[] bodies;
		private Task[] tasks;
		private int threshold;
		
		public SearchBody(int target, Graph graph, int threshold) {
			this.value = target;
			this.graph = graph;
			this.bodies = new SearchBody[graph.children.length];
			this.tasks = new Task[graph.children.length];
			this.threshold = threshold;
		}
		
		@Override
		public void execute(Runtime rt, Task current) {
			if (Graph.probe(graph, threshold)) {
				value = SeqBFS.seqCount(value, graph);
			} else {
				value = ((value == graph.value) ? 1 : 0);
				
				for (int i = 0; i < graph.children.length; i++) {
					bodies[i] = new SearchBody(value, graph.children[i], threshold);
					tasks[i] = rt.createNonBlockingTask(bodies[i], Runtime.NO_HINTS);
					rt.schedule(tasks[i], current, Runtime.NO_DEPS);
				}
				
				for (int i = 0; i < graph.children.length; i++) {
					tasks[i].getResult();
					value += bodies[i].value;
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
		if (be.args.length > 1) depth = Integer.parseInt(be.args[1]);
		
		Graph g = Graph.randomIntGraph(depth, Graph.DEFAULT_WIDTH, new Random(1L));
		
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		SearchBody body = new AeBFS.SearchBody(1, g, threshold);
		Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
		rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Found " + body.value + " occurrences of " + target);
		}
	}
}
