package oarlib.graph.transform;

import oarlib.core.Graph;

/**
 * Interface satisfied by any graph transformations.
 *
 * @param <S> the input type
 * @param <T> the output type
 * @author oliverlum
 */
public interface GraphTransformer<S extends Graph<?, ?>, T extends Graph<?, ?>> {
    public abstract void setGraph(S input);

    public abstract T transformGraph();
}
