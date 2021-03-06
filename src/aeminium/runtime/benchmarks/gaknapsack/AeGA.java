package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;
import java.util.Collection;

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

public class AeGA {

	static Indiv[] pop = new Indiv[Knapsack.popSize];
	static Indiv[] next = new Indiv[Knapsack.popSize];

	public static void main(String[] args) {
		final Benchmark be = new Benchmark(args);
		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		if (be.args.length > 0) {
			Knapsack.popSize = Integer.parseInt(be.args[0]);
			pop = new Indiv[Knapsack.popSize];
			next = new Indiv[Knapsack.popSize];
		}
		if (be.args.length > 1) Knapsack.numGen = Integer.parseInt(be.args[1]);

		while (!be.stop()) {
			Knapsack.resetSeed();
			be.start();
			rt.init();

			Task createRandomIndivs = ForTask.createFor(rt, new Range(Knapsack.popSize), new ForBody<Integer>() {
				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					pop[i] = Knapsack.createRandomIndiv();
				}
			}, (short) (Hints.NO_CHILDREN | Hints.LARGE));
			rt.schedule(createRandomIndivs, Runtime.NO_PARENT, Runtime.NO_DEPS);

			Task main = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					Collection<Task> previous = Runtime.NO_DEPS;
					for (int g = 0; g < Knapsack.numGen; g++) {

						Task eval = ForTask.createFor(rt, new Range(Knapsack.popSize), new ForBody<Integer>() {
							@Override
							public void iterate(Integer i, Runtime rt, Task current) {
								Knapsack.evaluate(pop[i]);
							}
						}, (short) (Hints.NO_CHILDREN | Hints.LARGE));
						rt.schedule(eval, Runtime.NO_PARENT, previous);

						Task sort = rt.createNonBlockingTask(new Body() {

							@Override
							public void execute(Runtime rt, Task current) throws Exception {
								Arrays.sort(pop);
							}
						}, (short) (Hints.LARGE | Hints.NO_CHILDREN));
						rt.schedule(sort, Runtime.NO_PARENT, Arrays.asList(eval));

						Task elitism = ForTask.createFor(rt, new Range(Knapsack.elitism), new ForBody<Integer>() {
							@Override
							public void iterate(Integer i, Runtime rt, Task current) {
								next[Knapsack.popSize - i - 1] = pop[i];
							}
						}, Hints.NO_CHILDREN);
						rt.schedule(elitism, Runtime.NO_PARENT, Arrays.asList(sort));

						Task recombine = ForTask.createFor(rt, new Range(Knapsack.popSize - Knapsack.elitism), new ForBody<Integer>() {
							@Override
							public void iterate(Integer i, Runtime rt, Task current) {
								Indiv other = (i < Knapsack.bestLimit) ? pop[i + 1] : pop[i - Knapsack.bestLimit];
								next[i] = Knapsack.recombine(pop[i], other);
							}
						}, Hints.NO_CHILDREN);
						rt.schedule(recombine, Runtime.NO_PARENT, Arrays.asList(elitism));

						Task mutation = ForTask.createFor(rt, new Range(Knapsack.popSize - Knapsack.elitism), new ForBody<Integer>() {
							@Override
							public void iterate(Integer i, Runtime rt, Task current) {
								Knapsack.mutate(next[i]);
							}
						}, Hints.NO_CHILDREN);
						rt.schedule(mutation, current, Arrays.asList(recombine));

						final int iter = g;
						Task switchThem = rt.createNonBlockingTask(new Body() {

							@Override
							public void execute(Runtime rt, Task current) throws Exception {
								if (be.verbose) {
									System.out.println("Best fit at " + iter + ": " + pop[0].fitness);
								}
								Indiv[] tmp = pop;
								pop = next;
								next = tmp;
							}
						}, (short) (Hints.SMALL | Hints.NO_CHILDREN));
						rt.schedule(switchThem, Runtime.NO_PARENT, Arrays.asList(mutation));

						previous = Arrays.asList(switchThem);

					}
				}
			}, (short) (Hints.LOOPS | Hints.NO_DEPENDENTS));
			rt.schedule(main, Runtime.NO_PARENT, Arrays.asList(createRandomIndivs));

			rt.shutdown();
			be.end();
		}
	}
}
