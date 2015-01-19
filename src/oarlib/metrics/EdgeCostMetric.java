package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class EdgeCostMetric extends Metric {

    private Graph<?, ? extends Link> mGraph;

    public EdgeCostMetric(Graph<?, ? extends Link> g) {
        mGraph = g;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        double ans = 0;
        for (Link l : mGraph.getEdges())
            ans += l.getCost();
        return ans;
    }

    @Override
    public Type getType() {
        return Type.EDGECOST;
    }

    @Override
    public String toString() {
        return "Sum of Edge Costs";
    }
}
