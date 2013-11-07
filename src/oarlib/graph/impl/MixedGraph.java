package oarlib.graph.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.graph.util.UnmatchedPair;
import oarlib.vertex.impl.MixedVertex;

/**
 * Representation of  Mixed Graph; that is, it can use both edges and arcs, in tandem with mixed vertices
 * @author Oliver
 *
 */
public class MixedGraph extends MutableGraph<MixedVertex, Link<MixedVertex>>{

	
	//constructors
	public MixedGraph(){
		super();
	}
	
	@Override
	public void addVertex(MixedVertex v) {
		getVertices().add(v);
	}
	
	@Override
	public void addEdge(E e) {
		
	}

	@Override
	public Collection<Link<MixedVertex>> findEdges(Pair<MixedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.MIXED;
	}
}
