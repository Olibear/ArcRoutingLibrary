package oarlib.solver.impl;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.UndirectedVertex;

public class UCPPSolver_Edmonds extends SingleVehicleSolver{

	UndirectedCPP mInstance;

	public UCPPSolver_Edmonds(UndirectedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected Route solve() {
		try {
			UndirectedGraph copy = mInstance.getGraph();
			eulerAugment(copy);

			HashMap<Integer, Edge> indexedEdges = copy.getInternalEdgeMap();
			//return the answer
			ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
			Tour eulerTour = new Tour();
			for (int i=0;i<ans.size();i++)
			{
				eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
			}
			return eulerTour;
		} catch (Exception e)
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
		return mInstance;
	}

	/**
	 * Carries out the bulk of the solve logic; it produces a least cost eulerian augmentation of the graph.
	 * @param input - the original undirected graph
	 * @return the least cost eulerian augmentation
	 */
	private static void eulerAugment(UndirectedGraph input)
	{
		try
		{
			//solve shortest paths
			int n = input.getVertices().size();
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(input, dist, path, edgePath);

			//setup the complete graph composed entirely of the unbalanced vertices
			UndirectedGraph matchingGraph = new UndirectedGraph();

			//setup our graph of unbalanced vertices
			for (UndirectedVertex v: input.getVertices())
			{
				if(v.getDegree() % 2 == 1)
				{
					matchingGraph.addVertex(new UndirectedVertex("oddVertex"), v.getId());
				}
			}

			//connect with least cost edges
			Collection<UndirectedVertex> oddVertices = matchingGraph.getVertices();
			for (UndirectedVertex v: oddVertices)
			{
				for (UndirectedVertex v2: oddVertices)
				{
					//only add one edge per pair of vertices
					if(v.getId() <= v2.getId())
						continue;
					matchingGraph.addEdge(new Edge("matchingEdge",new Pair<UndirectedVertex>(v,v2), dist[v.getMatchId()][v2.getMatchId()]));
				}
			}

			Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(matchingGraph);

			//add the paths to the graph
			for (Pair<UndirectedVertex> p: matchingSolution)
			{
				CommonAlgorithms.addShortestPath(input, dist, path, edgePath, new Pair<Integer>(p.getFirst().getMatchId(),p.getSecond().getMatchId()));
			}
			return;
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	@Override
	protected boolean checkGraphRequirements() {
		// make sure the graph is connected
		if(mInstance.getGraph() == null)
			return false;
		else
		{
			UndirectedGraph mGraph = mInstance.getGraph();
			if(!CommonAlgorithms.isConnected(mGraph))
				return false;
		}
		return true;
	}
}
