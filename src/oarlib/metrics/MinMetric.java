package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class MinMetric extends Metric {

    private Graph mGraph;

    public MinMetric() {
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        int min = Integer.MAX_VALUE;
        for (Route r : routes)
            if (r.getCost() < min)
                min = r.getCost();
        return min;
    }

    @Override
    public Type getType() {
        return Type.MIN;
    }

    @Override
    public String toString() {
        return "Min Route Length";
    }
}
