package aeminium.runtime.benchmarks.neuralnet;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjNeuralNetwork {
	
	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int size = NeuralNetwork.TRAIN_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		int threshold = NeuralNetwork.DEFAULT_THRESHOLD;
		if (be.args.length > 1) {
			threshold = Integer.parseInt(be.args[1]);
		}
		ForkJoinPool pool = new ForkJoinPool();
		while (!be.stop()) {
			NeuralNetwork nn = new NeuralNetwork();
			be.start();
			ForkJoinTrainer t1 = new ForkJoinTrainer(nn, 0, size, threshold);
			pool.invoke(t1);
			be.end();
			if (be.verbose) {
				nn.evaluate();
			}
		}
	}
	
	static class ForkJoinTrainer extends RecursiveAction {
		private static final long serialVersionUID = 1838371673290992232L;

		int start;
		int end;
		int threshold;
		NeuralNetwork nn;
		public ForkJoinTrainer(NeuralNetwork nn, int start, int end, int threshold) {
			this.start = start;
			this.end = end;
			this.nn = nn;
			this.threshold = threshold;
		}
		
		@Override
		protected void compute() {
			if ( end-start > threshold ) {
				int midpoint = (end - start)/2 + start;
				NeuralNetwork n2 = new NeuralNetwork(nn.size, nn.input, nn.output, nn.weightsHO, nn.weightsIH);
				ForkJoinTrainer t1 = new ForkJoinTrainer(nn, start, midpoint, threshold);
				ForkJoinTrainer t2 = new ForkJoinTrainer(n2, midpoint, end, threshold);
				invokeAll(t1,t2);
				nn.mergeFrom(n2);
			} else {
				nn.train(start, end);
			}
			
		}
		
	}

}
