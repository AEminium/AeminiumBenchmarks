package aeminium.runtime.benchmarks.helpers;

public class Stats {
	public static double doubleCoV(double[] numbers){
        double cov = 0.0;
        double stdDev = 0.0;
        double mean = Stats.doubleMean(numbers);
        for (Double value: numbers){ //for each locus in loci
            stdDev = Math.pow((value - mean), 2); //(value - mean) squared
        }
        stdDev = Math.sqrt(stdDev / (numbers.length - 1));
        
        if (mean == 0.0) {
            cov = Double.NaN; //return a non-number (NotANumber)
        } else {
            cov = stdDev / mean;
        }
        return cov;
        
    }
    
    public static double doubleMean(double[] numbers){
        //determine arithmetic mean for an array of double-precision floating-
        //point values.
        double mean = 0.0;
        for (Double value: numbers){ //for each value in numbers
            mean += value;
        }
        mean /= numbers.length;
        return mean;
    }
}
