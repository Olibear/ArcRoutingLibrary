package oarlib.improvements;

import oarlib.core.Problem;
import oarlib.core.Route;

import java.util.Collection;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class ImprovementProcedure<S extends Problem> {

    private S mProblem;
    private Collection<Route> mInitialSol;

    protected ImprovementProcedure(S p, Collection<Route> candidateRoute) {
        mProblem = p;
        mInitialSol = candidateRoute;
    }

    protected S getProblem() {
        return mProblem;
    }

    protected Collection<Route> getInitialSol() {
        return mInitialSol;
    }

    public abstract Collection<Route> improveSolution();

}
