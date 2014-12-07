package oarlib.improvements;

import oarlib.core.*;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class ImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> {

    private G mGraph;
    private Collection<Route<V, E>> mInitialSol;
    private static final Logger LOGGER = Logger.getLogger(ImprovementProcedure.class);

    protected ImprovementProcedure(G g, Collection<Route<V, E>> candidateSol) {

        boolean problem = false;
        //check preconditions
        if(candidateSol == null) {
            LOGGER.error("The solution you passed in is null.");
            problem = true;
        }
        if(candidateSol.size() == 0) {
            LOGGER.error("The solution you passed in is empty.");
            problem = true;
        }
        if(g.getVertices().size() == 0 || g.getEdges().size() == 0) {
            LOGGER.error("The problem graph seems to be trivial, and does not permit non-empty routes.");
            problem = true;
        }

        if(problem)
            throw new IllegalArgumentException();

        mGraph = g;
        mInitialSol = candidateSol;
    }

    public abstract Problem.Type getProblemType();

    protected Collection<Route<V, E>> getInitialSol() {
        return mInitialSol;
    }

    protected G getGraph() {
        return mGraph;
    }

    public abstract Collection<Route<V,E>> improveSolution();

}
