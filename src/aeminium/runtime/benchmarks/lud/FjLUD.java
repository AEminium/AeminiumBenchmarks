package aeminium.runtime.benchmarks.lud;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class FjLUD extends LUD {

	public FjLUD(Matrix a, int bs) {
		super(a, bs);
	}

	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		int size = LUD.DEFAULT_SIZE;
		if (be.args.length > 0) {
			size = Integer.parseInt(be.args[0]);
		}

		int blocksize = LUD.DEFAULT_BLOCK_SIZE;
		if (be.args.length > 1) {
			blocksize = Integer.parseInt(be.args[1]);
		}

		Random r = new Random(1L);
		Matrix A = Matrix.random(size, size, r);
		final int numOfBlocks = size / blocksize;

		ForkJoinPool pool = new ForkJoinPool();
		while (!be.stop()) {
			be.start();
			final FjLUD lud = new FjLUD(A, blocksize);
			pool.invoke(new CalcLU(lud, new MatrixPosition(0, 0), numOfBlocks));
			be.end();
		}
	}

	@SuppressWarnings("serial")
	static final class Schur extends RecursiveAction {
		final MatrixPosition posM;
		final MatrixPosition posV;
		final MatrixPosition posW;
		final int numOfBlocks;
		final LUD lud;

		Schur(final LUD lud, final MatrixPosition posM, final MatrixPosition posV, final MatrixPosition posW, final int numOfBlocks) {
			this.lud = lud;
			this.posM = posM;
			this.posV = posV;
			this.posW = posW;
			this.numOfBlocks = numOfBlocks;
		}

		public void compute() {
			if (numOfBlocks == 1) {
				lud.blockSchur(posM, posV, posW);
				return;
			}
			int BLOCK_SIZE = lud.BLOCK_SIZE;
			final int halfNb = numOfBlocks / 2;
			final MatrixPosition posM00 = posM;
			final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posM10 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col);
			final MatrixPosition posM11 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posV00 = posV;
			final MatrixPosition posV01 = new MatrixPosition(posV.row, posV.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posV10 = new MatrixPosition(posV.row + (halfNb * BLOCK_SIZE), posV.col);
			final MatrixPosition posV11 = new MatrixPosition(posV.row + (halfNb * BLOCK_SIZE), posV.col + (halfNb * BLOCK_SIZE));

			final MatrixPosition posW00 = posW;
			final MatrixPosition posW01 = new MatrixPosition(posW.row, posW.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posW10 = new MatrixPosition(posW.row + (halfNb * BLOCK_SIZE), posW.col);
			final MatrixPosition posW11 = new MatrixPosition(posW.row + (halfNb * BLOCK_SIZE), posW.col + (halfNb * BLOCK_SIZE));

			/* Form Schur complement with recursive calls. */
			final Schur s1 = new Schur(lud, posM00, posV00, posW00, halfNb);
			final Schur s2 = new Schur(lud, posM01, posV00, posW01, halfNb);
			final Schur s3 = new Schur(lud, posM10, posV10, posW00, halfNb);
			final Schur s4 = new Schur(lud, posM11, posV10, posW01, halfNb);
			s1.fork();
			s2.fork();
			s3.fork();
			s4.compute();

			if (s3.tryUnfork()) s3.compute();
			else s3.join();
			if (s2.tryUnfork()) s2.compute();
			else s2.join();
			if (s1.tryUnfork()) s1.compute();
			else s1.join();

			final Schur s5 = new Schur(lud, posM00, posV01, posW10, halfNb);
			final Schur s6 = new Schur(lud, posM01, posV01, posW11, halfNb);
			final Schur s7 = new Schur(lud, posM10, posV11, posW10, halfNb);
			final Schur s8 = new Schur(lud, posM11, posV11, posW11, halfNb);
			s5.fork();
			s6.fork();
			s7.fork();
			s8.compute();

			if (s7.tryUnfork()) s7.compute();
			else s7.join();
			if (s6.tryUnfork()) s6.compute();
			else s6.join();
			if (s5.tryUnfork()) s5.compute();
			else s5.join();
		}
	}

	@SuppressWarnings("serial")
	static final class LowerSolve extends RecursiveAction {
		final MatrixPosition posM;
		final MatrixPosition posL;
		final int numOfBlocks;
		final LUD lud;

		LowerSolve(final LUD lud, final MatrixPosition posM, final MatrixPosition posL, final int numOfBlocks) {
			this.lud = lud;
			this.posM = posM;
			this.posL = posL;
			this.numOfBlocks = numOfBlocks;
		}

		public void compute() {
			/* Check base case. */
			if (numOfBlocks == 1) {
				lud.blockLowerSolve(posM, posL);
				return;
			}

			int BLOCK_SIZE = lud.BLOCK_SIZE;

			/* Break matrices into 4 pieces. */
			final int halfNb = numOfBlocks / 2;
			/* MatrixPosition posM00, posM01, posM10, posM11; */

			final MatrixPosition posM00 = posM;
			final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posM10 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col);
			final MatrixPosition posM11 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col + (halfNb * BLOCK_SIZE));

			/* Solve with recursive calls. */
			final AuxLowerSolve a1 = new AuxLowerSolve(lud, posM00, posM10, posL, halfNb);
			final AuxLowerSolve a2 = new AuxLowerSolve(lud, posM01, posM11, posL, halfNb);

			a1.fork();
			a2.compute();
			if (a1.tryUnfork()) a1.compute();
			else a1.join();
		}
	}

	@SuppressWarnings("serial")
	static final class AuxLowerSolve extends RecursiveAction {
		final MatrixPosition posMa;
		final MatrixPosition posMb;
		final MatrixPosition posL;
		final int numOfBlocks;
		final LUD lud;

		AuxLowerSolve(final LUD lud, final MatrixPosition posMa, final MatrixPosition posMb, MatrixPosition posL, final int numOfBlocks) {
			this.lud = lud;
			this.posL = posL;
			this.posMa = posMa;
			this.posMb = posMb;
			this.numOfBlocks = numOfBlocks;
		}

		@SuppressWarnings("unused")
		public void compute() {
			int BLOCK_SIZE = lud.BLOCK_SIZE;
			final MatrixPosition posL00 = posL;
			final MatrixPosition posL01 = new MatrixPosition(posL.row, posL.col + (numOfBlocks * BLOCK_SIZE));
			final MatrixPosition posL10 = new MatrixPosition(posL.row + (numOfBlocks * BLOCK_SIZE), posL.col);
			final MatrixPosition posL11 = new MatrixPosition(posL.row + (numOfBlocks * BLOCK_SIZE), posL.col + (numOfBlocks * BLOCK_SIZE));

			/* Solve with recursive calls. */
			new LowerSolve(lud, posMa, posL00, numOfBlocks).compute();

			new Schur(lud, posMb, posL10, posMa, numOfBlocks).compute();

			new LowerSolve(lud, posMb, posL11, numOfBlocks).compute();
		}
	}

	/**
	 * upperSolve - Compute M' where M'U = M.
	 * 
	 * @param posM
	 *            The start position of matrix M in array LU
	 * @param posU
	 *            The start position of matrix U in array LU
	 * @param numOfBlocks
	 *            The extent of the target matrix in LU (with unit = BLOCK_SIZE)
	 **/
	@SuppressWarnings("serial")
	static final class UpperSolve extends RecursiveAction {
		final MatrixPosition posM;
		final MatrixPosition posU;
		final int numOfBlocks;
		final LUD lud;

		UpperSolve(LUD lud, MatrixPosition posM, final MatrixPosition posU, int numOfBlocks) {
			this.lud = lud;
			this.posM = posM;
			this.posU = posU;
			this.numOfBlocks = numOfBlocks;
		}

		public void compute() {
			/* Check base case. */
			if (numOfBlocks == 1) {
				lud.blockUpperSolve(posM, posU);
				return;
			}
			final int BLOCK_SIZE = lud.BLOCK_SIZE;
			/* Break matrices into 4 pieces. */
			final int halfNb = numOfBlocks / 2;
			/* MatrixPosition posM00, posM01, posM10, posM11; */

			final MatrixPosition posM00 = posM;
			final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition posM10 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col);
			final MatrixPosition posM11 = new MatrixPosition(posM.row + (halfNb * BLOCK_SIZE), posM.col + (halfNb * BLOCK_SIZE));

			/* Solve with recursive calls. */
			final AuxUpperSolve a1 = new AuxUpperSolve(lud, posM00, posM01, posU, halfNb);
			final AuxUpperSolve a2 = new AuxUpperSolve(lud, posM10, posM11, posU, halfNb);

			a1.fork();
			a2.compute();
			if (a1.tryUnfork()) a1.compute();
			else a1.join();
		}
	}

	@SuppressWarnings("serial")
	static final class AuxUpperSolve extends RecursiveAction {
		final MatrixPosition posMa;
		final MatrixPosition posMb;
		final MatrixPosition posU;
		final int numOfBlocks;
		final LUD lud;

		AuxUpperSolve(final LUD lud, final MatrixPosition posMa, final MatrixPosition posMb, final MatrixPosition posU, final int numOfBlocks) {
			this.lud = lud;
			this.posMa = posMa;
			this.posMb = posMb;
			this.posU = posU;
			this.numOfBlocks = numOfBlocks;
		}

		@SuppressWarnings("unused")
		public void compute() {
			final int BLOCK_SIZE = lud.BLOCK_SIZE;
			/* MatrixPosition posU00, posU01, posU10, posU11; */

			/* Break U matrix into 4 pieces. */
			final MatrixPosition posU00 = posU;
			final MatrixPosition posU01 = new MatrixPosition(posU.row, posU.col + (numOfBlocks * BLOCK_SIZE));
			final MatrixPosition posU10 = new MatrixPosition(posU.row + (numOfBlocks * BLOCK_SIZE), posU.col);
			final MatrixPosition posU11 = new MatrixPosition(posU.row + (numOfBlocks * BLOCK_SIZE), posU.col + (numOfBlocks * BLOCK_SIZE));

			/* Solve with recursive calls. */
			new UpperSolve(lud, posMa, posU00, numOfBlocks).compute();

			new Schur(lud, posMb, posMa, posU01, numOfBlocks).compute();

			new UpperSolve(lud, posMb, posU11, numOfBlocks).compute();
		}
	}

	public static boolean isFirstCall = true;

	/**
	 * calcLU - Perform LU decomposition on the matrix with value represented by
	 * this LU array.
	 * 
	 * @param pos
	 *            The position of where the target matrix starts in array LU
	 * @param numOfBlocks
	 *            The extent of the target matrix in LU (with unit = BLOCK_SIZE)
	 */
	@SuppressWarnings("serial")
	static final class CalcLU extends RecursiveAction {
		MatrixPosition pos;
		final int numOfBlocks;
		final LUD lud;

		CalcLU(LUD lud, MatrixPosition pos, int numOfBlocks) {
			this.lud = lud;
			this.pos = pos;
			this.numOfBlocks = numOfBlocks;
		}

		public void compute() {
			if (isFirstCall) { // first time called
				pos = new MatrixPosition(0, 0);
				isFirstCall = false;
			}
			if (numOfBlocks == 1) {
				lud.blockLU(pos);
				return; // *** new Matrix(LU);
			}

			int BLOCK_SIZE = lud.BLOCK_SIZE;

			final int halfNb = numOfBlocks / 2;
			/* MatrixPosition pos00, pos01, pos10, pos11; */

			final MatrixPosition pos00 = pos;
			final MatrixPosition pos01 = new MatrixPosition(pos.row, pos.col + (halfNb * BLOCK_SIZE));
			final MatrixPosition pos10 = new MatrixPosition(pos.row + (halfNb * BLOCK_SIZE), pos.col);
			final MatrixPosition pos11 = new MatrixPosition(pos.row + (halfNb * BLOCK_SIZE), pos.col + (halfNb * BLOCK_SIZE));

			new CalcLU(lud, pos00, halfNb).compute();

			final LowerSolve l1 = new LowerSolve(lud, pos01, pos00, halfNb);
			final UpperSolve u1 = new UpperSolve(lud, pos10, pos00, halfNb);
			l1.fork();
			u1.compute();
			if (l1.tryUnfork()) l1.compute();
			else l1.join();

			new Schur(lud, pos11, pos10, pos01, halfNb).compute();
			new CalcLU(lud, pos11, halfNb).compute();
		}
	}
}
