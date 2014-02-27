package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqGA {

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		if (be.args.length > 0) Knapsack.popSize = Integer.parseInt(be.args[0]);
		if (be.args.length > 1) Knapsack.numGen = Integer.parseInt(be.args[1]);

		Indiv[] pop = new Indiv[Knapsack.popSize];
		Indiv[] next = new Indiv[Knapsack.popSize];

		while (!be.stop()) {
			Knapsack.resetSeed();
			be.start();
			// Initialize Population Randomly
			for (int i = 0; i < Knapsack.popSize; i++) {
				pop[i] = Knapsack.createRandomIndiv();
			}

			// Main loop
			for (int g = 0; g < Knapsack.numGen; g++) {
				// Sort by fitness
				for (int i = 0; i < Knapsack.popSize; i++) {
					Knapsack.evaluate(pop[i]);
				}
				Arrays.sort(pop);
				if (be.verbose) {
					System.out.println("Best fit at " + g + ": " + pop[0].fitness);
				}

				// Elitism
				for (int i = 0; i < Knapsack.elitism; i++) {
					next[Knapsack.popSize - i - 1] = pop[i];
				}

				// Recombine
				for (int i = 0; i < Knapsack.popSize - Knapsack.elitism; i++) {
					Indiv other = (i < Knapsack.bestLimit) ? pop[i + 1] : pop[i - Knapsack.bestLimit];
					next[i] = Knapsack.recombine(pop[i], other);
				}

				// Mutation
				for (int i = 0; i < Knapsack.popSize - Knapsack.elitism; i++) {
					Knapsack.mutate(next[i]);
				}

				Indiv[] tmp = pop;
				pop = next;
				next = tmp;
			}
			be.end();
		}
	}
}
