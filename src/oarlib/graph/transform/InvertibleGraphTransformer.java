package oarlib.graph.transform;

import oarlib.core.Graph;

/**
 * Interface which invertible graph transformers must satisfy.  That is, any
 * graph transformers for which it is possible to reverse; it is recommended that
 * any 1-1 mappings implement this interface instead of implementing two different
 * transforms.
 * @author oliverlum
 *
 * @param <S>
 * @param <T>
 */
public interface InvertibleGraphTransformer<S extends Graph<?,?>, T extends Graph<?,?>> extends GraphTransformer<S,T> {
	public S invertTransformation(T input);
}
