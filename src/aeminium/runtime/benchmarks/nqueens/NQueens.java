package aeminium.runtime.benchmarks.nqueens;

public class NQueens {
	public final static int DEFAULT_MIN_SIZE = 8;
	public final static int DEFAULT_MAX_SIZE = 15;
	public static final int DEFAULT_THRESHOLD = 8;
	
	static final int[] expectedSolutions = new int[] {
        0, 1, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200,
        73712, 365596, 2279184, 14772512, 95815104, 666090624
    }; // see http://www.durangobill.com/N_Queens.html
	
}
