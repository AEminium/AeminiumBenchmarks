package aeminium.runtime.benchmarks.lud;

import java.util.Random;

class Matrix  {
	private double[][] A;

	private int m;

	private int n;

	public Matrix(int m, int n) {
		this.m = m;
		this.n = n;
		A = new double[m][n];
	}

	public double[][] getArray() {
		return A;
	}

	public double[][] getArrayCopy() {
		double[][] C = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j]=A[i][j];
			}
		}
		return C;
	}

	public int getRowDimension() {
		return m;
	}

	public int getColumnDimension() {
		return n;
	}

	public double get(int i, int j) {
		return A[i][j];
	}

	public static Matrix random(int m, int n, Random r) {
		Matrix A = new Matrix(m, n);
		double[][] X = A.getArray();
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				X[i][j] = r.nextDouble();
			}
		}
		return A;
	}
}
