package aeminium.runtime.benchmarks.moldyn;

public class MolDyn {
	public static final int DEFAULT_SIZE = 13;
	public static final int DEFAULT_THRESHOLD = 1000;
	public static final int DEFAULT_ITERATIONS = 100;
	public static final double LENGTH = 50e-10;
	public static final double m = 4.0026;
	public static final double mu = 1.66056e-27;
	public static final double kb = 1.38066e-23;
	public static final double TSIM = 50;
	public static final double deltat = 5e-16;

	public static int PARTSIZE;

	public particle one[] = null;
	public double epot = 0.0;
	public double vir = 0.0;
	public double count = 0.0;

	int iterations;
	int size, mm;

	public int interactions = 0;
	public int[] interacts;

}
