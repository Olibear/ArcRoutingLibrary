package oarlib.improvements;

import oarlib.core.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class IntraRouteImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends ImprovementProcedure<V,E,G> {
    protected IntraRouteImprovementProcedure(Problem<V,E,G> problem) {
        super(problem);
    }

    protected IntraRouteImprovementProcedure(Problem<V, E, G> problem, ImprovementStrategy.Type strat, Collection<Route<V, E>> initialSol) {
        super(problem, strat, initialSol);
    }
    @Override
    public final Collection<Route<V, E>> improveSolution() {
        HashSet<Route<V,E>> ans = new HashSet<Route<V,E>>();
        for(Route r: getInitialSol())
            ans.add(improveRoute(r));
        return ans;
    }
    public abstract Route<V,E> improveRoute(Route<V,E> r);
}
