package oarlib.problem.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import org.apache.log4j.Logger;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class ChinesePostmanProblem<S extends Graph<? extends Vertex, ? extends Link<? extends Vertex>>> extends Problem<S> {

    private static final Logger LOGGER = Logger.getLogger(ChinesePostmanProblem.class);

    protected ChinesePostmanProblem(S graph, String name){
        super(graph, name);
        for(Link l: graph.getEdges())
            if(!l.isRequired())
                LOGGER.warn("This problem will treat ever link as required, regardless of its status in the graph.");
    }

}
