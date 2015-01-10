package oarlib.objfunc;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by Oliver on 12/26/2014.
 */
public class SumObjectiveFunction extends ObjectiveFunction {

    public SumObjectiveFunction() {
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes) {
        int sum = 0;
        for (Route r : routes)
            sum += r.getCost();
        return sum;
    }
}
