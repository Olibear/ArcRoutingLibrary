package oarlib.solver.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.MixedEdge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.MixedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class MCPPSolver extends Solver{

	MixedCPP mInstance;

	public MCPPSolver(MixedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	/**
	 * Implements Frederickson's heuristic for the mixed CPP.
	 */
	@Override
	protected Collection<Route> solve() {

		MixedGraph copy = mInstance.getGraph();

		//Start Even-Symmetric-Even (Mixed 1)
		//Even
		evenDegree(copy);

		//Symmetric
		HashSet<MixedEdge> U = new HashSet<MixedEdge>();
		HashSet<MixedEdge> M = new HashSet<MixedEdge>();
		inOutDegree(copy, U, M);

		//Even

		//Start Large Cycles - Even (Mixed 2)

		//select the lower cost of the two
		return null;
	}

	/**
	 * Essentially solves the DCPP on the Mixed Graph, as in Mixed 1 of Frederickson.
	 * @param input - a mixed graph, which is augmented with the flow solution
	 */
	private void inOutDegree(MixedGraph input, HashSet<MixedEdge> U, HashSet<MixedEdge> M)
	{
		try {
			DirectedGraph setup = new DirectedGraph();
			for(MixedVertex v: input.getVertices())
			{
				setup.addVertex(new DirectedVertex("symmetric setup graph"), v.getId());
			}
			HashMap<Integer, DirectedVertex> matchedMap = setup.getMatchingVertexMap();
			for (MixedEdge e: input.getEdges())
			{
				if(e.isDirected())
				{
					setup.addEdge(new Arc("symmetric setup graph", new Pair<DirectedVertex>(matchedMap.get(e.getTail().getId()), matchedMap.get(e.getHead().getId())), e.getCost()), e.getId());
				}
				else
				{
					setup.addEdge(new Arc("symmetric setup graph", new Pair<DirectedVertex>(matchedMap.get(e.getTail().getId()), matchedMap.get(e.getHead().getId())), e.getCost()), e.getId());
					setup.addEdge(new Arc("symmetric setup graph", new Pair<DirectedVertex>(matchedMap.get(e.getHead().getId()), matchedMap.get(e.getTail().getId())), e.getCost()), e.getId());
				}
			}

		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	/**
	 * Essentially solves the UCPP on the Mixed Graph, ignoring arc direction,
	 * as in Mixed 1 of Frederickson.
	 * @param input - a mixed graph, which is augmented with the solution to the matching.
	 */
	private void evenDegree(MixedGraph input)
	{
		try {
			//set up the undirected graph, and then solve the min cost matching
			UndirectedGraph setup = new UndirectedGraph();
			for(MixedVertex v: input.getVertices())
			{
				setup.addVertex(new UndirectedVertex("even setup graph"), v.getId());
			}
			HashMap<Integer, UndirectedVertex> matchedMap = setup.getMatchingVertexMap();
			for(MixedEdge e:input.getEdges())
			{
				setup.addEdge(new Edge("even setup graph", new Pair<UndirectedVertex>(matchedMap.get(e.getTail().getId()), matchedMap.get(e.getHead().getId())), e.getCost()), e.getId());
			}

			//solve shortest paths
			int n = setup.getVertices().size();
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(setup, dist, path, edgePath);

			//setup the complete graph composed entirely of the unbalanced vertices
			UndirectedGraph matchingGraph = new UndirectedGraph();

			//setup our graph of unbalanced vertices
			for (UndirectedVertex v: setup.getVertices())
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


			//now add copies in the mixed graph
			MixedEdge e;
			HashMap<Integer, UndirectedVertex> setupVertices = setup.getInternalVertexMap();
			for(Pair<UndirectedVertex> p : matchingSolution)
			{
				//add the 'undirected' shortest path
				int curr = p.getFirst().getId();
				int end = p.getSecond().getId();
				int next = 0;
				int nextEdge = 0;
				int cost = 0;
				do {
					next = path[curr][end];
					nextEdge = edgePath[curr][end];
					cost = dist[curr][next];
					e = input.getInternalEdgeMap().get(setupVertices.get(nextEdge).getMatchId());
					input.addEdge(new MixedEdge("added in phase I",  new Pair<MixedVertex>(e.getTail(), e.getHead()), e.getCost(), e.isDirected()));
				} while ( (curr =next) != end);
			}

		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.MIXED_CHINESE_POSTMAN;
	}

	@Override
	protected MixedCPP getInstance() {
		return mInstance;
	}

}
