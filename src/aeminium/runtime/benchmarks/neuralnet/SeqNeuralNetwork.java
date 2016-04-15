package aeminium.runtime.benchmarks.neuralnet;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqNeuralNetwork {
	public static void main(String[] args) {
		
		Benchmark be = new Benchmark(args);

		int size = NeuralNetwork.TRAIN_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}
		while (!be.stop()) {
			NeuralNetwork nn = new NeuralNetwork();
			be.start();
			nn.train(size);
			be.end();

			if (be.verbose) {
				nn.evaluate();
			}
		}
	}
}
