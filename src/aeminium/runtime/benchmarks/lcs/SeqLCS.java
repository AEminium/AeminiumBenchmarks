package aeminium.runtime.benchmarks.lcs;

public class SeqLCS {
	public static void main(String[] args) {
		
		String s1 = "The quick fox jumps over the lazy dog.";
		String s2 = "Jacob is a very lazy dog.";

		if (args.length > 0) {
			s1 = AeLCS.readFile(args[0]);
		}

		if (args.length > 1) {
			s2 = AeLCS.readFile(args[1]);
		}
		
		FjLCS longest = new FjLCS(5);
		
		System.out.println(longest.seqCompute(s1, s2));
	}
}
