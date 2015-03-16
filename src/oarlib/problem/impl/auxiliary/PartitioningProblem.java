package oarlib.problem.impl.auxiliary;

import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.metrics.Metric;
import oarlib.problem.impl.ProblemAttributes;

import java.util.Collection;

/**
 * Created by oliverlum on 3/13/15.
 */
public class PartitioningProblem extends Problem {

    public PartitioningProblem(Graph graph, String name, Metric objFunc) {
        super(graph, name, objFunc);
    }

    @Override
    public boolean isFeasible(Collection collection) {
        //this is meaningless for this problem; just to ensure no checks go awry
        return true;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(null, ProblemAttributes.Type.PARTITIONING, ProblemAttributes.NumVehicles.NO_VEHICLES, ProblemAttributes.NumDepots.NO_DEPOTS, null);
    }
}
