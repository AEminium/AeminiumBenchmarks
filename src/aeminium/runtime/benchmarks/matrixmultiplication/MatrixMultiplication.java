package aeminium.runtime.benchmarks.matrixmultiplication;

class MatrixMultiplication {
	public static void main(String args[]) {

		long initialTime = System.currentTimeMillis();

		int m, n, p, q, sum = 0, c, d, k;

		m = 480;
		n = 480;
		p = 480;
		q = 480;

		int first[][] = new int[m][n];

		// System.out.println("Enter the elements of first matrix");

		for (c = 0; c < m; c++)
			for (d = 0; d < n; d++)
				first[c][d] = d * c;
		// first[c][d] = in.nextInt();

		// System.out.println("Enter the number of rows and columns of second matrix");
		// p = in.nextInt();
		// q = in.nextInt();


		if (n != p)
			System.out.println("Matrices with entered orders can't be multiplied with each other.");
		else {
			int second[][] = new int[p][q];
			int multiply[][] = new int[m][q];

			// System.out.println("Enter the elements of second matrix");

			for (c = 0; c < p; c++)
				for (d = 0; d < q; d++)
					second[c][d] = d * c;
			// second[c][d] = in.nextInt();

			for (c = 0; c < m; c++) {
				for (d = 0; d < q; d++) {
					for (k = 0; k < p; k++) {
						sum = sum + first[c][k] * second[k][d];
					}

					multiply[c][d] = sum;
					sum = 0;
				}
			}

			long finalTime = System.currentTimeMillis();
			System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);

			/*
			System.out.println("Product of entered matrices:-");

			for (c = 0; c < m; c++) {
				for (d = 0; d < q; d++)
					System.out.print(multiply[c][d] + "\t");

				System.out.print("\n");
			}
			 */
		}
	}
}