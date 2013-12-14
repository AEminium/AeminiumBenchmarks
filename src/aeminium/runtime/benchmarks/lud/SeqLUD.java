package aeminium.runtime.benchmarks.lud;

import java.util.Random;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqLUD extends LUD {
	
	public SeqLUD(Matrix a, int bs) {
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
		
		be.start();
		final SeqLUD lu = new SeqLUD(A, blocksize);
		lu.calcLU(new MatrixPosition(0, 0), numOfBlocks);
		be.end();		
	}

	/* ---------------------------------------------
	 * Divide-and-conquer matrix LU decomposition.
	 * --------------------------------------------- */

	/**
	 * schur - Compute M' = M - VW.
	 * @param posM The start position of matrix M in array LU
	 * @param posV The start position of matrix V in array LU
	 * @param posW The start position of matrix W in array LU
	 * @param numOfBlocks The extent of the target matrix in LU 
	 *                    (with unit = BLOCK_SIZE)
	 **/
	public void schur(MatrixPosition posM,  
			MatrixPosition posV, 
			MatrixPosition posW, int numOfBlocks) {
		/* Check base case. */
		if (numOfBlocks == 1) {
			blockSchur(posM, posV, posW);
			return;
		}

		/*MatrixPosition posM00, posM01, posM10, posM11;
         MatrixPosition posV00, posV01, posV10, posV11;
         MatrixPosition posW00, posW01, posW10, posW11;
         int halfNb; */

		/* Break matrices into 4 pieces. */
		final int halfNb = numOfBlocks / 2;
		final MatrixPosition posM00 = posM;
		final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posM10 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), posM.col);
		final MatrixPosition posM11 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), 
				posM.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posV00 = posV;
		final MatrixPosition posV01 = new MatrixPosition(posV.row, posV.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posV10 = new MatrixPosition(posV.row+(halfNb*BLOCK_SIZE), posV.col);
		final MatrixPosition posV11 = new MatrixPosition(posV.row+(halfNb*BLOCK_SIZE), 
				posV.col+(halfNb*BLOCK_SIZE));

		final MatrixPosition posW00 = posW;
		final MatrixPosition posW01 = new MatrixPosition(posW.row, posW.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posW10 = new MatrixPosition(posW.row+(halfNb*BLOCK_SIZE), posW.col);
		final MatrixPosition posW11 = new MatrixPosition(posW.row+(halfNb*BLOCK_SIZE), 
				posW.col+(halfNb*BLOCK_SIZE));

		/* Form Schur complement with recursive calls. */
		schur(posM00, posV00, posW00, halfNb);
		schur(posM01, posV00, posW01, halfNb);
		schur(posM10, posV10, posW00, halfNb);
		schur(posM11, posV10, posW01, halfNb);

		schur(posM00, posV01, posW10, halfNb);
		schur(posM01, posV01, posW11, halfNb);
		schur(posM10, posV11, posW10, halfNb);
		schur(posM11, posV11, posW11, halfNb);

		return;
	}



	/**
	 * lowerSolve - Compute M' where LM' = M.
	 * @param posM The start position of matrix M in array LU
	 * @param posL The start position of matrix L in array LU
	 * @param numOfBlocks The extent of the target matrix in LU 
	 *                    (with unit = BLOCK_SIZE)
	 **/
	public void lowerSolve(MatrixPosition posM, 
			final MatrixPosition posL, final int numOfBlocks) {
		/* Check base case. */
		if (numOfBlocks == 1) {
			blockLowerSolve(posM, posL);
			return;
		}

		/* Break matrices into 4 pieces. */
		final int halfNb = numOfBlocks / 2;
		/* MatrixPosition posM00, posM01, posM10, posM11; */

		final MatrixPosition posM00 = posM;
		final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posM10 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), posM.col);
		final MatrixPosition posM11 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), 
				posM.col+(halfNb*BLOCK_SIZE));

		/* Solve with recursive calls. */
		auxLowerSolve(posM00, posM10, posL, halfNb);
		auxLowerSolve(posM01, posM11, posL, halfNb);


		return;
	}

	@SuppressWarnings("unused")
	public void auxLowerSolve(final MatrixPosition posMa, 
			final MatrixPosition posMb, 
			MatrixPosition posL, 
			final int numOfBlocks) {
		/* MatrixPosition posL00, posL01, posL10, posL11; */

		/* Break L matrix into 4 pieces. */
		final MatrixPosition posL00 = posL;
		final MatrixPosition posL01 = new MatrixPosition(posL.row, 
				posL.col+(numOfBlocks*BLOCK_SIZE));
		final MatrixPosition posL10 = new MatrixPosition(posL.row+(numOfBlocks*BLOCK_SIZE),
				posL.col);
		final MatrixPosition posL11 = new MatrixPosition(posL.row+(numOfBlocks*BLOCK_SIZE),
				posL.col+(numOfBlocks*BLOCK_SIZE));

		/* Solve with recursive calls. */
		lowerSolve(posMa, posL00, numOfBlocks);

		schur(posMb, posL10, posMa, numOfBlocks);

		lowerSolve(posMb, posL11, numOfBlocks);

		return;
	}


	/**
	 * upperSolve - Compute M' where M'U = M.
	 * @param posM The start position of matrix M in array LU
	 * @param posU The start position of matrix U in array LU
	 * @param numOfBlocks The extent of the target matrix in LU 
	 *                    (with unit = BLOCK_SIZE)
	 **/
	public void upperSolve(MatrixPosition posM, 
			final MatrixPosition posU, int numOfBlocks) {
		/* Check base case. */
		if (numOfBlocks == 1) {
			blockUpperSolve(posM, posU);
			return;
		}

		/* Break matrices into 4 pieces. */
		final int halfNb= numOfBlocks / 2;
		/* MatrixPosition posM00, posM01, posM10, posM11; */

		final MatrixPosition posM00 = posM;
		final MatrixPosition posM01 = new MatrixPosition(posM.row, posM.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition posM10 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), posM.col);
		final MatrixPosition posM11 = new MatrixPosition(posM.row+(halfNb*BLOCK_SIZE), 
				posM.col+(halfNb*BLOCK_SIZE));

		/* Solve with recursive calls. */
		auxUpperSolve(posM00, posM01, posU, halfNb);
		auxUpperSolve(posM10, posM11, posU, halfNb);

		return;
	}

	@SuppressWarnings("unused")
	public void auxUpperSolve(final MatrixPosition posMa, 
			final MatrixPosition posMb, 
			final MatrixPosition posU, 
			final int numOfBlocks) {
		/* MatrixPosition posU00, posU01, posU10, posU11; */

		/* Break U matrix into 4 pieces. */
		final MatrixPosition posU00 = posU;
		final MatrixPosition posU01 = new MatrixPosition(posU.row, 
				posU.col+(numOfBlocks*BLOCK_SIZE));
		final MatrixPosition posU10 = new MatrixPosition(posU.row+(numOfBlocks*BLOCK_SIZE), 
				posU.col);
		final MatrixPosition posU11 = new MatrixPosition(posU.row+(numOfBlocks*BLOCK_SIZE), 
				posU.col+(numOfBlocks*BLOCK_SIZE));

		/* Solve with recursive calls. */
		upperSolve(posMa, posU00, numOfBlocks);

		schur(posMb, posMa, posU01, numOfBlocks);

		upperSolve(posMb, posU11, numOfBlocks);

		return;
	}

	boolean isFirstCall = true;                                                                 
	/** 
	 * calcLU - Perform LU decomposition on the matrix with value 
	 *           represented by this LU array.
	 * @param pos The position of where the target matrix starts in array LU
	 * @param numOfBlocks The extent of the target matrix in LU 
	 *                    (with unit = BLOCK_SIZE)
	 */
	public /*cilk Matrix*/ void calcLU(MatrixPosition pos, int numOfBlocks) {
		if (isFirstCall) { // first time called
			pos = new MatrixPosition(0, 0);
			isFirstCall = false;
		} 
		if(numOfBlocks == 1) {
			blockLU(pos);
			return; //***  new Matrix(LU);
		}

		final int halfNb = numOfBlocks / 2;
		/* MatrixPosition pos00, pos01, pos10, pos11; */

		final MatrixPosition pos00 = pos;
		final MatrixPosition pos01 = new MatrixPosition(pos.row, pos.col+(halfNb*BLOCK_SIZE));
		final MatrixPosition pos10 = new MatrixPosition(pos.row+(halfNb*BLOCK_SIZE), pos.col);
		final MatrixPosition pos11 = new MatrixPosition(pos.row+(halfNb*BLOCK_SIZE), 
				pos.col+(halfNb*BLOCK_SIZE));

		calcLU(pos00, halfNb);

		lowerSolve(pos01, pos00, halfNb);
		upperSolve(pos10, pos00, halfNb);

		schur(pos11, pos10, pos01, halfNb);

		calcLU(pos11, halfNb);

		//*** return new Matrix(LU);
	}

}
