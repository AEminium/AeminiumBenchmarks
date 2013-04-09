package aeminium.runtime.benchmarks.integrate;


public class SeqIntegrate {
	
	static final double errorTolerance = 1.0e-12;
	static final int threshold = 100;
	static double start = -2101.0;
    static double end = 1036.0;
    static final long NPS = (1000L * 1000 * 1000);
	
    public static void main(String[] args) throws Exception {
        
        long tstart = System.nanoTime();
		double a = AeIntegrate.recEval(start, end, AeIntegrate.computeFunction(start), AeIntegrate.computeFunction(end), 0);
		System.out.println("Integral: " + a);
		long tend = System.nanoTime();
		System.out.println((double)(tend - tstart)/NPS);
    }
}
