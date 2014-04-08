package oarlib.graph.util;

public class MSArbor {

	/**
	 * The actual JNI call to the shortest spanning arborescence C++ code MSArbor.
	 * We are following in the spirit of the msa15 example included with the code,
	 * and providing directly the edge weights instead of extracting them from a
	 * formatted text file.
	 * @param n - the number of nodes
	 * @param m - the number of edges
	 * @param weights - the associated edge costs; the graph is assumed to be complete, and
	 * the weights are ordered according to the source vertex, 
	 * 
	 * (e.g. if there are 5 nodes, then they are numbered 0,1,2,3,4 and:
	 *   
	 *   weights = ["0",0-1,0-2,0-3,1-0,"0",1-2,1-3...4-2,4-3]
	 *   
	 *   Notice that we do not include weights going into the final vertex because this
	 *   is assumed to be the root of the spanning arborescence.
	 *   ).
	 * @return
	 */
	public native static int[] msArbor(int n, int m, int[] weights);
	static {
		System.loadLibrary("MSArbor");
	}
}
