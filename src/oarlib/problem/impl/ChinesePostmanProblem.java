package oarlib.problem.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import oarlib.objfunc.ObjectiveFunction;
import org.apache.log4j.Logger;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class ChinesePostmanProblem<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends Problem<V,E,G> {

    private static final Logger LOGGER = Logger.getLogger(ChinesePostmanProblem.class);

    protected ChinesePostmanProblem(G graph, String name, ObjectiveFunction objFunc) {
        super(graph, name, objFunc);
        for(Link l: graph.getEdges())
            if(!l.isRequired())
                LOGGER.warn("This problem will treat ever link as required, regardless of its status in the graph.");
    }

}
