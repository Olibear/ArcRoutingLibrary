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
    private Problem<V,E,G> mProblem;
    private static final Logger LOGGER = Logger.getLogger(ImprovementProcedure.class);

    protected ImprovementProcedure(Problem<V,E,G> instance) {
        this(instance, null);
    }

    protected ImprovementProcedure(Problem<V,E,G> instance, Collection<Route<V,E>> initialSol) {

        boolean err = false;
        Collection<Route<V,E>> candidateSol;

        if(initialSol == null)
            candidateSol = instance.getSol();
        else
            candidateSol = initialSol;

        G g = instance.getGraph();

        //check preconditions
        if(candidateSol == null) {
            LOGGER.error("The solution you passed in is null; perhaps the instance has not yet been solved.");
            err = true;
        }
        if(candidateSol.size() == 0) {
            LOGGER.error("The solution you passed in is empty.");
            err = true;
        }
        if(g.getVertices().size() == 0 || g.getEdges().size() == 0) {
            LOGGER.error("The problem graph seems to be trivial, and does not permit non-empty routes.");
            err = true;
        }

        if(err)
            throw new IllegalArgumentException();

        mGraph = g;
        mInitialSol = candidateSol;
        mProblem = instance;

    }

    public abstract Problem.Type getProblemType();

    protected Collection<Route<V, E>> getInitialSol() {
        return mInitialSol;
    }

    protected G getGraph() {
        return mGraph;
    }

    protected Problem<V,E,G> getProblem() { return mProblem; }

    public abstract Collection<Route<V,E>> improveSolution();

}
