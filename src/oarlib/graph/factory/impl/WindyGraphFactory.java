package oarlib.graph.factory.impl;

import oarlib.core.Factory;
import oarlib.graph.impl.WindyGraph;

/**
 * Simple factory class to create WindyGraphs without reflection
 * Created by Oliver Lum on 7/26/2014.
 */
public class WindyGraphFactory implements Factory<WindyGraph> {
    public WindyGraphFactory() {
    }

    @Override
    public WindyGraph instantiate() {
        return new WindyGraph();
    }
}
