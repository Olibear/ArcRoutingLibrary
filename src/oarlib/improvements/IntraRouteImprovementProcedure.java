package oarlib.improvements;

import oarlib.core.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class IntraRouteImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends ImprovementProcedure<V,E,G> {
    protected IntraRouteImprovementProcedure(G g, Collection<Route<V, E>> candidateRoute) {
        super(g, candidateRoute);
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
