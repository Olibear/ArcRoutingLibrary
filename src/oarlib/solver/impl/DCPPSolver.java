package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.problem.impl.DirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;

public class DCPPSolver extends Solver{
	
	DirectedCPP mInstance;

	public DCPPSolver(DirectedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected Collection<Route> solve() {

		DirectedGraph copy = mInstance.getGraph();
		HashMap<Integer, DirectedVertex> indexedVertices = copy.getInternalVertexMap();
		HashMap<Integer, Arc> indexedArcs = copy.getInternalEdgeMap();

		LinkedHashSet<DirectedVertex> Dplus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dminus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dall = new LinkedHashSet<DirectedVertex>();

		//prepare our unbalanced vertex sets
		for(DirectedVertex v: copy.getVertices())
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
		try {
			int[][] flowanswer = CommonAlgorithms.minCostNetworkFlow(copy);

			//add the solution to the graph (augment)
			for (int i=0; i<flowanswer.length;i++)
			{
				if(flowanswer[i][3] == 0)
					continue;
				for (int j=0; j<flowanswer[i][3];j++)
				{
					copy.addEdge(new Arc("duped arc", 
							new Pair<DirectedVertex>(indexedVertices.get(flowanswer[i][0]), indexedVertices.get(flowanswer[i][1])),
							flowanswer[i][2]));
				}
			}


			// return the answer
			int[] ans = CommonAlgorithms.tryFleury(copy);
			Tour eulerTour = new Tour();
			for(int i=1; i<ans.length;i++)
			{
				eulerTour.appendEdge(indexedArcs.get(ans[i]));
			}
			ArrayList<Route> ret = new ArrayList<Route>();
			ret.add(eulerTour);
			return ret;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

	@Override
	protected Problem getInstance() {
		return mInstance;
	}

}
