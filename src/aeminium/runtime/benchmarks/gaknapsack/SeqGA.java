package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;

public class SeqGA {
	
	public static boolean debug = false;
	
	public static void main(String[] args) {
		if (args.length >= 1)
			Knapsack.popSize = Integer.parseInt(args[0]);
		if (args.length >= 2)
			Knapsack.numGen = Integer.parseInt(args[1]);
		
		Indiv[] pop = new Indiv[Knapsack.popSize];
		Indiv[] next = new Indiv[Knapsack.popSize];
		
		// Initialize Population Randomly
		for (int i=0; i < Knapsack.popSize; i++ ) {
			pop[i] = Knapsack.createRandomIndiv();
		}

		// Main loop
		for (int g=0; g<Knapsack.numGen; g++) {
			// Sort by fitness
			for (int i=0; i < Knapsack.popSize; i++ ) {
				Knapsack.evaluate(pop[i]);
			}
			Arrays.sort(pop);
			if (debug || g == Knapsack.numGen-1) {
				System.out.println("Best fit at " + g + ": " + pop[0].fitness);
			}
			
			// Elitism
			for (int i=0; i < Knapsack.elitism; i++ ) {
				next[Knapsack.popSize - i - 1] = pop[i];
			}
			
			// Recombine
			for (int i=0; i < Knapsack.popSize - Knapsack.elitism; i++ ) {
				Indiv other = (i < Knapsack.bestLimit) ? pop[i+1] : pop[i-Knapsack.bestLimit];
				next[i] = Knapsack.recombine(pop[i], other);
			}
			
			// Mutation
			for (int i=0; i < Knapsack.popSize - Knapsack.elitism; i++ ) {
				Knapsack.mutate(next[i]);
			}
			
			Indiv[] tmp = pop;
			pop = next;
			next = tmp;
		}
	}
}
