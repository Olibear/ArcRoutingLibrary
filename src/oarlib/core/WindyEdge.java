package oarlib.core;

import oarlib.graph.util.Pair;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;
/**
 * WindyEdge class, basic class for an undirected link with asymmetric costs.
 * @author Oliver
 *
 */
public class WindyEdge extends Link<WindyVertex>{

	private int mReverseCost;
	public WindyEdge(String label, Pair<WindyVertex> endpoints, int cost, int reverseCost) {
		super(label, endpoints, cost);
		setReverseCost(reverseCost);
		setDirected(false);
	}
	public WindyEdge(String label, Pair<WindyVertex> endpoints, int cost, int reverseCost, boolean required)
	{
		super(label, endpoints, cost, required);
		setReverseCost(reverseCost);
		setDirected(false);
	}
	public int getReverseCost() {
		return mReverseCost;
	}
	public void setReverseCost(int mReverseCost) {
		this.mReverseCost = mReverseCost;
	}
	@Override
	public WindyEdge getCopy() {
		return new WindyEdge("copy", this.getEndpoints(), this.getCost(), this.getReverseCost(), this.isRequired());
	}

}
