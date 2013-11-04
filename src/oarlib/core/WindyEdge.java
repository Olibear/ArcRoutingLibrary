package oarlib.core;

import oarlib.graph.util.Pair;
import oarlib.vertex.impl.UndirectedVertex;
/**
 * WindyEdge class, basic class for an undirected link with asymmetric costs.
 * @author Oliver
 *
 */
public class WindyEdge extends Link<UndirectedVertex>{

	private int mReverseCost;
	public WindyEdge(String label, Pair<UndirectedVertex> endpoints, int cost, int reverseCost) {
		super(label, endpoints, cost);
		setReverseCost(reverseCost);
		setDirected(false);
	}
	public int getReverseCost() {
		return mReverseCost;
	}
	public void setReverseCost(int mReverseCost) {
		this.mReverseCost = mReverseCost;
	}

}
