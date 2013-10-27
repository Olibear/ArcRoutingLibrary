package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import oarlib.graph.util.CommonAlgorithms;;
import oarlib.graph.util.Pair;
import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.problem.impl.DirectedCPP;
import oarlib.vertex.impl.DirectedVertex;

public class DCPPSolver extends Solver{
	
	DirectedGraph<Arc> mGraph;

	public DCPPSolver(DirectedCPP instance) throws IllegalArgumentException {
		super(instance);
	}

	@Override
	protected Collection<Route> solve() {
		
		DirectedGraph<Arc> copy = mGraph;
		
		LinkedHashSet<DirectedVertex> Dplus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dminus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dall = new LinkedHashSet<DirectedVertex>();
		
		//prepare our unbalanced vertex sets
		for(DirectedVertex v: mGraph.getVertices())
		{
			if(v.getDelta() > 0)
			{
				Dplus.add(v);
				Dall.add(v);
				v.setDemand(v.getDelta());
			}
			if(v.getDelta() < 0)
			{
				Dminus.add(v);
				Dall.add(v);
				v.setDemand(v.getDelta());
			}
		}
		
		//min cost flow
		int[][] flowanswer = CommonAlgorithms.minCostNetworkFlow(mGraph);
		
		//add the solution to the graph (augment)
		for (int i=0; i<flowanswer.length;i++)
		{
			if(flowanswer[i][3] == 0)
				continue;
			for (int j=0; j<flowanswer[i][3];j++)
			{
				//TODO: need to be able to fetch vertex from guid
				copy.addEdge(new Arc("duped arc",new Pair<DirectedVertex>(),flowanswer[i][2]));
			}
		}
		
		
		// return the answer
		Route ans = CommonAlgorithms.tryFleury(copy);
		ArrayList<Route> ret = new ArrayList<Route>();
		ret.add(ans);
		return ret;
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

}
