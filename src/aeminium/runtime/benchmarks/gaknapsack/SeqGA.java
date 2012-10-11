package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;

public class SeqGA {
	public static void main(String[] args) {
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
			System.out.println("Best fit at " + g + ": " + pop[0].fitness);
			
			// Elitism
			for (int i=0; i < Knapsack.elitism; i++ ) {
				next[Knapsack.popSize - i - 1] = pop[i];
			}
			
			// Recombine
			for (int i=0; i < Knapsack.popSize - Knapsack.elitism; i++ ) {
				Indiv other = (i < 10) ? pop[i+1] : pop[i-10];
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
