package aeminium.runtime.benchmarks.lcs;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqLCS {
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		
		String s1 = LCS.s1;
		String s2 = LCS.s1;
		if (args.length > 0) {
			s1 = LCS.readFile(args[0]);
		}
		if (args.length > 1) {
			s2 = LCS.readFile(args[1]);
		}
		be.start();
		String common = seqCompute(s1, s2);
		be.end();
		if (be.verbose) {
			System.out.println(common);
		}
	}
	
	public static String seqCompute(String x, String y) {
		// adapted from http://www.cs.princeton.edu/introcs/96optimization/LCS.java.html
		
		int M = x.length();
        int N = y.length();

        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
        int[][] opt = new int[M+1][N+1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = M-1; i >= 0; i--) {
            for (int j = N-1; j >= 0; j--) {
                if (x.charAt(i) == y.charAt(j))
                    opt[i][j] = opt[i+1][j+1] + 1;
                else 
                    opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
            }
        }

        
        StringBuilder sol = new StringBuilder();
        // recover LCS itself and print it to standard output
        int i = 0, j = 0;
        while(i < M && j < N) {
            if (x.charAt(i) == y.charAt(j)) {
            	sol.append(x.charAt(i));
                i++;
                j++;
            }
            else if (opt[i+1][j] >= opt[i][j+1]) i++;
            else                                 j++;
        }
        return sol.toString();
	}
}
