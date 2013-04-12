package aeminium.runtime.benchmarks.matrixmult;

public class Matrix {
	public static int DEFAULT_M = 1000;
	public static int DEFAULT_N = 1000;
	public static int DEFAULT_Q = 1000;
	
	public static int[][] createMatrix(int m, int n) {
		int[][] t = new int[m][n];
		for (int c=0;c<m;c++)
			for (int d=0;d<n;d++)
				t[c][d] = d * c;
		return t;
	}
	
	
}
