package oarlib.problem.impl.rpp;

import oarlib.core.Graph;
import oarlib.graph.impl.DirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.DirectedVertex;

/**
 * The Directed Rural Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedRPP extends RuralPostmanProblem<DirectedVertex, Arc, DirectedGraph> {

    public DirectedRPP(DirectedGraph g) {
        this(g, "");
    }

    public DirectedRPP(DirectedGraph g, String name) {
        super(g, name, new SumMetric());
        mGraph = g;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.DIRECTED, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
