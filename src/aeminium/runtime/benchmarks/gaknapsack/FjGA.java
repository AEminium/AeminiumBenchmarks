package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjGA {
	
	static abstract class Action {
		abstract public void lambda(Indiv[] pop, Indiv[] next, int i);
	}
	
	static class Applier extends RecursiveAction {
		private static final long serialVersionUID = -2276522791388601840L;
		
		Indiv[] pop;
		Indiv[] next;
		int st;
		int end;
		int threshold;
		Action action;
		
		public Applier(Indiv[] pop, Indiv[] next, int st, int end, Action a, int threshold) {
			this.pop = pop;
			this.next = next;
			this.st = st;
			this.end = end;
			this.action = a;
			this.threshold = threshold;
		}
		
		@Override
		protected void compute() {
			if (st == end) {
				action.lambda(pop, next, st);
			} else if (end - st < threshold) {
				for (int i = st; i < end; i++) {
					action.lambda(pop, next, i);
				}
			} else {
				int h = (end - st)/2 + st;
				Applier a = new Applier(pop, next, st, h, action, threshold);
				Applier b = new Applier(pop, next, h, end, action, threshold);
				invokeAll(a,b);
			}
			
		}
		
	}
	
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		if (be.args.length > 0)
			Knapsack.popSize = Integer.parseInt(be.args[0]);
		if (be.args.length > 1)
			Knapsack.numGen = Integer.parseInt(be.args[1]);
		int threshold = Knapsack.DEFAULT_THRESHOLD;
		if (be.args.length > 2)
			threshold = Integer.parseInt(be.args[2]);
		
		Indiv[] pop = new Indiv[Knapsack.popSize];
		Indiv[] next = new Indiv[Knapsack.popSize];
		
		ForkJoinPool pool = new ForkJoinPool();
		
		while (!be.stop()) {
			Knapsack.resetSeed();
			be.start();
			
			// Initialize Population Randomly
			pool.invoke(new Applier(pop, next, 0, Knapsack.popSize, new Action() {
				@Override
				public void lambda(Indiv[] pop, Indiv[] next, int i) {
					pop[i] = Knapsack.createRandomIndiv();
				}
			},threshold));
	
			// Main loop
			for (int g=0; g<Knapsack.numGen; g++) {
				// Sort by fitness
				pool.invoke(new Applier(pop, next, 0, Knapsack.popSize, new Action() {
					@Override
					public void lambda(Indiv[] pop, Indiv[] next, int i) {
						Knapsack.evaluate(pop[i]);
					}
				},threshold));
				Arrays.sort(pop);
				if (be.verbose) {
					System.out.println("Best fit at " + g + ": " + pop[0].fitness);
				}
				
				// Elitism
				pool.invoke(new Applier(pop, next, 0, Knapsack.elitism, new Action() {
					@Override
					public void lambda(Indiv[] pop, Indiv[] next, int i) {
						next[Knapsack.popSize - i - 1] = pop[i];
					}
				},threshold));
				
				
				// Recombine
				pool.invoke(new Applier(pop, next, 0, Knapsack.popSize - Knapsack.elitism, new Action() {
					@Override
					public void lambda(Indiv[] pop, Indiv[] next, int i) {
						Indiv other = (i < Knapsack.bestLimit) ? pop[i+1] : pop[i-Knapsack.bestLimit];
						next[i] = Knapsack.recombine(pop[i], other);
					}
				},threshold));
				
				// Mutation
				pool.invoke(new Applier(pop, next, 0, Knapsack.popSize - Knapsack.elitism, new Action() {
					@Override
					public void lambda(Indiv[] pop, Indiv[] next, int i) {
						Knapsack.mutate(next[i]);
					}
				},threshold));
				
				Indiv[] tmp = pop;
				pop = next;
				next = tmp;
			}
			be.end();
		}
	}
}
