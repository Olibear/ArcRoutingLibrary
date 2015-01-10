package oarlib.objfunc;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by Oliver on 12/26/2014.
 */
public class DevObjectiveFunction extends ObjectiveFunction {

    public DevObjectiveFunction() {
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int tempCost;
        for (Route r : routes) {
            tempCost = r.getCost();
            if (tempCost > max)
                max = tempCost;
            if (tempCost < min)
                min = tempCost;
        }
        return (100 * max / min) - 100;
    }
}
