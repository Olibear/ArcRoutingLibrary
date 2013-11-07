package oarlib.graph.impl;

import java.util.List;

import oarlib.core.Graph;
import oarlib.core.WindyEdge;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.UndirectedVertex;

public class WindyGraph extends MutableGraph<UndirectedVertex, WindyEdge>{

	@Override
	public List<WindyEdge> findEdges(Pair<UndirectedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.WINDY;
	}

}
