package aeminium.runtime.benchmarks.kdtree;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjKDTree {

	// 2D k-d tree
	private FjKDTree childA, childB;
	private Point point; // defines the boundary
	private int d; // dimension: 0 => left/right split, 1 => up/down split
	private int threshold;
	private ForkJoinPool pool;

	public FjKDTree(ForkJoinPool pool, Point[] points, int depth, int threshold) {
		childA = null;
		childB = null;
		this.threshold = threshold;
		this.pool = pool;
		d = depth % 2;

		// find median by sorting in dimension 'd' (either x or y)
		Comparator<Point> comp = new Point.PointComp(d);
		Arrays.sort(points, comp);

		int median = (points.length - 1) / 2;
		point = points[median];
		createSubTrees(points, depth);
	}
	
	public void createSubTrees(final Point[] points, final int depth) {
		int median = (points.length - 1) / 2;
		if (depth < threshold)
			createSubTreesInPar(points, depth, median);
		else
			createSubTreesInSeq(points, depth, median);
	}
	
	private void createSubTreesInPar(final Point[] points, final int depth, final int median) {
		ForkJoinTask<Void> t1 = null, t2 = null;
		if (median > 0) {
			t1 = new ForkJoinTask<Void>() {
				private static final long serialVersionUID = 2537705569519846490L;

				@Override
				public Void getRawResult() {
					return null;
				}

				@Override
				protected void setRawResult(Void value) {
				}

				@Override
				protected boolean exec() {
					Point[] ps = Arrays.copyOfRange(points, 0, median);
					childA = new FjKDTree(pool, ps, depth + 1, threshold);
					return true;
				}
			};
			pool.submit(t1);
		}
		if (median + 1 < points.length) {
			t2 = new ForkJoinTask<Void>() {
				private static final long serialVersionUID = 2537705569519846490L;

				@Override
				public Void getRawResult() {
					return null;
				}

				@Override
				protected void setRawResult(Void value) {
				}

				@Override
				protected boolean exec() {
					Point[] ps = Arrays.copyOfRange(points, median+1, points.length);
					childB = new FjKDTree(pool, ps, depth + 1, threshold);
					return true;
				}
			};
			pool.submit(t2);
		}
	
		try {
			if (t1 != null) t1.get();
			if (t2 != null) t2.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private void createSubTreesInSeq(Point[] points, int depth, final int median) {
		// Create childA and childB recursively.
		if (median > 0) {
			Point[] ps = Arrays.copyOfRange(points, 0, median);
			childA = new FjKDTree(pool, ps, depth + 1, threshold);
		}
		if (median + 1 < points.length) {
			Point[] ps = Arrays.copyOfRange(points, median+1, points.length);
			childB = new FjKDTree(pool, ps, depth + 1, threshold);
		}
	}

	public Point findClosest(Point target) {
		Point closest = point.equals(target) ? Point.INFINITY : point;
		double bestDist = closest.distance(target);
		double spacing = target.coord[d] - point.coord[d];
		FjKDTree rightSide = (spacing < 0) ? childA : childB;
		FjKDTree otherSide = (spacing < 0) ? childB : childA;

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
	
	static class Finder extends RecursiveAction {
		private static final long serialVersionUID = -2276522791388601840L;
		
		FjKDTree tree;
		Point[] points;
		Point[] closest;
		int st;
		int end;
		int threshold;
		
		public Finder(FjKDTree tree, Point[] points, Point[] closests, int st, int end, int threshold) {
			this.tree = tree;
			this.points = points;
			this.closest = closests;
			this.st = st;
			this.end = end;
			this.threshold = threshold;
		}
		
		@Override
		protected void compute() {
			if (st == end) {
				closest[st] = tree.findClosest(points[st]);
			} else if (end - st < threshold) {
				for (int i = st; i < end; i++) {
					closest[i] = tree.findClosest(points[i]);
				}
			} else {
				int h = (end - st)/2 + st;
				Finder a = new Finder(tree, points, closest, st, h, threshold);
				Finder b = new Finder(tree, points, closest, h, end, threshold);
				invokeAll(a,b);
			}
			
		}
		
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = Point.DEFAULT_SIZE;
		if (be.args.length > 0)
			size = Integer.parseInt(be.args[0]);

		int threshold = Point.DEFAULT_THRESHOLD;
		if (be.args.length > 1)
			threshold = Integer.parseInt(be.args[1]);
		
		Point[] points = Point.generatePoints(size);
		Point[] closest = new Point[size];

		be.start();
		ForkJoinPool pool = new ForkJoinPool();
		FjKDTree tree = new FjKDTree(pool, points, 0, threshold);

		pool.invoke(new Finder(tree, points, closest, 0, points.length, threshold));

		Point markPoint = new Point(10, 100);
		Point closestP = tree.findClosest(markPoint);
		be.end();
		if (be.verbose) {
			System.out.println("Closest:" + closestP);
		}
	}
}
