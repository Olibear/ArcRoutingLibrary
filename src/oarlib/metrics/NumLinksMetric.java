package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class NumLinksMetric extends Metric {

    private Graph mGraph;

    public NumLinksMetric(Graph g) {
        mGraph = g;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        return mGraph.getEdges().size();
    }

    @Override
    public Type getType() {
        return Type.M;
    }

    @Override
    public String toString() {
        return "Num Links in Graph (M)";
    }
}
