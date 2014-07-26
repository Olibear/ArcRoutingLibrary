package oarlib.graph.factory.impl;

import oarlib.core.Factory;
import oarlib.graph.impl.UndirectedGraph;

/**
 * Simple factory for creating UndirectedGraphs without using reflection
 * Created by Oliver Lum on 7/26/2014.
 */
public class UndirectedGraphFactory implements Factory<UndirectedGraph> {
    public UndirectedGraphFactory(){};
    @Override
    public UndirectedGraph instantiate() {
        return new UndirectedGraph();
    }
}
