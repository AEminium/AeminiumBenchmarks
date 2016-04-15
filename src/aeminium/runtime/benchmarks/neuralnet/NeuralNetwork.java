package aeminium.runtime.benchmarks.neuralnet;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class NeuralNetwork {
	public static final int MAX_VALUE = 5;
	public static final int TRAIN_SIZE = 500000;
	public static int DEFAULT_SIZE = 5000;
	public static int DEFAULT_THRESHOLD = 10000;
	
	public static int NUM_INPUTS = 10; //number of inputs - this includes the input bias
	public static int NUM_HIDDEN = 20;
	
	public static double LR_IH = 0.7; //learning rate
	public static double LR_HO = 0.07; //learning rate
	
	public static double[][] generateTrainingInput(int size) {
		double[][] data = new double[size][NUM_INPUTS];
		for (int i=0; i<size; i++) {
			for (int j=0; j < NUM_INPUTS; j++) {
				data[i][j] = ThreadLocalRandom.current().nextDouble() - 0.5; 	
			}
		}
		return data;
	}
	
	public static double[] generateTrainingOutput(double[][] input, int size) {
		double[] output = new double[size];
		for (int i=0; i<size; i++) {
			output[i] = 0;
			for (int j=0; j < NUM_INPUTS; j++)
				output[i] += input[i][j];
		}
		return output;
	}
	
	public static double tanh(double x) {
	    if (x > 20)
	        return 1;
	    else if (x < -20)
	        return -1;
	    else {
	        double a = Math.exp(x);
	        double b = Math.exp(-x);
	        return (a-b)/(a+b);
	    }
	 }
	
	public double[][] weightsIH = new double[NeuralNetwork.NUM_INPUTS][NeuralNetwork.NUM_HIDDEN];
	public double[] weightsHO = new double[NeuralNetwork.NUM_HIDDEN];
	public double[] hiddenVal = new double[NeuralNetwork.NUM_HIDDEN];

	public int size = DEFAULT_SIZE;
	public double[][] input;
	public double[] output;
	
	
	public NeuralNetwork() {
		input = NeuralNetwork.generateTrainingInput(size);
		output = NeuralNetwork.generateTrainingOutput(input, size);
		initWeights();
	}

	public NeuralNetwork(int size, double[][] input, double[] output, double[] weightsHO, double[][] weightsIH) {
		this.size = size;
		this.input = input;
		this.output = output;
		this.weightsHO = Arrays.copyOf(weightsHO, weightsHO.length);
		this.weightsIH = Arrays.copyOf(weightsIH, weightsIH.length);
	}

	public void train(int size) {
		train(0, size);
	}
	
	public void train(int start, int end) {
		for (int i = start; i < end; i++) {
			double r = forward(input[i % size]);
			double error = r - output[i % size];
			propagateBack(input[i % size], error);
		}
	}
	
	public double forward(double[] row) {
		for (int i = 0; i < NeuralNetwork.NUM_HIDDEN; i++) {
			hiddenVal[i] = 0;
			for (int j = 0; j < NeuralNetwork.NUM_INPUTS; j++) {
				hiddenVal[i] += (row[j] * weightsIH[j][i]);
			}
			hiddenVal[i] = NeuralNetwork.tanh(hiddenVal[i]);
		}
		double outPred = 0.0;
		for (int i = 0; i < NeuralNetwork.NUM_HIDDEN; i++) {
			outPred = outPred + hiddenVal[i] * weightsHO[i];
		}
		return outPred;
	}

	private void propagateBack(double[] row, double error) {
		for (int k = 0; k < NeuralNetwork.NUM_HIDDEN; k++) {
			double weightChange = NeuralNetwork.LR_HO * error * hiddenVal[k];
			weightsHO[k] -= weightChange;
			if (weightsHO[k] < -NeuralNetwork.MAX_VALUE) weightsHO[k] = -NeuralNetwork.MAX_VALUE;
			if (weightsHO[k] > NeuralNetwork.MAX_VALUE) weightsHO[k] = NeuralNetwork.MAX_VALUE;
		}
		
		for (int i = 0; i < NeuralNetwork.NUM_HIDDEN; i++) {
			for (int j = 0; j < NeuralNetwork.NUM_INPUTS; j++) {
				double x = 1 - (hiddenVal[i] * hiddenVal[i]);
			    x = x * weightsHO[i] * error * NeuralNetwork.LR_IH;
			    x = x * row[j];
			    double weightChange = x;
			    weightsIH[j][i] = weightsIH[j][i] - weightChange;
			}
		}
	}

	private void initWeights() {
		for (int j = 0; j < NeuralNetwork.NUM_HIDDEN; j++) {
			weightsHO[j] = (Math.random() - 0.5) / 2;
			for (int i = 0; i < NeuralNetwork.NUM_INPUTS; i++) {
				weightsIH[i][j] = (Math.random() - 0.5) / 5;
			}
		}
	}

	public void mergeFrom(NeuralNetwork n2) {
		for (int k = 0; k < NeuralNetwork.NUM_HIDDEN; k++) {
			weightsHO[k] = (weightsHO[k] + n2.weightsHO[k])/2.0;
		}
		for (int i = 0; i < NeuralNetwork.NUM_HIDDEN; i++) {
			for (int j = 0; j < NeuralNetwork.NUM_INPUTS; j++) {
			    weightsIH[j][i] = (weightsIH[j][i] + n2.weightsIH[j][i]) / 2.0;
			}
		}
	}
	
	public void evaluate() {
		double totalError = 0.0;
		for (int i = 0; i < size; i++) {
			double r = forward(input[i]);
			double error = r - output[i];
			totalError += error * error;
		}
		totalError = Math.sqrt(totalError / size);
		System.out.println("RMS: " + totalError);
	}
}
