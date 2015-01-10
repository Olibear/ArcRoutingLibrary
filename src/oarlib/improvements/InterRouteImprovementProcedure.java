package oarlib.improvements;

import oarlib.core.*;

import java.util.Collection;

/**
 * Created by oliverlum on 11/19/14.
 */
public abstract class InterRouteImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends ImprovementProcedure<V,E,G> {
    protected InterRouteImprovementProcedure(Problem<V,E,G> problem) {
        super(problem);
    }

    protected InterRouteImprovementProcedure(Problem<V, E, G> problem, ImprovementStrategy.Type strat, Collection<Route<V, E>> initialSol) {
        super(problem, strat, initialSol);
    }
}
