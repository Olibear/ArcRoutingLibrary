package arl.core;

import arl.graph.util.Pair;
import arl.vertex.impl.UndirectedVertex;
/**
 * Edge class, basic class for an undirected link.
 * @author Oliver
 *
 */
public class Edge extends Link<UndirectedVertex>{

	public Edge(String label, Pair<UndirectedVertex> endpoints, double cost) {
		super(label, endpoints, cost);
	}

}
