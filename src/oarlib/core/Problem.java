package oarlib.core;

import java.util.Collection;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * If you wish to solve a capacitated problem, please see CapacitatedProblem.
 *
 * @author oliverlum
 */
public abstract class Problem<S extends Graph<? extends Vertex, ? extends Link<? extends Vertex>>> {

    protected String mName = "";
    protected S mGraph;

    protected Problem(S graph, String name) {
        mName = name;
        mGraph = graph;
    }

    public String getName() {
        return mName;
    }

    /**
     * Retrieve the graph that has been associated with this Problem
     *
     * @return the graph
     */
    public S getGraph() {
        return mGraph;
    }

    /**
     * @return - The type of graph that this problem operates over.
     */
    public Graph.Type getGraphType() {
        return mGraph.getType();
    }

    /**
     * Says whether the provided set of routes is a feasible solution
     *
     * @return true if problem instance is solvable; false oth.
     */
    public abstract boolean isFeasible(Collection<Route> routes);


    /**
     * @return - The type of problem that this represents.
     */
    public abstract Type getProblemType();

    public enum Type {
        DIRECTED_CHINESE_POSTMAN,
        UNDIRECTED_CHINESE_POSTMAN,
        MIXED_CHINESE_POSTMAN,
        WINDY_CHINESE_POSTMAN,
        DIRECTED_RURAL_POSTMAN,
        WINDY_RURAL_POSTMAN
    }
}
