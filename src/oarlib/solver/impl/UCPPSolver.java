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

		//solve min cost matching
		try {
			UndirectedGraph copy = mInstance.getGraph();
			HashMap<Integer, UndirectedVertex> indexedVertices = copy.getInternalVertexMap();
			HashMap<Integer, Edge> indexedEdges = copy.getInternalEdgeMap();

			//solve shortest paths
			int n = copy.getVertices().size();
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(copy, dist, path);


			//setup the complete graph composed entirely of the unbalanced vertices
			UndirectedGraph matchingGraph = new UndirectedGraph();

			//setup our graph of unbalanced vertices
			for (UndirectedVertex v: copy.getVertices())
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
					if(v.getId() == v2.getId())
						continue;
					matchingGraph.addEdge(new Edge("matchingEdge",new Pair<UndirectedVertex>(v,v2), dist[v.getMatchId()][v2.getMatchId()]));
				}
			}

			Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(matchingGraph);


			//add the paths to the graph
			for (Pair<UndirectedVertex> p: matchingSolution)
			{
				CommonAlgorithms.addShortestPath(copy, dist, path, new Pair<Integer>(p.getFirst().getMatchId(),p.getSecond().getMatchId()));
			}

			//return the answer
			ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
			Tour eulerTour = new Tour();
			for (int i=0;i<ans.size();i++)
			{
				eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
			}
			ArrayList<Route> ret = new ArrayList<Route>();
			ret.add(eulerTour);
			return ret;
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
		// TODO Auto-generated method stub
		return mInstance;
	}

}
