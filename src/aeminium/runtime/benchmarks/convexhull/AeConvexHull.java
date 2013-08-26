package aeminium.runtime.benchmarks.convexhull;

import java.util.ArrayList;
import java.util.Random;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeConvexHull {
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

		
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		ArrayList<Point> data = ConvexHull.generateData(size, new Random(1L));
		
		be.start();
		rt.init();
		
		ArrayList<Point> result = AeConvexHull.quickHull(data, rt, threshold);
		
		rt.shutdown();
		be.end();
		if (be.verbose)
			System.out.println(result.size());

	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Point> quickHull(final ArrayList<Point> points,
			Runtime rt, final int threshold) {
		if (points.size() < 3)
			return (ArrayList<Point>) points.clone();
		
		final ArrayList<Point> convexHull = new ArrayList<Point>();
		Body setupB = new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {

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
				
				Task one = rt.createBlockingTask(new AeConvexHullBody(A, B, rightSet, convexHull, threshold), Hints.RECURSION);
				rt.schedule(one, current, Runtime.NO_DEPS);
				Task two = rt.createBlockingTask(new AeConvexHullBody(B, A, leftSet, convexHull, threshold), Hints.RECURSION);
				rt.schedule(two, current, Runtime.NO_DEPS);
			}
			
		};
		Task setup = rt.createNonBlockingTask(setupB, Hints.RECURSION);
		rt.schedule(setup, Runtime.NO_PARENT, Runtime.NO_DEPS);
		return convexHull;
	}
}


class AeConvexHullBody implements Body {
	
	private Point A;
	private Point B;
	private ArrayList<Point> set;
	private ArrayList<Point> hull;
	private int threshold;
	
	public AeConvexHullBody(Point A, Point B, ArrayList<Point> set,
			ArrayList<Point> hull, int threshold) {
		this.A = A;
		this.B = B;
		this.set = set;
		this.hull = hull;
		this.threshold = threshold;
	}
	
	public void execute(Runtime rt, Task current) throws Exception {
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

		
		if (Benchmark.useThreshold ? set.size() < threshold : rt.parallelize(current)) {
			SeqConvexHull.hullSet(A,P,leftSetAP,hull);
			SeqConvexHull.hullSet(P,B,leftSetPB,hull);
		} else {
			Task one = rt.createBlockingTask(new AeConvexHullBody(A, P, leftSetAP, hull, threshold), Hints.RECURSION);
			rt.schedule(one, current, Runtime.NO_DEPS);
			Task two = rt.createBlockingTask(new AeConvexHullBody(P, B, leftSetPB, hull, threshold), Hints.RECURSION);
			rt.schedule(two, current, Runtime.NO_DEPS);
		}
	}
}
