package arl.graph.impl;

import arl.core.Arc;
import arl.core.Edge;
import arl.core.Link;
import arl.vertex.impl.MixedVertex;

/**
 * Representation of  Mixed Graph; that is, it can use both edges and arcs, in tandem with mixed vertices
 * @author Oliver
 *
 */
public class MixedGraph extends MutableGraph<MixedVertex, Link<MixedVertex>>{

	@Override
	public void addVertex(MixedVertex v) {
		getVertices().add(v);
	}

	@Override
	protected void addToNeighbors(Link<MixedVertex> e) throws IllegalArgumentException {
		if(e.getClass() == Edge.class)
		{
			
		}
		else if(e.getClass() == Arc.class)
		{
			
		}
	}

	@Override
	public void addEdge(Link<MixedVertex> e) {
		if(e.getClass() == Edge.class)
		{
			
		}
		else if(e.getClass() == Arc.class)
		{
			
		}
	}
}
