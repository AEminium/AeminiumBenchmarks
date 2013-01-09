package aeminium.runtime.benchmarks.kdtree;

import java.util.*;

class KDTree {
	// 2D k-d tree
	private KDTree childA, childB;
	private Point point; // defines the boundary
	private int d; // dimension: 0 => left/right split, 1 => up/down split

	public KDTree(List<Point> points, int depth) {
		childA = null;
		childB = null;
		d = depth % 2;

		// find median by sorting in dimension 'd' (either x or y)
		Comparator<Point> comp = new Point.PointComp(d);
		Collections.sort(points, comp);

		int median = (points.size() - 1) / 2;
		point = points.get(median);

		// Create childA and childB recursively.
		// WARNING: subList() does not create a true copy,
		// so the original will get modified.
		if (median > 0) {
			childA = new KDTree(points.subList(0, median), depth + 1);
		}
		if (median + 1 < points.size()) {
			childB = new KDTree(points.subList(median + 1, points.size()), depth + 1);
		}
	}

	public Point findClosest(Point target) {
		Point closest = point.equals(target) ? Point.INFINITY : point;
		double bestDist = closest.distance(target);
		double spacing = target.coord[d] - point.coord[d];
		KDTree rightSide = (spacing < 0) ? childA : childB;
		KDTree otherSide = (spacing < 0) ? childB : childA;

		/*
		 * The 'rightSide' is the side on which 'target' lies and the
		 * 'otherSide' is the other one. It is possible that 'otherSide' will
		 * not have to be searched.
		 */

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
		long initialTime = System.currentTimeMillis();

		List<Point> points = new ArrayList<Point>();// = generatePoints();
		for (int i = 0; i < 4000000; i++) {
			Point point = new Point(1 + i, 10 + i);
			points.add(point);
		}

		Point[] closest = new Point[points.size()];

		KDTree tree = new KDTree(points, 0); // WILL MODIFY 'points'

		/*
		 * for (int i = 0; i < points.size(); i++) { closest[i] =
		 * tree.findClosest(points.get(i)); }
		 */

		/*
		 * for (int i = 0; i < points.size(); i++) {
		 * System.out.println(points.get(i) + " is closest to " + closest[i]); }
		 */

		Point markPoint = new Point(10, 100);
		System.out.println("Closest:" + tree.findClosest(markPoint));

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}

	private static List<Point> generatePoints() {
		ArrayList<Point> points = new ArrayList<Point>();
		Random r = new Random();

		for (int i = 0; i < 1000; i++) {
			points.add(new Point(r.nextInt() % 1000, r.nextInt() % 1000));
		}

		return points;
	}
}