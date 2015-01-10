package oarlib.core;

import oarlib.objfunc.ObjectiveFunction;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * If you wish to solve a capacitated problem, please see CapacitatedProblem.
 *
 * @author oliverlum
 */
public abstract class Problem<V extends Vertex, E extends Link<V>, G extends Graph<V,E>>{

    private static final Logger LOGGER = Logger.getLogger(Problem.class);

    protected String mName = "";
    protected G mGraph;
    protected Collection<Route<V,E>> mSol;
    protected boolean solved;
    protected ObjectiveFunction mObjFunc;

    protected Problem(G graph, String name, ObjectiveFunction objFunc) {
        mName = name;
        mGraph = graph;
        solved = false;
        mObjFunc = objFunc;
    }

    public String getName() {
        return mName;
    }

    /**
     * Retrieve the graph that has been associated with this Problem
     *
     * @return the graph
     */
    public G getGraph() {
        return mGraph;
    }

    /**
     * @return - The type of graph that this problem operates over.
     */
    public Graph.Type getGraphType() {
        return mGraph.getType();
    }

    public Collection<Route<V,E>> getSol(){
        if(!solved) {
            LOGGER.error("No solution has been set for this problem yet.");
        }
        return mSol;
    }

    public void setSol(Route<V,E> newSol) {
        ArrayList<Route<V,E>> container = new ArrayList<Route<V,E>>();
        container.add(newSol);
        mSol = container;
        solved = true;
    }

    public void setSol(Collection<Route<V, E>> newSol) {
        mSol = newSol;
        solved = true;
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

    /**
     * @return - The
     */
    public ObjectiveFunction getObjectiveFunction() {
        return mObjFunc;
    }

    ;

    public enum Type {
        DIRECTED_CHINESE_POSTMAN,
        UNDIRECTED_CHINESE_POSTMAN,
        MIXED_CHINESE_POSTMAN,
        WINDY_CHINESE_POSTMAN,
        DIRECTED_RURAL_POSTMAN,
        WINDY_RURAL_POSTMAN
    }
}
