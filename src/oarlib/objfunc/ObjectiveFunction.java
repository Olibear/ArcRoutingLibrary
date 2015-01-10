package oarlib.objfunc;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 12/10/14.
 */
public abstract class ObjectiveFunction {
    public abstract <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes);
}
