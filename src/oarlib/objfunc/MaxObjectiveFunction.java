package oarlib.objfunc;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by Oliver on 12/26/2014.
 */
public class MaxObjectiveFunction extends ObjectiveFunction {

    public MaxObjectiveFunction() {
    }

    ;

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes) {
        int max = Integer.MIN_VALUE;
        for (Route r : routes)
            if (r.getCost() > max)
                max = r.getCost();
        return max;
    }
}
