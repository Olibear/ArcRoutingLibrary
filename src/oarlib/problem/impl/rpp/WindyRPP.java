package oarlib.problem.impl.rpp;

import oarlib.core.Graph;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.WindyVertex;

public class WindyRPP extends RuralPostmanProblem<WindyVertex, WindyEdge, WindyGraph> {

    public WindyRPP(WindyGraph g) {
        this(g, "");
    }

    public WindyRPP(WindyGraph g, String name) {
        super(g, name, new SumMetric());
        mGraph = g;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
