package aeminium.runtime.benchmarks.integrate;

public class Integrate {
	static final double errorTolerance = 1.0e-12;
	static final int threshold = 100;
	static double start = -2101.0;
    static double end = 1036.0;
    
	// the function to integrate
    static double computeFunction(double x)  {
        return (x * x + 1.0) * x;
    }
}
