package aeminium.runtime.benchmarks.gaknapsack;

import java.util.Arrays;
import java.util.Collection;

import aeminium.runtime.Body;
import aeminium.runtime.ErrorHandler;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;

public class AeGA {
	
	public static Runtime rt = Factory.getRuntime();
	static Indiv[] pop = new Indiv[Knapsack.popSize];
	static Indiv[] next = new Indiv[Knapsack.popSize];
	public static boolean debug = false;
	
	
	public static void main(String[] args) {;
		rt.init();
		
		rt.addErrorHandler(new ErrorHandler() {

			@Override
			public void handleTaskException(Task task, Throwable t) {
				t.printStackTrace();
			}

			@Override
			public void handleLockingDeadlock() {}
			@Override
			public void handleDependencyCycle(Task task) {}
			@Override
			public void handleTaskDuplicatedSchedule(Task task) {}
			@Override
			public void handleInternalError(Error err) {}
		});
		
		if (args.length >= 1) {
			Knapsack.popSize = Integer.parseInt(args[0]);
			pop = new Indiv[Knapsack.popSize];
			next = new Indiv[Knapsack.popSize];
		}
		if (args.length >= 2)
			Knapsack.numGen = Integer.parseInt(args[1]);
		
		
		Task createRandomIndivs = ForTask.createFor(rt, new Range(Knapsack.popSize), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i) {
				pop[i] = Knapsack.createRandomIndiv();
			}
		});
		rt.schedule(createRandomIndivs, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		Task main = rt.createNonBlockingTask(new Body(){
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				Collection<Task> previous = Runtime.NO_DEPS;
				for (int g=0; g<Knapsack.numGen; g++) {
					
					Task eval = ForTask.createFor(rt, new Range(Knapsack.popSize), new ForBody<Integer>() {
						@Override
						public void iterate(Integer i) {
							Knapsack.evaluate(pop[i]);
						}
					});
					rt.schedule(eval, Runtime.NO_PARENT, previous);
					
					Task sort = rt.createNonBlockingTask(new Body() {

						@Override
						public void execute(Runtime rt, Task current)
								throws Exception {
							Arrays.sort(pop);
						}
					}, Runtime.NO_HINTS);
					rt.schedule(sort, Runtime.NO_PARENT, Arrays.asList(eval));
					
					Task elitism = ForTask.createFor(rt, new Range(Knapsack.elitism), new ForBody<Integer>() {
						@Override
						public void iterate(Integer i) {
							next[Knapsack.popSize - i - 1] = pop[i];
						}
					});
					rt.schedule(elitism, Runtime.NO_PARENT, Arrays.asList(sort));
					
					Task recombine = ForTask.createFor(rt, new Range(Knapsack.popSize - Knapsack.elitism), new ForBody<Integer>() {
						@Override
						public void iterate(Integer i) {
							Indiv other = (i < Knapsack.bestLimit) ? pop[i+1] : pop[i-Knapsack.bestLimit];
							next[i] = Knapsack.recombine(pop[i], other);
						}
					});
					rt.schedule(recombine, Runtime.NO_PARENT, Arrays.asList(elitism));
					
					Task mutation = ForTask.createFor(rt, new Range(Knapsack.popSize - Knapsack.elitism), new ForBody<Integer>() {
						@Override
						public void iterate(Integer i) {
							Knapsack.mutate(next[i]);
						}
					});
					rt.schedule(mutation, current, Arrays.asList(recombine));
					
					final int iter = g;
					Task switchThem = rt.createNonBlockingTask(new Body() {

						@Override
						public void execute(Runtime rt, Task current)
								throws Exception {
							if (debug || iter == Knapsack.numGen-1) {
								System.out.println("Best fit at " + iter + ": " + pop[0].fitness);
							}
							Indiv[] tmp = pop;
							pop = next;
							next = tmp;
						}
					}, Runtime.NO_HINTS);
					rt.schedule(switchThem, Runtime.NO_PARENT, Arrays.asList(mutation));
					
					previous = Arrays.asList(switchThem);
					
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(main, Runtime.NO_PARENT, Arrays.asList(createRandomIndivs));
		
		rt.shutdown();
	}
}
