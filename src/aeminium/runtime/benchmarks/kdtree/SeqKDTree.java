package aeminium.runtime.benchmarks.kdtree;

import java.util.Arrays;
import java.util.Comparator;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqKDTree {
	
	// 2D k-d tree
	private SeqKDTree childA, childB;
	private Point point; // defines the boundary
	private int d; // dimension: 0 => left/right split, 1 => up/down split
	
	public SeqKDTree(Point[] points, int depth) {
		childA = null;
		childB = null;
		d = depth % 2;

		// find median by sorting in dimension 'd' (either x or y)
		Comparator<Point> comp = new Point.PointComp(d);
		Arrays.sort(points, comp);

		int median = (points.length - 1) / 2;
		point = points[median];

		// Create childA and childB recursively.
		if (median > 0) {
			childA = new SeqKDTree(Arrays.copyOfRange(points, 0, median), depth + 1);
		}
		if (median + 1 < points.length) {
			childB = new SeqKDTree(Arrays.copyOfRange(points, median+1, points.length), depth + 1);
		}
	}
	
	public Point findClosest(Point target) {
		Point closest = point.equals(target) ? Point.INFINITY : point;
		double bestDist = closest.distance(target);
		double spacing = target.coord[d] - point.coord[d];
		SeqKDTree rightSide = (spacing < 0) ? childA : childB;
		SeqKDTree otherSide = (spacing < 0) ? childB : childA;

		if (rightSide != null) {
			Point candidate = rightSide.findClosest(target);
			if (candidate.distance(target) < bestDist) {
				closest = candidate;
				bestDist = closest.distance(target);
			}
		}

		if (otherSide != null && (Math.abs(spacing) < bestDist)) {
			Point candidate = otherSide.findClosest(target);
			if (candidate.distance(target) < bestDist) {
				closest = candidate;
				bestDist = closest.distance(target);
			}
		}

		return closest;
	}
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = Point.DEFAULT_SIZE;
		if (args.length > 0)
			size = Integer.parseInt(args[0]);
		
		Point[] points = Point.generatePoints(size);
		Point[] closest = new Point[size];
		
		be.start();
		SeqKDTree tree = new SeqKDTree(points, 0);
		
		for (int i = 0; i < points.length; i++) { 
			closest[i] = tree.findClosest(points[i]); 
		}
		
		Point markPoint = new Point(10, 100);
		Point closestP = tree.findClosest(markPoint); 
		be.end();
		if (be.verbose) {
			System.out.println("Closest:" + closestP);
		}
		
	}
	
}
