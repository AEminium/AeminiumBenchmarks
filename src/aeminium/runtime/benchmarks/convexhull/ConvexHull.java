package aeminium.runtime.benchmarks.convexhull;

import java.util.ArrayList;
import java.util.Random;

public class ConvexHull {
	public static int DEFAULT_SIZE = 10 * 1000 * 1000;
	
	
	
	public static ArrayList<Point> generateData(int size, Random r) {
		ArrayList<Point> ret = new ArrayList<>(size);
		for (int i =0; i< size; i++) {
			ret.add(new Point(r.nextDouble(), r.nextDouble()));
		}
		return ret;
	}
	
	  /*
	   * Computes the square of the distance of point C to the segment defined by points AB
	   */
	  public static double distance(Point A, Point B, Point C) {
	    double ABx = B.x-A.x;
	    double ABy = B.y-A.y;
	    double num = ABx*(A.y-C.y)-ABy*(A.x-C.x);
	    if (num < 0) num = -num;
	    return num;
	  }
	  
	  
	  public static double pointLocation(Point A, Point B, Point P) {
		    double cp1 = (B.x-A.x)*(P.y-A.y) - (B.y-A.y)*(P.x-A.x);
		    return (cp1>0)?1:-1;
		  }
	
}
