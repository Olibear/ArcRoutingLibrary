package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

		eulerAugment(copy);

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
	}

	private static void eulerAugment(DirectedGraph input)
	{
		//prepare our unbalanced vertex sets
		for(DirectedVertex v: input.getVertices())
		{
			if(v.getDelta() != 0)
			{
				v.setDemand(v.getDelta());
			}
		}
		try {
			if(!CommonAlgorithms.isEulerian(input))
			{
				int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(input);

				HashMap<Integer, Arc> indexedArcs = input.getInternalEdgeMap();
				Arc temp;
				//add the solution to the graph (augment)
				for(int i = 1; i < flowanswer.length; i++)
				{
					temp = indexedArcs.get(i);
					for(int j = 0; j < flowanswer[i]; j++)
					{
						input.addEdge(new Arc("added from flow", temp.getEndpoints(), temp.getCost()));
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
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
