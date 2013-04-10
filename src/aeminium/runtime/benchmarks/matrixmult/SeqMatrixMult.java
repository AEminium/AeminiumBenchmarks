package aeminium.runtime.benchmarks.matrixmult;

class SeqMatrixMult {
	
	static int first[][];
	static int second[][];
	static int result[][];
	
	public static int[][] createMatrix(int m, int n) {
		int[][] t = new int[m][n];
		for (int c=0;c<m;c++)
			for (int d=0;d<n;d++)
				t[c][d] = d * c;
		return t;
	}
	
	public static void main(String args[]) {
		long initialTime = System.currentTimeMillis();
		
		int m = 1000;
		if (args.length > 0) m = Integer.parseInt(args[0]);
		int n = 1000;
		if (args.length > 1) n = Integer.parseInt(args[1]);
		int p = n;
		int q = 1000;
		if (args.length > 2) q = Integer.parseInt(args[2]);
		
		first = createMatrix(m,n);
		second = createMatrix(p,q);
		result = new int[m][q];

		for (int c = 0; c < m; c++) {
			for (int d = 0; d < q; d++) {
				int sum = 0;
				for (int k = 0; k < p; k++) {
					sum += first[c][k] * second[k][d];
				}
				result[c][d] = sum;
			}
		}

		if (args.length > 3) {
			System.out.println("Product of entered matrices:-");
			for (int c = 0; c < m; c++) {
				for (int d = 0; d < q; d++)
					System.out.print(result[c][d] + "\t");
				System.out.print("\n");
			}
		}
		
		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}
}