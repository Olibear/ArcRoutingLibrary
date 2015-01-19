package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.util.Utils;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class DepotDistanceToCenter extends Metric {

    private Graph<? extends Vertex, ?> mGraph;

    public <V extends Vertex, E extends Link<V>> DepotDistanceToCenter(Graph<V, E> g) {
        mGraph = g;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {

        double meanX = 0;
        double meanY = 0;
        int n = mGraph.getVertices().size();

        for (Vertex v : mGraph.getVertices()) {
            meanX += v.getX();
            meanY += v.getY();
        }

        meanX = meanX / n;
        meanY = meanY / n;

        Vertex depot = mGraph.getVertex(mGraph.getDepotId());
        double depotDist = Utils.dist(depot.getX(), depot.getY(), meanX, meanY);

        int bestId = -1;
        double largestDist = Double.MIN_VALUE;
        double candidateDist;

        for (Vertex v : mGraph.getVertices()) {
            candidateDist = Utils.dist(v.getX(), v.getY(), meanX, meanY);
            if (candidateDist > largestDist) {
                largestDist = candidateDist;
                bestId = v.getId();
            }
        }

        return depotDist / largestDist;
    }

    @Override
    public Type getType() {
        return Type.DEPDIST;
    }

    @Override
    public String toString() {
        return "Normalized Depot Distance from Center";
    }
}
