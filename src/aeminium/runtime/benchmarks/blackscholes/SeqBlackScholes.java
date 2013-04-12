package aeminium.runtime.benchmarks.blackscholes;

import aeminium.runtime.benchmarks.helpers.Benchmark;


/*************************************************************************
 *  Compilation:  javac BlackScholes.java MyMath.java
 *  Execution:    java BlackScholes S X r sigma T
 *  
 *  Reads in five command line inputs and calculates the option price
 *  according to the Black-Scholes formula.
 *
 *  % java BlackScholes 23.75 15.00 0.01 0.35 0.5
 *  8.879159279691955                                  (actual =  9.10)
 *      
 *  % java BlackScholes 30.14 15.0 0.01 0.332 0.25
 *  15.177462481562186                                 (actual = 14.50)
 *
 *
 *  Information calculated based on closing data on Monday, June 9th 2003.
 *
 *      Microsoft:   share price:             23.75
 *                   strike price:            15.00
 *                   risk-free interest rate:  1%
 *                   volatility:              35%          (historical estimate)
 *                   time until expiration:    0.5 years
 *
 *       GE:         share price:             30.14
 *                   strike price:            15.00
 *                   risk-free interest rate   1%
 *                   volatility:              33.2%         (historical estimate)
 *                   time until expiration     0.25 years
 *
 *
 *  Reference:  http://www.hoadley.net/options/develtoolsvolcalc.htm
 *
 *************************************************************************/


public class SeqBlackScholes {

    // Black-Scholes formula
    public static double callPrice(double S, double X, double r, double sigma, double T) {
        double d1 = (Math.log(S/X) + (r + sigma * sigma/2) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);
        return S * Gaussian.Phi(d1) - X * Math.exp(-r * T) * Gaussian.Phi(d2);
    }

    // estimate by Monte Carlo simulation
    public static double call(double S, double X, double r, double sigma, double T, long N) {
        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            double eps = StdRandom.gaussian();
            double price = S * Math.exp(r*T - 0.5*sigma*sigma*T + sigma*eps*Math.sqrt(T));
            double value = Math.max(price - X, 0);
            sum += value;
        }
        double mean = sum / N;
     
        return Math.exp(-r*T) * mean;
    }

    // estimate by Monte Carlo simulation
    public static double call2(double S, double X, double r, double sigma, double T, long N) {
        double sum = 0.0;
        for (int i = 0; i < N; i++) {
            double price = S;
            double dt = T/10000.0;
            for (double t = 0; t <= T; t = t + dt) {
                price += r*price*dt +sigma*price*Math.sqrt(dt)*StdRandom.gaussian();
            }
            double value = Math.max(price - X, 0);
            sum += value;
        }
        double mean = sum / N;
     
        return Math.exp(-r*T) * mean;
    }



    public static void main(String[] args) {
    	Benchmark be = new Benchmark(args);
    	be.start();
        double S     = Double.parseDouble(be.args[0]);
        double X     = Double.parseDouble(be.args[1]);
        double r     = Double.parseDouble(be.args[2]);
        double sigma = Double.parseDouble(be.args[3]);
        double T     = Double.parseDouble(be.args[4]);
        long N 		 = Long.parseLong(be.args[5]);
        
        double cP = callPrice(S, X, r, sigma, T);
        double ca = call(S, X, r, sigma, T, N);
        double c2 = call2(S, X, r, sigma, T, N);
        be.end();
        if (be.verbose) {
        	System.out.println(cP);
        	System.out.println(ca);
        	System.out.println(c2);
        }
    }
}
