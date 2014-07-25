package oarlib.graph.graphgen;

import oarlib.core.Graph;

public abstract class GraphGenerator {
	public GraphGenerator(){};
	/**
	 * Randomly generates an Erdos-Renyi like graph with n vertices (if connectedness or Eulerianness is enforced
	 * then the process is not necessarily exactly according to Erdos-Renyi).
	 * @param n - number of vertices in the graph
	 * @return - a graph with n vertices.
	 */
	public Graph<?,?> generateGraph(int n)
	{
		return this.generateGraph(n, -1, false, -1);
	}
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have.  If this is set to a negative number, it is ignored.
	 * @return - a graph with the specified properties.
	 */
	public Graph<?,?> generateGraph(int n, int maxCost)
	{
		return this.generateGraph(n, maxCost, false, -1);
	}
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have. If this is set to a negative number, it is ignored.
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @return - a graph with the specified properties.
	 */
	public Graph<?,?> generateGraph(int n, int maxCost, boolean connected)
	{
		return this.generateGraph(n, maxCost, connected, -1);
	}
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * The density specified will be the probability that a link from vertex i to vertex j is included.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @param density - the approximate density of the graph, must be between [0,1], but connected will override this,
	 * so that a true connected flag will have at least the number of links required to make the graph connected.  If this
	 * is set to something outside of the range [0,1], then it is ignored.
	 * @return - a graph with the specified properties.
	 */
	public Graph<?,?> generateGraph(int n, int maxCost, boolean connected, double density)
	{
		return this.generateGraph(n, maxCost, connected, density, false);
	}
	/**
	 * Randomly generates a graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * The density specified will be the probability that a link from vertex i to vertex j is included.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @param density - the approximate density of the graph, must be between [0,1], but connected will override this,
	 * so that a true connected flag will have at least the number of links required to make the graph connected.  If this
	 * is set to something outside of the range [0,1], then it is ignored.
	 * @param positiveCosts - flag to enforce whether all edge weights are strictly positive.
	 * @return - a graph with the specified properties.
	 */
	public abstract Graph<?,?> generateGraph(int n, int maxCost, boolean connected, double density, boolean positiveCosts);
	/**
	 * Randomly generates an Eulerian graph with n vertices.
	 * @param n - number of vertices in the graph
	 * @return - a graph with n vertices.
	 */
	public Graph<?,?> generateEulerianGraph(int n)
	{
		return this.generateEulerianGraph(n, -1, false, -1);
	}
	/**
	 * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have.  If this is set to a negative number, it is ignored.
	 * @return - a graph with the specified properties.
	 */
	public Graph<?,?> generateEulerianGraph(int n, int maxCost)
	{
		return this.generateEulerianGraph(n, maxCost, false, -1);
	}
	/**
	 * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have. If this is set to a negative number, it is ignored.
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @return - a graph with the specified properties.
	 */
	public Graph<?,?> generateEulerianGraph(int n, int maxCost, boolean connected)
	{
		return this.generateEulerianGraph(n, maxCost, connected, -1);
	}
	/**
	 * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
	 * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
	 * The density specified will be the probability that a link from vertex i to vertex j is included.
	 * @param n - number of vertices in the graph
	 * @param maxCost - the maximum cost that a link in this graph can have
	 * @param connected - flag to enforce (strong) connectedness on the graph
	 * @param density - the approximate density of the graph, must be between [0,1], but connected will override this,
	 * so that a true connected flag will have at least the number of links required to make the graph connected.  If this
	 * is set to something outside of the range [0,1], then it is ignored.
	 * @return - a graph with the specified properties.
	 */
	public abstract Graph<?,?> generateEulerianGraph(int n, int maxCost, boolean connected, double density);
}
