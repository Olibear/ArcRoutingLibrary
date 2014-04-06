package oarlib.graph.util;


/**
 * The actual JNI call to our wrapper of Kolmogorov's Blossom V code.  We are following
 * in the spirit of the example.cpp included in the code, and providing directly the edge / weight
 * vectors instead of extracting them from a DIMACS formatted text file.
 * @param edges - edge i connects vertices edges[2i] and edges[2i+1]
 * @param weights - edge i has cost weights[i]
 * @return - say how this gets formatted.
 */
public class BlossomV {
	public native static int[] blossomV(int n, int m, int[] edges, int[] weights);
	static {
		System.loadLibrary("BlossomV");
	}

}
