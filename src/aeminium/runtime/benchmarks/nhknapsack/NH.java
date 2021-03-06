package aeminium.runtime.benchmarks.nhknapsack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


public class NH {
	
	public static final int lookahead = 0;

	public static int NDIM = 2;

	public static final int threshold = 5000;

	public static final int lookahead_threshold = 100;
	
	public static int[][] importDataObjects(String fileName, int dim) {
        String line = null;
        int[][] arr = null;
        int size = 0;
        int i = 0;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
            	if (size == 0) {
            		size = Integer.parseInt(line.trim());
            		arr = new int[size][dim];
            	} else {
            		int[] o = new int[dim];
            		String[] parts = line.trim().replaceAll(" +", " ").split(" ");
            		for (int j=0; j<dim; j++) {
            			o[j] = Integer.parseInt(parts[1+j].trim());
            		}
            		arr[i++] = o;
            		if (i == size) break;
            	}
            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }
		return arr;
	}
	
	public static void printPareto(int[] paretoFront) {
		System.out.println(paretoFront.length);
		/*
		for (int i=0; i<paretoFront.length/NDIM; i+=NDIM) {
			int value = paretoFront[i];
			int weight = paretoFront[i+1];
			System.out.println("v:" + value + ", w:" + weight);
		}*/
	}
	
	public static int[] computeParetoNH(int[][] objects, DominanceMethod dom) {
		int[] paretoFront = new int[NDIM];
		for (int i=0; i<NDIM; i++)
			paretoFront[i] = 0;
		for (int[] o : objects) {
			int[] evals = Arrays.copyOf(paretoFront, paretoFront.length * 2);
			for (int i=0; i<paretoFront.length; i++) {
				evals[paretoFront.length+i] = evals[i] + o[i % o.length];
			}
			paretoFront = dom.getNonDominated(evals);
		}
		return paretoFront;
	}
	
	public static int[] computeParetoNHWithLookAhead(int[][] objects, DominanceMethod dom, int lookahead, int th) {
		int[] paretoFront = new int[NDIM];
		for (int i=0; i<NDIM; i++)
			paretoFront[i] = 0;
		
		int look = lookahead;
		boolean last=false;
		for (int oid =0; oid < objects.length; oid++) {
			int[] evals = Arrays.copyOf(paretoFront, paretoFront.length * 2 );
			int[] o = objects[oid];
			for (int i=0; i < paretoFront.length; i++) {
				evals[ paretoFront.length + i ] = evals[i] + o[i % o.length];
			}
			last = false;
			if (look == 0 || (look > 0 && oid % (1+look) == 0 ) || ( look == -1 && evals.length >= th) ) {
				paretoFront = dom.getNonDominated(evals);
				last = true;
			} else {
				paretoFront = evals;
			}
		}
		if (!last) {
			paretoFront = dom.getNonDominated(paretoFront);
		}
		return paretoFront;
	}
}
