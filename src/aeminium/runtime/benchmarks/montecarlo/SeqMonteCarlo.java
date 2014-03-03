package aeminium.runtime.benchmarks.montecarlo;

import java.util.Vector;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqMonteCarlo extends MonteCarlo {

	public SeqMonteCarlo(String dataFilename, int nTimeStepsMC, int nRunsMC) {
		this.dataFilename = dataFilename;
		this.nTimeStepsMC = nTimeStepsMC;
		this.nRunsMC = nRunsMC;
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
	
	public void run() {
		results = new Vector<ToResult>(nRunsMC);
		
		PriceStock ps;
		for (int iRun = 0; iRun < nRunsMC; iRun++) {
			ps = new PriceStock();
			ps.setInitAllTasks(SeqMonteCarlo.initAllTasks);
			ps.setTask(SeqMonteCarlo.tasks.elementAt(iRun));
			ps.run();
			SeqMonteCarlo.results.addElement(ps.getResult());
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

		SeqMonteCarlo mc = new SeqMonteCarlo(fname, size1, size2);
		mc.initSerial();
		while (!be.stop()) {
			be.start();
			mc.run();
			try {
				mc.processResults(be.verbose);
			} catch (DemoException e) {
				e.printStackTrace();
			}
			be.end();		
		}
	}

}
