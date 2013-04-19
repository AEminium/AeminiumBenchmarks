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

public class Graph {

	public static int DEFAULT_DEPTH = 23;
	public static int DEFAULT_WIDTH = 2;
	public static int DEFAULT_TARGET = 5;
	
	public int value;
	public Graph[] children;

	public Graph(int value, int n) {
		this.value = value;
		this.children = new Graph[n];
	}

	public static Graph randomIntGraph(int depth, int width, Random r) {
		Graph root = new Graph(r.nextInt() % 10, (depth > 0) ? width : 0);
		if (depth > 0) {
			for (int i = 0; i < width; i++) {
				root.children[i] = randomIntGraph(depth - 1, width, r);
			}
		}
		return root;
	}
	
	public static boolean probe(Graph graph, int threshold) {
		Graph tmp = graph;
		while(tmp.children.length > 0) {
			if (threshold-- < 0) return false;
			tmp = tmp.children[0];
		}
		return true;
		
	}

}
