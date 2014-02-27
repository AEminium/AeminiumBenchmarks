package aeminium.runtime.benchmarks.montecarlo;

import java.util.Vector;

public class MonteCarlo {
	public static final int SIZE_1 = 1000;
	public static final int SIZE_2 = 60000;
	public static final int DEFAULT_THRESHOLD = 10;

	protected String dataFilename;

	protected int nTimeStepsMC = 0;
	protected int nRunsMC = 0;
	protected double dTime = 1.0 / 365.0;

	public static double JGFavgExpectedReturnRateMC = 0.0;

	PriceStock psMC;
	double pathStartValue = 100.0;
	double avgExpectedReturnRateMC = 0.0;
	double avgVolatilityMC = 0.0;

	public static ToInitAllTasks initAllTasks = null;

	public static Vector<ToTask> tasks;
	public static Vector<ToResult> results;

	protected void initTasks(int nRunsMC) {
		tasks = new Vector<ToTask>(nRunsMC);
		for (int i = 0; i < nRunsMC; i++) {
			String header = "MC run " + String.valueOf(i);
			ToTask task = new ToTask(header, (long) i * 11);
			tasks.addElement(task);
		}
	}

	public void initSerial() {
		try {
			RatePath rateP = new RatePath(dataFilename);
			rateP.dbgDumpFields();
			ReturnPath returnP = rateP.getReturnCompounded();
			returnP.estimatePath();
			returnP.dbgDumpFields();

			initAllTasks = new ToInitAllTasks(returnP, nTimeStepsMC, pathStartValue);
			initTasks(nRunsMC);
		} catch (DemoException demoEx) {
			System.out.println(demoEx.toString());
			System.exit(-1);
		}
	}
}
