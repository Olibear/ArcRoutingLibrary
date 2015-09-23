package oarlib.problem.impl.rpp;

import oarlib.core.Graph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.link.impl.ZigZagLink;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.ZigZagVertex;

/**
 * Created by oliverlum on 6/20/15.
 */
public class WindyRPPZZ extends RuralPostmanProblem<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    public WindyRPPZZ(ZigZagGraph graph, String name) {
        super(graph, name, new SumMetric());
        mGraph = graph;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
