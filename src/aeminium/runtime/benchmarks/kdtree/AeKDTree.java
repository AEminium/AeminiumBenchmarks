package aeminium.runtime.benchmarks.kdtree;

import java.util.Arrays;
import java.util.Comparator;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeKDTree {
	
	static Point result;
	static AeKDTree root;
	
	// 2D k-d tree
	public AeKDTree childA, childB;
	private Point point; // defines the boundary
	private int d; // dimension: 0 => left/right split, 1 => up/down split

	
	public AeKDTree(Point[] points, int depth) {
		childA = null;
		childB = null;
		d = depth % 2;
		
		// find median by sorting in dimension 'd' (either x or y)
		Comparator<Point> comp = new Point.PointComp(d);
		Arrays.sort(points, comp);
		int median = (points.length - 1) / 2;
		point = points[median];
	}
	
	public void createSubTrees(final Point[] points, final int depth, Runtime rt, Task parent) {
		int median = (points.length - 1) / 2;
		if (Benchmark.useThreshold ? depth <= Point.DEFAULT_THRESHOLD : !rt.parallelize(parent))
			createSubTreesInPar(points, depth, rt, parent, median);
		else
			createSubTreesInSeq(points, depth, rt, parent, median);
	}

	private void createSubTreesInSeq(Point[] points, int depth, Runtime rt, Task parent, final int median) {
		// Create childA and childB recursively.
		if (median > 0) {
			Point[] ps = Arrays.copyOfRange(points, 0, median);
			childA = new AeKDTree(ps, depth + 1);
			childA.createSubTrees(ps,  depth + 1, rt, parent);
		}
		if (median + 1 < points.length) {
			Point[] ps = Arrays.copyOfRange(points, median+1, points.length);
			childB = new AeKDTree(ps, depth + 1);
			childB.createSubTrees(ps,  depth + 1, rt, parent);
		}
	}

	protected void createSubTreesInPar(final Point[] points, final int depth,
			Runtime rt, Task parent, final int median) {
		// Create childA and childB recursively.
		if (median > 0) {
			Task tA = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					Point[] ps = Arrays.copyOfRange(points, 0, median);
					childA = new AeKDTree(ps, depth + 1);
					childA.createSubTrees(ps,  depth + 1, rt, current);
				}
			}, (short)(Hints.RECURSION));
			rt.schedule(tA, parent, Runtime.NO_DEPS);
		}
		if (median + 1 < points.length) {
			Task tB = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					Point[] ps = Arrays.copyOfRange(points, median+1, points.length);
					childB = new AeKDTree(ps, depth + 1);
					childB.createSubTrees(ps,  depth + 1, rt, current);
				}
			}, (short)(Hints.RECURSION));
			rt.schedule(tB, parent, Runtime.NO_DEPS);
		}
	}
	
	
	public Point findClosest(Point target) {
		Point closest = point.equals(target) ? Point.INFINITY : point;
		double bestDist = closest.distance(target);
		double spacing = target.coord[d] - point.coord[d];
		AeKDTree rightSide = (spacing < 0) ? childA : childB;
		AeKDTree otherSide = (spacing < 0) ? childB : childA;

		if (rightSide != null) {
			Point candidate = rightSide.findClosest(target); // TODO
			if (candidate.distance(target) < bestDist) {
				closest = candidate;
				bestDist = closest.distance(target);
			}
		}

		if (otherSide != null && (Math.abs(spacing) < bestDist)) {
			Point candidate = otherSide.findClosest(target); // TODO
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
		if (be.args.length > 0)
			size = Integer.parseInt(be.args[0]);
		
		final Point[] points = Point.generatePoints(size);
		final Point[] closest = new Point[size];
		
		be.start();
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		rt.init();
		
		Task createTree = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				root = new AeKDTree(points, 0);
				root.createSubTrees(points, 0, rt, current);
			}
		}, (short)(Hints.RECURSION));
		rt.schedule(createTree, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		Task findClosest = ForTask.createFor(rt, new Range(points.length), new ForBody<Integer>(){
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				closest[i] = root.findClosest(points[i]);
			}
		}, Hints.LARGE);
		rt.schedule(findClosest, Runtime.NO_PARENT, Arrays.asList(createTree));
		
		
		Task findPoint = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Point markPoint = new Point(10, 100);
				result = root.findClosest(markPoint);
			}
		}, (short)(Hints.LARGE | Hints.NO_CHILDREN));
		rt.schedule(findPoint, Runtime.NO_PARENT, Arrays.asList(findClosest));
		
		rt.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Closest:" + result);
		}
		
	}
}
