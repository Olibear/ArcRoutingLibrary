package oarlib.graph.factory.impl;

import oarlib.core.Factory;
import oarlib.graph.impl.MixedGraph;

/**
 * Simple factory class to create MixedGraphs without reflection
 * Created by Oliver Lum on 7/26/2014.
 */
public class MixedGraphFactory implements Factory<MixedGraph> {
    public MixedGraphFactory() {
    }

    @Override
    public MixedGraph instantiate() {
        return new MixedGraph();
    }
}
