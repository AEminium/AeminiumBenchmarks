package aeminium.runtime.benchmarks.fft;


public class SeqFFT {
	public static void main(String[] args) {
		int size = 524288;
		if (args.length > 0) size = Integer.parseInt(args[0]);
		
		Complex[] input = AeFFT.createRandomComplexArray(size);
		Complex[] result = AeFFT.sequentialFFT(input);
		
		if (args.length <= 1) {
			AeFFT.show(result, "Result");
		}
	}
}
