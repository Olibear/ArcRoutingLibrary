package oarlib.graph.util;



public class BlossomV {
	/**
	 * The actual JNI call to our wrapper of Kolmogorov's Blossom V code.  We are following
	 * in the spirit of the example.cpp included in the code, and providing directly the edge / weight
	 * vectors instead of extracting them from a DIMACS formatted text file.
	 * @param edges - edge i connects vertices edges[2i] and edges[2i+1]
	 * @param weights - edge i has cost weights[i]
	 * @return - an integer array where the index + 1 is half of a matched-pair, 
	 * (e.g. if the first entry is 3, then 1-3 is in the matching).
	 */
	public native static int[] blossomV(int n, int m, int[] edges, int[] weights);
	static {
		System.loadLibrary("BlossomV");
	}

}
