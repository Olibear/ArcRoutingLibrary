package oarlib.graph.graphgen.erdosrenyi;

import oarlib.core.Graph;
import org.apache.log4j.Logger;

import java.util.Random;


public abstract class ErdosRenyiGraphGenerator<S extends Graph> {

    private static final Logger LOGGER = Logger.getLogger(ErdosRenyiGraphGenerator.class);

    private double defaultDensity;

    public ErdosRenyiGraphGenerator() {
        Random rng = new Random();
        defaultDensity = rng.nextDouble();
    }

    /**
     * Randomly generates an Erdos-Renyi like graph with n vertices (if connectedness or Eulerianness is enforced
     * then the process is not necessarily exactly according to Erdos-Renyi).
     *
     * @param n - number of vertices in the graph
     * @return - a graph with n vertices.
     */
    public S generateGraph(int n) {
        return this.generateGraph(n, 10, false, defaultDensity);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     *
     * @param n       - number of vertices in the graph
     * @param maxCost - the maximum (magnitude) that the cost of a link in this graph can have
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost) {
        return this.generateGraph(n, maxCost, false, defaultDensity);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     *
     * @param n         - number of vertices in the graph
     * @param maxCost   - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected - flag to enforce (strong) connectedness on the graph
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, boolean connected) {
        return this.generateGraph(n, maxCost, connected, -1);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     * The density specified will be the probability that a link from vertex i to vertex j is included.
     *
     * @param n         - number of vertices in the graph
     * @param maxCost   - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected - flag to enforce (strong) connectedness on the graph
     * @param density   - the approximate density of the graph, must be between [0,1]
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, boolean connected, double density) {
        return this.generateGraph(n, maxCost, connected, density, false);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     * The density specified will be the probability that a link from vertex i to vertex j is included.
     *
     * @param n             - number of vertices in the graph
     * @param maxCost       - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected     - flag to enforce (strong) connectedness on the graph
     * @param density       - the approximate density of the graph, must be between [0,1]
     * @param positiveCosts - flag to enforce whether all edge weights are strictly positive.
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, boolean connected, double density, boolean positiveCosts) {
        return this.generateGraph(n, maxCost, connected, density, 1, positiveCosts);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     * The density specified will be the probability that a link from vertex i to vertex j is included
     * (similarly for reqDensity).
     *
     * @param n             - number of vertices in the graph
     * @param maxCost       - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected     - flag to enforce (strong) connectedness on the graph
     * @param density       - the approximate density of the graph, must be between [0,1]
     * @param reqDensity    - the probability that a link will be required in the graph.
     * @param positiveCosts - flag to enforce whether all edge weights are strictly positive.
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, boolean connected, double density, double reqDensity, boolean positiveCosts) {
        checkArgs(n, maxCost, density);
        return this.generate(n, maxCost, connected, density, reqDensity, positiveCosts);
    }

    protected abstract S generate(int n, int maxCost, boolean connected, double density, double reqDensity, boolean positiveCosts);

    /**
     * Randomly generates an Eulerian graph with n vertices.
     *
     * @param n - number of vertices in the graph
     * @return - a graph with n vertices.
     */
    public S generateEulerianGraph(int n) {
        return this.generateEulerianGraph(n, 10, false, defaultDensity);
    }

    /**
     * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
     *
     * @param n       - number of vertices in the graph
     * @param maxCost - the maximum (magnitude) that the cost of a link in this graph can have
     * @return - a graph with the specified properties.
     */
    public S generateEulerianGraph(int n, int maxCost) {
        return this.generateEulerianGraph(n, maxCost, false, defaultDensity);
    }

    /**
     * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     *
     * @param n         - number of vertices in the graph
     * @param maxCost   - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected - flag to enforce (strong) connectedness on the graph
     * @return - a graph with the specified properties.
     */
    public S generateEulerianGraph(int n, int maxCost, boolean connected) {
        return this.generateEulerianGraph(n, maxCost, connected, defaultDensity);
    }

    /**
     * Randomly generates an Eulerian graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     * The density specified will be the probability that a link from vertex i to vertex j is included.
     *
     * @param n         - number of vertices in the graph
     * @param maxCost   - the maximum (magnitude) that the cost of a link in this graph can have
     * @param connected - flag to enforce (strong) connectedness on the graph
     * @param density   - the approximate density of the graph, must be between [0,1]
     * @return - a graph with the specified properties.
     */
    public S generateEulerianGraph(int n, int maxCost, boolean connected, double density) {
        checkArgs(n, maxCost, density);
        return this.generateEulerian(n, maxCost, connected, density);
    }

    protected abstract S generateEulerian(int n, int maxCost, boolean connected, double density);

    /**
     * Check inputs to the graph generator methods
     *
     * @param n       - number of vertices in the graph
     * @param maxCost - the maximum (magnitude) that the cost of a link in this graph can have
     * @param density - the approximate density of the graph, must be between [0,1]
     */
    private static void checkArgs(int n, int maxCost, double density) {
        boolean problem = false;
        if (n < 0) {
            LOGGER.error("Cannot create a graph with negative number of vertices.");
            problem = true;
        }
        if (density < 0 || density > 1) {
            LOGGER.error("Invalid density.");
            problem = true;
        }
        if (maxCost < 0) {
            LOGGER.error("maxCost is a cardinality argument; it should not be signed.");
            problem = true;
        }
        if (problem)
            throw new IllegalArgumentException();
    }
}
