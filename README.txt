Aeminium vs ForkJoin Benchmarks
==============================================================================

Alcides Fonseca <amaf@dei.uc.pt>
Sven Stork <stork@dei.uc.pt>


Benchmarks
==============================================================================

BFS: From a balanced tree of random Integers of depth 23 and width 2, tries to 
find all the elements equals to 1. (Threshold 21)

BlackScholes: MonteCarlo simulations using the BlackScholes Formula with 10000
points.

Do-All: Divides values from two arrays of 100 milion numbers.

FFT: Fast-Fourier Transform of an array of 1048576 random complex numbers. 
(Threshold 32768)

Fibonacci: Fibonacci of 23 (Threshold 16)

GA: Solves the Knapsack problem with 500 items using a Genetic Algorithm,
with population size of 100, 100 generations, a probability of mutation 
and recombination of 20% and elitism of the 10 best individuals.

Integrate: Approximation of the integrate of (x^2 + 1)*x from -2101.0 to 200
with an error tolerance of 10^-11. (Threshold 10)

Jacobi: Iterative mesh relaxation with barriers: 100 steps of nearest
neighbor averaging on 1024x1024 matrices of doubles.

LUD: Decomposition of 2048x2048 matrices with a 16 block size.

KDTree: finds the closest point to each existing point of the set in a 
10000000 random-element KD-tree. (Threshold 100)

MatrixMult: Multiplication of two 1000x1000 matrices. 

MergeSort: Sorts an array of 1000000 random longs using parallel mergesort.
(Threshold 10000)

MonteCarlo: A financial simulation, using Monte Carlo techniques to price 
products derived from the price of an underlying asset. The code generates 
N sample time series with the same mean and fluctuation as a series of 
historical data. (Threshold is 10 iterations) Source: JavaGrande

MolDyn: MolDyn is an N-body code modelling particles interacting under a 
Lennard-Jones potential in a cubic spatial volume with periodic boundary 
conditions. Performance is reported in interactions per second. The number 
of particles is give by N.  The computationally intense component of the 
benchmark is the force calculation, which calculates the force on a particle 
in a pair wise manner. (Threshold is 1000 particles) Source: JavaGrande

N-Body: Simulation predicting the individual motions of 50000 Jovian planets
 after three iterations.

NQueens: Calculates the number of solutions of the NQueens problem,
for boards between 8 and 15 squares of side.

Pi: Estimates the Pi value using a monte-carlo simulation with 100000000
darts and 48 of threshold.

QuickSort: Sorts an array of 1000000 random longs using quicksort.
(Threshold 10000)


Running Benchmark Suite
==============================================================================

$ ant run

Will list all available benchmark suites. To execute a particular benchmark, 
use:

$ ant benchmark -Dclass=<BENCHMARKSUITE> -Dp=<CODE>

The <BENCHMARKSUITE> is one of the options provided by "ant run".  Code can be 
one of the three values:

0: sequential version
1: forkjoin version
2: aeminium version


$ ant all

Runs all benchmarks and tests.
