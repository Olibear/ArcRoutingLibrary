package oarlib.improvements;

import oarlib.core.*;

import java.util.Collection;

/**
 * Created by oliverlum on 11/19/14.
 */
public abstract class InterRouteImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends ImprovementProcedure<V,E,G> {
    protected InterRouteImprovementProcedure(G g, Collection<Route<V,E>> candidateRoute) {
        super(g, candidateRoute);
    }
}
