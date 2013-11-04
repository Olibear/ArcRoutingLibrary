package oarlib.graph.graphgen;

import oarlib.core.Graph;

public abstract class GraphGenerator {
	public GraphGenerator(){};
	/**
	 * Randomly generates a graph with n vertices.
	 * @param n - number of vertices in the graph
	 * @return - a graph with n vertices.
	 */
	public abstract Graph<?,?> generateGraph(int n);
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @return - a graph with the specified properties.
	 */
	public abstract Graph<?,?> generateGraph(int n, int maxCost);
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @return - a graph with the specified properties.
	 */
	public abstract Graph<?,?> generateGraph(int n, int maxCost, boolean connected);
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * The density specified will be the probability that a link from vertex i to vertex j is included.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @param density - the approximate density of the graph, must be between [0,1], but connected will override this,
	 * so that a true connected flag will have at least the number of links required to make the graph connected
	 * @return - a graph with the specified properties.
	 */
	public abstract Graph<?,?> generateGraph(int n, int maxCost, boolean connected, double density);
}
