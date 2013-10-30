package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.exceptions.SetupException;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.UndirectedVertex;

public class UCPPSolver extends Solver{
	
	UndirectedCPP mInstance;

	public UCPPSolver(UndirectedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected Collection<Route> solve() {
		
		UndirectedGraph copy = mInstance.getGraph();
		HashMap<Integer, UndirectedVertex> indexedVertices = copy.getInternalVertexMap();
		HashMap<Integer, Edge> indexedEdges = copy.getInternalEdgeMap();
		LinkedHashSet<UndirectedVertex> unbalanced = new LinkedHashSet<UndirectedVertex>();
		
		
		//setup our unbalanced vertices
		for (UndirectedVertex v: copy.getVertices())
		{
			if(v.getDegree() % 2 == 1)
			{
				unbalanced.add(v);
			}
		}
		
		//solve min cost matching
		try {
			Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(copy);
			
			
			//add the paths to the graph
			
			//return the answer
			int[] ans = CommonAlgorithms.tryFleury(copy);
			Tour eulerTour = new Tour();
			for (int i=0;i<ans.length;i++)
			{
				eulerTour.appendEdge(indexedEdges.get(ans[i]));
			}
			ArrayList<Route> ret = new ArrayList<Route>();
			ret.add(eulerTour);
			return ret;
		} catch (SetupException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
	}

	@Override
	protected UndirectedCPP getInstance() {
		// TODO Auto-generated method stub
		return mInstance;
	}

}
