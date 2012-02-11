Aeminium vs ForkJoin Benchmarks
==============================================================================

Alcides Fonseca <amaf@dei.uc.pt>
Sven Stork <stork@dei.uc.pt>


Benchmarks
==============================================================================

BFS: From a balanced tree of random Integers of depth 23 and width 2, tries to 
find all the elements equals to 1. (Threshold 21)

FFT: Fast-Fourier Transform of an array of 1048576 random complex numbers. 
(Threshold 32768)

Fibonacci: Fibonacci of 23 (Threshold 16)

Integrate: Approximation of the integrate of (x^2 + 1)*x from -2101.0 to 200
with an error tolerance of 10^-11. (Threshold 10)

LCS: Least Common Subsequence of two random strings of size 800 with a block
size of 100.

LogCounter: Uncompresses and counts the number of accesses to a certain URL 
from 1000 Apache webserver gziped logs.

MergeSort: Sorts an array of 1000000 random longs using parallel mergesort.
(Threshold 10000)



Running Benchmark Suite
==============================================================================

$ ant run

Will list all available benchmark suites. To execute a particular benchmark, 
use:

$ ant -Dclass=<BENCHMARKSUITE> -Dp=<CODE>

The <BENCHMARKSUITE> is one of the options provided by "ant run".  Code can be 
one of the three values:

0: sequential version
1: forkjoin version
2: aeminium version


$ ant all

Runs all benchmarks and tests.
