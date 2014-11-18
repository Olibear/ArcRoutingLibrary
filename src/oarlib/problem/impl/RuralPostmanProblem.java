package oarlib.problem.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import org.apache.log4j.Logger;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class RuralPostmanProblem<S extends Graph<? extends Vertex, ? extends Link<? extends Vertex>>> extends Problem<S> {

    private static final Logger LOGGER = Logger.getLogger(RuralPostmanProblem.class);

    protected RuralPostmanProblem(S graph, String name) {
        super(graph, name);
        boolean isCpp = true;
        for(Link l: graph.getEdges())
            if(!l.isRequired())
                isCpp = false;
        if(isCpp)
            LOGGER.warn("It appears as though every link in this graph is required.  Consider running a Chinese Postman solver.");
    }
}
