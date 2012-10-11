package aeminium.runtime.benchmarks.gaknapsack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

public class AeGA {
	
	static Runtime rt = Factory.getRuntime();
	static Indiv[] pop = new Indiv[Knapsack.popSize];
	static Indiv[] next = new Indiv[Knapsack.popSize];
	
	
	public static void main(String[] args) {
		rt.init();
		List<Task> round = new ArrayList<Task>();
		for (int i=0; i < Knapsack.popSize; i++ ) {
			Task init = createRandomIndiv(i);
			round.add(init);
		}
		for (int g=0; g<Knapsack.numGen; g++) {
			round = makeRound(g, round);
		}
		rt.shutdown();
	
	}

	private static List<Task> makeRound(final int g, List<Task> round) {
		List<Task> nround = new ArrayList<Task>();
		Task turn = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				performRound(pop, next, g, current);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(turn, Runtime.NO_PARENT, round);
		nround.add(turn);
		return nround;
		
	}

	protected static void performRound(final Indiv[] pop, final Indiv[] next, final int g, Task current) {
		List<Task> prev = new ArrayList<Task>();
		for (int i=0; i < Knapsack.popSize; i++ ) {
			prev.add(evaluateIndiv(i, current));
		}
		Task sort = sortArray(g, current, prev);
		List<Task> sorts = new ArrayList<Task>();
		sorts.add(sort);
		
		List<Task> recs = new ArrayList<Task>();
		for (int i=0; i < Knapsack.popSize - Knapsack.elitism; i++ ) {
			recs.add(recombineIndivs(i, current, sorts));
		}
		
		List<Task> muts = new ArrayList<Task>();
		for (int i=0; i < Knapsack.popSize - Knapsack.elitism; i++ ) {
			muts.add(mutateIndivs(i, current, recs));
		}
		makeSwitch(current, muts);
		
	}

	private static void makeSwitch(Task current,
			List<Task> prev) {
		Task ev = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				performSwitch();
			}
		}, Runtime.NO_HINTS);
		rt.schedule(ev, current, prev);
	}
	
	private static void performSwitch() {
		Indiv[] tmp = pop;
		pop = next;
		next = tmp;
	}

	private static Task mutateIndivs(final int i, Task current,
			List<Task> prev) {
		Task ev = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				try {
					Knapsack.mutate(next[i]);
				} catch(Exception e) {
					System.out.println("Here: " + next[i]);
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(ev, current, prev);
		return ev;
	}

	private static Task recombineIndivs(final int i,
			Task current, List<Task> prev) {
		Task ev = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Indiv other = (i < 10) ? pop[i+1] : pop[i-10];
				next[i] = Knapsack.recombine(pop[i], other);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(ev, current, prev);
		return ev;
	}

	private static Task sortArray(final int g, Task current, List<Task> prev) {
		Task ev = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Arrays.sort(pop);
				System.out.println("Best fit at " + g + ": " + pop[0].fitness);
				
				// Elitism
				for (int i=0; i < Knapsack.elitism; i++ ) {
					next[Knapsack.popSize - i - 1] = pop[i];
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(ev, current, prev);
		return ev;
	}

	private static Task evaluateIndiv(final int i, Task current) {
		Task ev = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Knapsack.evaluate(pop[i]);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(ev, current, Runtime.NO_DEPS);
		return ev;
	}

	private static Task createRandomIndiv(final int i) {
		Task init = rt.createNonBlockingTask(new Body() {

			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				pop[i] = Knapsack.createRandomIndiv();
			}
		}, Runtime.NO_HINTS);
		rt.schedule(init, Runtime.NO_PARENT, Runtime.NO_DEPS);
		return init;
	}
	
	
	
}
