Aeminium vs ForkJoin Benchmarks
==============================================================================

Alcides Fonseca <amaf@dei.uc.pt>
Sven Stork <stork@dei.uc.pt>


Benchmarks
==============================================================================

BFS: From a balanced tree of random Integers of depth 23 and width 2, tries to 
find all the elements equals to 1. (Threshold 21) Source: PBBS

BlackScholes: MonteCarlo simulations using the BlackScholes Formula with 10000
points. Source: TBB

Convex Hull: Calculates the set of points in the convex hull of 10 millions
random points. Source: PBBS

Do-All: Divides values from two arrays of 100 milion numbers.

FFT: Fast-Fourier Transform of an array of 1048576 random complex numbers. 
(Threshold 32768) Source: HPCC

Fibonacci: Fibonacci of 23 (Threshold 16) Source: ForkJoin

GA: Solves the Knapsack problem with 500 items using a Genetic Algorithm,
with population size of 100, 100 generations, a probability of mutation 
and recombination of 20% and elitism of the 10 best individuals.

Heat: Heat diffusion simulation across a mesh of size 4096 x 1024 
(based on algorithm from Cilk- 5.4.6). Leaf column size of 10 is 
the granularity parameter. Timestep used is 200. Source: ForkJoin

Health: simulates de Columbian Health Care System[25]. It uses multilevel 
lists where each element in the structure represents a village with a list 
of potential patients and one hospital. The hospital has several 
double-linked lists representing the possible status of a patient inside it 
(waiting, in assessment, in treatment or waiting for reallocation). At each 
timestep all patients are simulated according with several probabilities (of 
getting sick, needing a convalescence treatment, or being reallocated to an 
upper level hospital). A task is created for each village being simulated. 
Once the lower levels have been simulated synchronization occurs. Source: BOTS

Integrate: Approximation of the integrate of (x^2 + 1)*x from -2101.0 to 200
with an error tolerance of 10^-11. (Threshold 10) Source: ForkJoin

Jacobi: Iterative mesh relaxation with barriers: 100 steps of nearest
neighbor averaging on 1024x1024 matrices of doubles. Source: ForkJoin

LUD: Decomposition of 2048x2048 matrices with a 16 block size. Source: ForkJoin

KDTree: finds the closest point to each existing point of the set in a 
10000000 random-element KD-tree. (Threshold 100) Source: LoneStar

MatrixMult: Multiplication of two 1000x1000 matrices. Source: ForkJoin

MergeSort: Sorts an array of 1000000 random longs using parallel mergesort.
(Threshold 10000) Source: DPJ

MonteCarlo: A financial simulation, using Monte Carlo techniques to price 
products derived from the price of an underlying asset. The code generates 
N sample time series with the same mean and fluctuation as a series of 
historical data. (Threshold is 10 iterations) Source: JavaGrande

MolDyn: MolDyn is an N-body code modeling particles interacting under a 
Lennard-Jones potential in a cubic spatial volume with periodic boundary 
conditions. Performance is reported in interactions per second. The number 
of particles is give by N.  The computationally intense component of the 
benchmark is the force calculation, which calculates the force on a particle 
in a pair wise manner. (Threshold is 1000 particles) Source: JavaGrande

N-Body: Simulation predicting the individual motions of 50000 Jovian planets
 after three iterations. Source: PBBS

NeuralNet: Training a Neural Network with 1 hidden layer. The data is randomly
generated according to the network size.

NH: Nemhauser-Ullmann algorithm for Multi-Objective Knapsack problem of any
dimension. 

NQueens: Calculates the number of solutions of the NQueens problem,
for boards between 8 and 15 squares of side. Source: ForkJoin

Pi: Estimates the Pi value using a monte-carlo simulation with 100000000
darts and 48 of threshold.

QuickSort: Sorts an array of 1000000 random longs using quicksort.
(Threshold 10000) Source: ForkJoin

RayTracer: This benchmark measures the performance of a 3D raytracer. The scene 
rendered contains 64 spheres, and is rendered at a resolution of 500x500 pixels.
(Threshold is 10 pixels) Source: JavaGrande


ForkJoin: Work-Stealing Without The Baggage

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
