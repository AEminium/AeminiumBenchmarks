package aeminium.runtime.benchmarks.jacobi;

public class Jacobi {
	public final static double EPSILON = 0.0001;
	public final static int DEFAULT_SIZE = 1024;
	public final static int DEFAULT_STEPS = 100;
	public final static int DEFAULT_GRANULARITY = 2;
	
	public static void setup(int n, double[][] a, double[][] b) {
		int dim = n+2;
		double smallVal = Jacobi.EPSILON; // 1.0/dim;
		for (int i = 1; i < dim-1; ++i) {
			for (int j = 1; j < dim-1; ++j)
				a[i][j] = smallVal;
		}
		for (int k = 0; k < dim; ++k) {
			a[k][0] = 1.0;
			a[k][n+1] = 1.0;
			a[0][k] = 1.0;
			a[n+1][k] = 1.0;
			b[k][0] = 1.0;
			b[k][n+1] = 1.0;
			b[0][k] = 1.0;
			b[n+1][k] = 1.0;
		}

	}
}
