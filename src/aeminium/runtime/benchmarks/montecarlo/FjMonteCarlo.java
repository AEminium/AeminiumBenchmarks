package aeminium.runtime.benchmarks.montecarlo;

import java.util.Vector;
import jsr166e.ForkJoinPool;
import jsr166e.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjMonteCarlo extends MonteCarlo {
	
	protected int threshold;
	
	public FjMonteCarlo(String dataFilename, int nTimeStepsMC, int nRunsMC, int threshold) {
		this.dataFilename = dataFilename;
		this.nTimeStepsMC = nTimeStepsMC;
		this.nRunsMC = nRunsMC;
		this.threshold = threshold;
	}

	public void processResults(boolean verbose) throws DemoException {
		double avgExpectedReturnRateMC = 0.0;
		ToResult returnMC;
		assert(nRunsMC == results.size());
		// Create an instance of a RatePath, for accumulating the results of the Monte Carlo simulations.
		RatePath avgMCrate = new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
		for (int i = 0; i < nRunsMC; i++) {
			// First, create an instance which is supposed to generate a
			// particularly simple MC path.
			returnMC = (ToResult) results.elementAt(i);
			avgMCrate.inc_pathValue(returnMC.get_pathValue());
			avgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
			avgVolatilityMC += returnMC.get_volatility();
		} // for i;
		avgMCrate.inc_pathValue((double) 1.0 / ((double) nRunsMC));
		avgExpectedReturnRateMC /= nRunsMC;
		avgVolatilityMC /= nRunsMC;

		JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
		if (verbose) {
			System.out.println("Average over "+nRunsMC+": expectedReturnRate="+
					avgExpectedReturnRateMC+" volatility="+avgVolatilityMC +
					JGFavgExpectedReturnRateMC);
		}
	}
	
	public void run(ForkJoinPool pool) {
		results = new Vector<ToResult>(nRunsMC);
		ForkCarlo fc = new ForkCarlo(0, nRunsMC, threshold);
		pool.invoke(fc);
	}
	
	@SuppressWarnings("serial")
	public class ForkCarlo extends RecursiveAction {
		int bottom, top, threshold;
		public ForkCarlo(int bottom, int top, int thr) {
			this.bottom = bottom;
			this.top = top;
			this.threshold = thr;
		}
		
		@Override
		protected void compute() {
			if (top - bottom < threshold || top-bottom < 2) {
				seq();
			} else {
				int mid = (top-bottom)/2 + bottom; 
				ForkCarlo f1 = new ForkCarlo(bottom, mid, threshold);
				ForkCarlo f2 = new ForkCarlo(mid, top, threshold);
				invokeAll(f1, f2);
			}
		}
		
		protected void seq() {
			for (int i = bottom; i < top; i++) {
				PriceStock ps = new PriceStock();
				ps.setInitAllTasks(FjMonteCarlo.initAllTasks);
				ps.setTask(FjMonteCarlo.tasks.elementAt(i));
				ps.run();
				FjMonteCarlo.results.addElement(ps.getResult());
			}
		}
		
	}

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		
		String fname = be.args[0];
		
		int size1 = MonteCarlo.SIZE_1;
		if (be.args.length > 1) {
			size1 = Integer.parseInt(be.args[1]);
		}
		int size2 = MonteCarlo.SIZE_2;
		if (be.args.length > 2) {
			size2 = Integer.parseInt(be.args[2]);
		}
		int threshold = MonteCarlo.DEFAULT_THRESHOLD;
		if (be.args.length > 3) {
			threshold = Integer.parseInt(be.args[3]);
		}
		ForkJoinPool pool = new ForkJoinPool();
		FjMonteCarlo mc = new FjMonteCarlo(fname, size1, size2, threshold);
		mc.initSerial();
		while (!be.stop()) {
			be.start();
			mc.run(pool);
			try {
				mc.processResults(be.verbose);
			} catch (DemoException e) {
				e.printStackTrace();
			}
			be.end();		
		}
	}
}
