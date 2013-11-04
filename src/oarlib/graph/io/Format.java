package oarlib.graph.io;

/**
 * Contains supported formats and format information for conversion / export / import
 * @author Oliver
 *
 */
public class Format {
	//Names of supported formats
	public enum Name{
		/**
		 * first line is type (either "Undirected," "Windy," "Mixed" or "Directed")
		 * second line is "n m"
		 * the next m lines are "i j c" where the mth edge is from vertex i to vertex j, with cost c
		 */
		DIMACS_Modified
	}
}
