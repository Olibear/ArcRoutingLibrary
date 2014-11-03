package oarlib.core;

import java.util.Collection;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * If you wish to solve a capacitated problem, please see CapacitatedProblem.
 *
 * @author oliverlum
 */
public abstract class Problem {

    protected String mName = "";

    protected Problem(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    /**
     * Says whether the provided set of routes is a feasible solution
     *
     * @return true if problem instance is solvable; false oth.
     */
    public abstract boolean isFeasible(Collection<Route> routes);

    /**
     * Retrieve the graph that has been associated with this Problem
     *
     * @return the graph
     */
    public abstract Graph<?, ?> getGraph();

    /**
     * @return - The type of problem that this represents.
     */
    public abstract Type getType();

    public enum Type {
        DIRECTED_CHINESE_POSTMAN,
        UNDIRECTED_CHINESE_POSTMAN,
        MIXED_CHINESE_POSTMAN,
        WINDY_CHINESE_POSTMAN,
        DIRECTED_RURAL_POSTMAN,
        WINDY_RURAL_POSTMAN
    }
}
