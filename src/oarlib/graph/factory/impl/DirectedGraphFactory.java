package oarlib.graph.factory.impl;

import oarlib.core.Factory;
import oarlib.graph.impl.DirectedGraph;

/**
 * Simple factory class to create DirectedGraphs without reflection
 * Created by Oliver Lum on 7/26/2014.
 */
public class DirectedGraphFactory implements Factory<DirectedGraph> {
    public DirectedGraphFactory() {
    }

    @Override
    public DirectedGraph instantiate() {
        return new DirectedGraph();
    }
}
