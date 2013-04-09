package aeminium.runtime.benchmarks.fft;


public class SeqFFT {
	public static void main(String[] args) {
		Complex[] input = AeFFT.createRandomComplexArray(524288);
		Complex[] result = AeFFT.sequentialFFT(input);
		
		if (args.length == 0) {
			AeFFT.show(result, "Result");
		}
	}
}
