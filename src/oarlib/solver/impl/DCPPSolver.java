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
		HashMap<Integer, Arc> indexedArcs = copy.getInternalEdgeMap();

		//prepare our unbalanced vertex sets
		for(DirectedVertex v: copy.getVertices())
		{
			if(v.getDelta() != 0)
			{
				v.setDemand(v.getDelta());
			}
		}

		//min cost flow
		try {
			if(!CommonAlgorithms.isEulerian(copy))
			{
				int n = copy.getVertices().size();
				int [][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(copy,dist,path);
				HashMap<Pair<Integer>, Integer> flowanswer = CommonAlgorithms.cycleCancelingMinCostNetworkFlow(copy, dist);

				//add the solution to the graph (augment)
				for (Pair<Integer> p: flowanswer.keySet())
				{
					for(int i = 0; i < flowanswer.get(p); i++)
						CommonAlgorithms.addShortestPath(copy, dist, path, p);
				}
			}


			// return the answer
			ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
			Tour eulerTour = new Tour();
			for(int i=0; i<ans.size();i++)
			{
				eulerTour.appendEdge(indexedArcs.get(ans.get(i)));
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
	protected DirectedCPP getInstance() {
		return mInstance;
	}

}
