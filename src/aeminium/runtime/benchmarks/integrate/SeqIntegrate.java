package aeminium.runtime.benchmarks.integrate;

import aeminium.runtime.benchmarks.helpers.Benchmark;


public class SeqIntegrate {
	
    public static void main(String[] args) throws Exception {
        Benchmark be = new Benchmark(args);
        
	    if (be.args.length > 0) {
	    	double exp = Double.parseDouble(be.args[0]);
	    	Integrate.errorTolerance = Math.pow(10, -exp);
	    }
        
        be.start();
		double a = recEval(Integrate.start, Integrate.end, Integrate.computeFunction(Integrate.start), Integrate.computeFunction(Integrate.end), 0);
		be.end();
		if (be.verbose) {
			System.out.println("Integral: " + a);
		}
    }
    
    static final double recEval(double l, double r, double fl, double fr,
			double a) {
		double h = (r - l) * 0.5;
		double c = l + h;
		double fc = (c * c + 1.0) * c;
		double hh = h * 0.5;
		double al = (fl + fc) * hh;
		double ar = (fr + fc) * hh;
		double alr = al + ar;
		if (Math.abs(alr - a) <= Integrate.errorTolerance)
			return alr;
		else
			return recEval(c, r, fc, fr, ar) + recEval(l, c, fl, fc, al);
	}
}
