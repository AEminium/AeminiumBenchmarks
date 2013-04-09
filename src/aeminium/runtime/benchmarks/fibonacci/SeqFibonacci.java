package aeminium.runtime.benchmarks.fibonacci;


public class SeqFibonacci {
	public static void main(String[] args) {
		long initialTime = System.currentTimeMillis();

		int fib = 46;
		if (args.length >= 1) {
			fib = Integer.parseInt(args[0]);
		}
		
		long val = AeFibonacci.seqFib(fib);

		System.out.println("F(" + fib + ") = " + val);

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}
}
