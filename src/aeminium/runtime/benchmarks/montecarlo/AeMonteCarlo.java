package aeminium.runtime.benchmarks.montecarlo;

import java.util.Arrays;
import java.util.Vector;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeMonteCarlo extends MonteCarlo {

	protected int threshold;
	protected Task tmain;

	public AeMonteCarlo(String dataFilename, int nTimeStepsMC, int nRunsMC, int threshold) {
		this.dataFilename = dataFilename;
		this.nTimeStepsMC = nTimeStepsMC;
		this.nRunsMC = nRunsMC;
		this.threshold = threshold;
	}

	public void processResults(final boolean verbose, Runtime rt) throws DemoException {
		Task merge = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				double avgExpectedReturnRateMC = 0.0;
				ToResult returnMC;
				assert (nRunsMC == results.size());
				// Create an instance of a RatePath, for accumulating the
				// results of the Monte Carlo simulations.
				RatePath avgMCrate = new RatePath(nTimeStepsMC, "MC", 19990109, 19991231, dTime);
				for (int i = 0; i < nRunsMC; i++) {
					// First, create an instance which is supposed to generate a
					// particularly simple MC path.
					returnMC = AeMonteCarlo.results.elementAt(i);
					avgMCrate.inc_pathValue(returnMC.get_pathValue());
					avgExpectedReturnRateMC += returnMC.get_expectedReturnRate();
					avgVolatilityMC += returnMC.get_volatility();
				} // for i;
				avgMCrate.inc_pathValue((double) 1.0 / ((double) nRunsMC));
				avgExpectedReturnRateMC /= nRunsMC;
				avgVolatilityMC /= nRunsMC;

				JGFavgExpectedReturnRateMC = avgExpectedReturnRateMC;
				if (verbose) {
					System.out.println("Average over " + nRunsMC + ": expectedReturnRate=" + avgExpectedReturnRateMC + " volatility=" + avgVolatilityMC
							+ JGFavgExpectedReturnRateMC);
				}
			}
		}, Hints.LOOPS);
		rt.schedule(merge, Runtime.NO_PARENT, Arrays.asList(tmain));
	}

	public void run(Runtime rt) {
		AeMonteCarlo.results = new Vector<ToResult>(nRunsMC);
		tmain = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Task eval = ForTask.createFor(rt, new Range(nRunsMC), new ForBody<Integer>() {
					@Override
					public void iterate(Integer i, Runtime rt, Task current) {
						PriceStock ps = new PriceStock();
						ps.setInitAllTasks(AeMonteCarlo.initAllTasks);
						ps.setTask(AeMonteCarlo.tasks.elementAt(i));
						ps.run();
						AeMonteCarlo.results.addElement(ps.getResult());
					}
				}, (short) (Hints.NO_CHILDREN | Hints.LARGE));
				rt.schedule(eval, current, Runtime.NO_DEPS);
			}
		}, Hints.LOOPS);
		rt.schedule(tmain, Runtime.NO_PARENT, Runtime.NO_DEPS);

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
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		AeMonteCarlo mc = new AeMonteCarlo(fname, size1, size2, threshold);
		mc.initSerial();
		while (!be.stop()) {
			rt.init();
			be.start();
			mc.run(rt);
			try {
				mc.processResults(be.verbose, rt);
			} catch (DemoException e) {
				e.printStackTrace();
			}
			rt.shutdown();
			be.end();
		}
	}
}
