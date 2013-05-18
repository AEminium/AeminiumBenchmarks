package aeminium.runtime.benchmarks.convexhull;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FJConvexHull extends RecursiveAction {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		int size = ConvexHull.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		
		int threshold = ConvexHull.DEFAULT_THRESHOLD;
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		}

		ArrayList<Point> data = ConvexHull.generateData(size, new Random(1L));
		ForkJoinPool pool = new ForkJoinPool();
		be.start();
		ArrayList<Point> result = FJConvexHull.quickHull(data, pool, threshold);
		be.end();
		if (be.verbose)
			System.out.println(result.size());

	}

	private Point A;
	private Point B;
	private ArrayList<Point> set;
	private ArrayList<Point> hull;
	private int threshold;

	public FJConvexHull(Point A, Point B, ArrayList<Point> set,
			ArrayList<Point> hull, int threshold) {
		this.A = A;
		this.B = B;
		this.set = set;
		this.hull = hull;
		this.threshold = threshold;
	}

	@Override
	protected void compute() {
		if (set.size() == 0)
			return;
		if (set.size() == 1) {
			Point p = set.get(0);
			set.remove(p);
			synchronized (hull) { hull.add(p); }
			return;
		}
		double dist = Double.MIN_VALUE;
		int furthestPoint = -1;
		for (int i = 0; i < set.size(); i++) {
			Point p = set.get(i);
			double distance = ConvexHull.distance(A, B, p);
			if (distance > dist) {
				dist = distance;
				furthestPoint = i;
			}
		}
		Point P = set.get(furthestPoint);
		set.remove(furthestPoint);
		synchronized (hull) { hull.add(P); }


		// Determine who's to the left of AP
		ArrayList<Point> leftSetAP = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point M = set.get(i);
			if (ConvexHull.pointLocation(A, P, M) == 1) {
				leftSetAP.add(M);
			}
		}

		// Determine who's to the left of PB
		ArrayList<Point> leftSetPB = new ArrayList<Point>();
		for (int i = 0; i < set.size(); i++) {
			Point M = set.get(i);
			if (ConvexHull.pointLocation(P, B, M) == 1) {
				leftSetPB.add(M);
			}
		}

		
		if (set.size() < threshold) {
			SeqConvexHull.hullSet(A,P,leftSetAP,hull);
			SeqConvexHull.hullSet(P,B,leftSetPB,hull);
		} else {
			FJConvexHull one = new FJConvexHull(A, P, leftSetAP, hull, threshold);
			FJConvexHull other = new FJConvexHull(P, B, leftSetPB, hull, threshold);
			invokeAll(one,other);
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Point> quickHull(ArrayList<Point> points,
			ForkJoinPool pool, int threshold) {
		ArrayList<Point> convexHull = new ArrayList<Point>();
		if (points.size() < 3)
			return (ArrayList<Point>) points.clone();
		// find extremals
		int minPoint = -1, maxPoint = -1;
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).x < minX) {
				minX = points.get(i).x;
				minPoint = i;
			}
			if (points.get(i).x > maxX) {
				maxX = points.get(i).x;
				maxPoint = i;
			}
		}
		Point A = points.get(minPoint);
		Point B = points.get(maxPoint);
		convexHull.add(A);
		convexHull.add(B);
		points.remove(A);
		points.remove(B);

		ArrayList<Point> leftSet = new ArrayList<Point>();
		ArrayList<Point> rightSet = new ArrayList<Point>();

		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (ConvexHull.pointLocation(A, B, p) == -1)
				leftSet.add(p);
			else
				rightSet.add(p);
		}

		FJConvexHull one = new FJConvexHull(A, B, rightSet, convexHull, threshold);
		FJConvexHull other = new FJConvexHull(B, A, leftSet, convexHull, threshold);
		pool.execute(other);
		pool.invoke(one);
		pool.invoke(other);
		return convexHull;
	}
}
