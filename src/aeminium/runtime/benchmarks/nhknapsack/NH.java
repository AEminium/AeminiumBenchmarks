package aeminium.runtime.benchmarks.nhknapsack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


public class NH {
	
	public static final int lookahead = 0;

	public static final int NDIM = 2;

	public static final int threshold = 5000;

	public static final int lookahead_threshold = 100;
	
	public static int[][] importDataObjects(String fileName) {
        String line = null;
        int[][] arr = null;
        int size = 0;
        int i = 0;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((line = bufferedReader.readLine()) != null) {
            	if (size == 0) {
            		size = Integer.parseInt(line);
            		arr = new int[size][NDIM];
            	} else {
            		int[] o = new int[NDIM];
            		String[] parts = line.trim().replaceAll(" +", " ").split(" ");
            		o[0] = Integer.parseInt(parts[1].trim());
            		o[1] = Integer.parseInt(parts[2].trim());
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
		for (int i=0; i<paretoFront.length/NDIM; i+=NDIM) {
			int value = paretoFront[i];
			int weight = paretoFront[i+1];
			System.out.println("v:" + value + ", w:" + weight);
		}
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
		
		for (int oid =0; oid < objects.length; oid++) {
			int look = lookahead;
			if (lookahead == -1) {
				look = (paretoFront.length < th) ? 1 : 0;
			}
			int[] evals = Arrays.copyOf(paretoFront, paretoFront.length * (2 + look) );
			for (int l=0; l < (1+look) && oid+l < objects.length; l++) {
				int[] o = objects[oid+l];
				for (int i=0; i < paretoFront.length; i++) {
					evals[ paretoFront.length * (1+l) + i ] = evals[i] + o[i % o.length];
				}
			}
			paretoFront = dom.getNonDominated(evals);
		}
		return paretoFront;
	}
}
