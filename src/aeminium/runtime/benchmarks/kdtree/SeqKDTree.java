package aeminium.runtime.benchmarks.kdtree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class SeqKDTree {
	
	static final long NPS = (1000L * 1000 * 1000);
	
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
		long initialTime = System.nanoTime();
		int size = 1000000;
		if (args.length > 0)
			size = Integer.parseInt(args[0]);
		
		Point[] points = generatePoints(size);
		Point[] closest = new Point[size];
		
		SeqKDTree tree = new SeqKDTree(points, 0);
		
		for (int i = 0; i < points.length; i++) { 
			closest[i] = tree.findClosest(points[i]); 
		}
		
		Point markPoint = new Point(10, 100);
		System.out.println("Closest:" + tree.findClosest(markPoint));
		long finalTime = System.nanoTime();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / NPS);
		
	}
	
	public static Point[] generatePoints(int size) {
		Point[] points = new Point[size];
		Random r = new Random(1L);

		for (int i = 0; i < size; i++) {
			points[i] = new Point(r.nextInt() % 1000, r.nextInt() % 1000);
		}

		return points;
	}
	
}
