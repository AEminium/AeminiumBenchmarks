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
import aeminium.runtime.implementations.Factory;

public class AeBFS {

	public static class SearchBody implements Body {
		public volatile int value;
		private int threshold;
		private Graph graph;
		
		public SearchBody(int target, Graph graph, int threshold) {
			this.value = target;
			this.threshold = threshold;
			this.graph = graph;
		}
		
		@Override
		public void execute(Runtime rt, Task current) {
			if (FjBFS.probe(graph, threshold)) {
				value = FjBFS.seqCount(value, graph);
			} else {
				int found;
				if (value == graph.value) found = 1; else found = 0;
				Task[] tasks = new Task[graph.children.length];
				SearchBody[] bodies = new SearchBody[graph.children.length];
				for(int i=0;i<graph.children.length;i++){
					bodies[i] = new SearchBody(value, graph.children[i], threshold);
					tasks[i] = rt.createNonBlockingTask(bodies[i], Runtime.NO_HINTS);
					rt.schedule(tasks[i], current, Runtime.NO_DEPS);
				}
				for (int i=0; i<tasks.length;i++) {
					tasks[i].getResult();
					found += bodies[i].value;
				}
				value = found;
			}
			
			
		}
	}

	public static SearchBody createSearchBody(final Runtime rt, final int target, Graph graph, int threshold) {
		return new AeBFS.SearchBody(target, graph, threshold);
	}

	public static void main(String[] args) {
		int target = 23;
	    if (args.length >= 1) {
	        target = Integer.parseInt(args[0]);
	    }
		
		Runtime rt = Factory.getRuntime();
		rt.init();
		SearchBody body = createSearchBody(rt, 1, Graph.randomIntGraph(target, 2, new Random(1234567890)), 21);
		Task t1 = rt.createNonBlockingTask(body, Runtime.NO_HINTS);
		
		long start = System.nanoTime();
		rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
		rt.shutdown();
		long end = System.nanoTime();
		System.out.println("Found " + body.value + " occurrences of " + target
				+ " and took " + (end - start) + " nanoseconds.");
	}
}
