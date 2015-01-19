package oarlib.metrics;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class VarMetric extends Metric {

    public VarMetric() {
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        MaxMetric max = new MaxMetric();
        MinMetric min = new MinMetric();

        double maxVal = max.evaluate(routes);
        double minVal = min.evaluate(routes);

        return 100.0 * ((maxVal - minVal) / maxVal);
    }

    @Override
    public Type getType() {
        return Type.VAR;
    }

    @Override
    public String toString() {
        return "Max-Min Variance";
    }
}
