package aeminium.runtime.benchmarks.gaknapsack;

import external.MersenneTwisterFast;

// Describes the KnapSack Problem
public class Knapsack {
	public static int SIZE_LIMIT = 87;
	public final static int numberOfItems = 500;
	
	public final static MersenneTwisterFast rand = new MersenneTwisterFast(1L);
	public final static Item[] items = Knapsack.createItems(numberOfItems);
	
	// GA Settings:
	public final static int popSize = 10;
	public final static int cromSize = numberOfItems;
	public final static int numGen = 3;
	public final static double prob_mut = 0.2;
	public final static double prob_rec = 0.2;
	public static final int elitism = 2;
	
	
	private static Item[] createItems(int n) {
		Item[] tmp = new Item[n];
		for (int i = 0; i < n; i++) {
			tmp[i] = new Item("obj" + i, rand.nextInt(100), rand.nextInt(100));
		}
		return tmp;
	}


	public static Indiv createRandomIndiv() {
		Indiv ind = new Indiv(cromSize);
		boolean hasSth = false;
		for (int i=0; i<cromSize; i++) {
			boolean b = ( Knapsack.rand.nextDouble() < 0.01 );
			ind.set(i, b);
			hasSth = hasSth || b;
		}
		if (!hasSth) { // Enforce at least 1 item in backpack;
			ind.set(rand.nextInt(cromSize), true);
		}
		return ind;
	}


	public static Indiv recombine(Indiv p1, Indiv p2) {
		if (rand.nextFloat() > Knapsack.prob_rec) return p1;
		Indiv ind = new Indiv(cromSize);
		int cutpoint = rand.nextInt(cromSize);
		for (int i=0; i<cromSize; i++) {
			if (i < cutpoint) ind.set(i, p1.has[i]);
			else ind.set(i, p2.has[i]);
		}
		return ind;
	}
	
	public static void evaluate(Indiv indiv) {
		int[] ph = phenotype(indiv);
		int value = ph[0];
		int weight = ph[1];

		// Evaluation
		if (weight >= Knapsack.SIZE_LIMIT || value == 0) {
			indiv.fitness= 2.0;
		} else {
			indiv.fitness= 1.0/(value); // Minimization problem.
		}
	}

	public static int[] phenotype(Indiv indiv) {
		int value = 0;
		int weight = 0;
		for (int i=0; i< indiv.size; i++) {
			if (indiv.has[i]) {
				value += Knapsack.items[i].value;
				weight += Knapsack.items[i].weight;
			}
		}
		return new int[] {value, weight};
	}


	public static void mutate(Indiv indiv) {
		if (rand.nextFloat() < Knapsack.prob_mut) {
			int p = rand.nextInt(cromSize);
			indiv.set(p, !indiv.has[p]);
		}
	}
	
}
