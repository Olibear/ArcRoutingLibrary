package oarlib.graph.transform.partition;

import oarlib.core.Graph;
import oarlib.graph.transform.GraphTransformer;

/**
 * Created by oliverlum on 8/9/14.
 */
public interface PartitionTransformer<S extends Graph<?,?>> extends GraphTransformer<S,S> {}
