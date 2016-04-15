package aeminium.runtime.benchmarks.neuralnet;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.implementations.Factory;

public class AeNeuralNetwork {
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
		Runtime rt = Factory.getRuntime();
		while (!be.stop()) {
			NeuralNetwork nn = new NeuralNetwork();
			be.start();
			rt.init();
			NNBody b1 = new NNBody(nn, 0, size, threshold);
			Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
			rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
			if (be.verbose) {
				nn.evaluate();
			}
		}
	}
	
	public static class NNBody implements Body {
		private int start;
		private int end;
		private int threshold;
		private NeuralNetwork nn;

		public NNBody(NeuralNetwork nn, int start, int end, int threshold) {
			this.nn = nn;
			this.start = start;
			this.end = end;
			this.threshold = threshold;
		}

		@Override
		public void execute(Runtime rt, Task current) {
			if ( end-start <= 2 && (Benchmark.useThreshold ? end-start < threshold : !rt.parallelize(current))) {
				nn.train(start, end);
			} else {
				int midpoint = (end - start)/2 + start;
				NeuralNetwork n2 = new NeuralNetwork(nn.size, nn.input, nn.output, nn.weightsHO, nn.weightsIH);
				NNBody b1 = new NNBody(nn, start, midpoint, threshold);
				Task t1 = rt.createNonBlockingTask(b1, Hints.RECURSION);
				rt.schedule(t1, Runtime.NO_PARENT, Runtime.NO_DEPS);
				
				NNBody b2 = new NNBody(n2, midpoint, end, threshold);
				Task t2 = rt.createNonBlockingTask(b2, Hints.RECURSION);
				rt.schedule(t2, Runtime.NO_PARENT, Runtime.NO_DEPS);

				t1.getResult();
				t2.getResult();

				nn.mergeFrom(n2);
			}
		}
	}
}
