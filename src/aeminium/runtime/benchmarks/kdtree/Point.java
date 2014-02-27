package aeminium.runtime.benchmarks.kdtree;

import java.util.Comparator;
import java.util.Random;

class Point {
	public static final int DEFAULT_SIZE = 1000000;
	public static final Point INFINITY = new Point(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	public static final int DEFAULT_THRESHOLD = 100;

	public double[] coord; // coord[0] = x, coord[1] = y

	public Point(double x, double y) {
		coord = new double[] { x, y };
	}

	public double getX() {
		return coord[0];
	}

	public double getY() {
		return coord[1];
	}

	public double distance(Point p) {
		double dX = getX() - p.getX();
		double dY = getY() - p.getY();
		return Math.sqrt(dX * dX + dY * dY);
	}

	public boolean equals(Point p) {
		return (getX() == p.getX()) && (getY() == p.getY());
	}

	public String toString() {
		return "(" + getX() + ", " + getY() + ")";
	}

	public static class PointComp implements Comparator<Point> {
		int d; // the dimension to compare in (0 => x, 1 => y)

		public PointComp(int dimension) {
			d = dimension;
		}

		public int compare(Point a, Point b) {
			return (int) (a.coord[d] - b.coord[d]);
		}
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
