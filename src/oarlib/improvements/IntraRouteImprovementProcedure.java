package oarlib.improvements;

import oarlib.core.Problem;
import oarlib.core.Route;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class IntraRouteImprovementProcedure<S extends Problem> extends ImprovementProcedure<S> {
    protected IntraRouteImprovementProcedure(S p, Collection<Route> candidateRoute) {
        super(p, candidateRoute);
    }
    @Override
    public Collection<Route> improveSolution() {
        HashSet<Route> ans = new HashSet<Route>();
        for(Route r: getInitialSol())
            ans.add(improveRoute(r));
        return ans;
    }
    protected abstract Route improveRoute(Route r);
}
