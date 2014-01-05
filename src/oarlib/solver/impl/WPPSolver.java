package oarlib.solver.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.core.WindyEdge;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.WindyCPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class WPPSolver extends Solver{

	WindyCPP mInstance;

	public WPPSolver(WindyCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected WindyCPP getInstance() {
		return mInstance;
	}

	@Override
	protected Collection<Route> solve() {
		try
		{

			WindyGraph copy = mInstance.getGraph();
			eulerAugment(copy);
			constructOptimalWindyTour(copy);
			return null;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Type getProblemType() {
		return Problem.Type.WINDY_CHINESE_POSTMAN;
	}

	public static void constructOptimalWindyTour(WindyGraph g) throws IllegalArgumentException
	{
		if(!CommonAlgorithms.isEulerian(g))
			throw new IllegalArgumentException();
		try {
			//construct the optimal tour on the Eulerian Windy Graph.
			int n = g.getVertices().size();
			int m = g.getEdges().size();
			//construct the digraph for the min-cost flow solution
			DirectedGraph flowGraph = new DirectedGraph();
			for (int i = 1; i < n+1; i++)
			{
				flowGraph.addVertex(new DirectedVertex("flow"));
			}

			HashMap<Integer, DirectedVertex> flowVertices = flowGraph.getInternalVertexMap();
			HashMap<Integer, WindyEdge> windyEdges = g.getInternalEdgeMap();
			WindyEdge e;
			Arc temp;
			int artID = 0;
			for(int i = 1; i < m+1; i++)
			{
				e = windyEdges.get(i);
				//add an artificial one in the greater cost direction
				if(e.getCost() > e.getReverseCost())
				{
					temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), e.getCost());
					temp.setCapacity(2);
					flowGraph.addEdge(temp);
					artID = temp.getId();
				}
				else
				{
					temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), e.getReverseCost());
					temp.setCapacity(2);
					flowGraph.addEdge(temp);
					artID = temp.getId();
				}
				//add one in each direction
				flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), e.getCost()), artID);
				flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), e.getReverseCost()), artID);
				
			}
			
			for(DirectedVertex v: flowGraph.getVertices())
			{
				if(v.getDelta() != 0)
					v.setDemand(v.getDelta());
			}
			int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(flowGraph);
			
			//now parse the result
			HashMap<Integer, Arc> flowEdges = flowGraph.getInternalEdgeMap();
			Arc artificial;
			DirectedGraph ans = new DirectedGraph();
			for (int i = 1; i < n+1; i++)
			{
				flowGraph.addVertex(new DirectedVertex("ans"));
			}
			for(int i = 1; i < flowanswer.length; i++)
			{
				temp = flowEdges.get(i);
				if(temp.isCapacitySet()) //this is an artificial edge, ignore it
					continue;
				//look at the relevant artificial arc's flow, and determine which direction to go
				artificial = flowEdges.get(temp.getMatchId());
				if(artificial.getHead().getId() == temp.getHead().getId() && flowanswer[temp.getMatchId()] == 2) // artificial and temp in same direction
				{
					for (int j =0; j <= flowanswer[i];j++)
					{
						ans.addEdge(temp.getTail().getId(),temp.getHead().getId(),"ans",temp.getCost());
					}
				}
				else if(artificial.getHead().getId() == temp.getTail().getId() && flowanswer[temp.getMatchId()] == 0)
				{
					for(int j = 0; j <= flowanswer[i]; j++)
					{
						ans.addEdge(temp.getTail().getId(),temp.getHead().getId(),"ans",temp.getCost());
					}
				}
			}
			
			//should be done now
			if(!CommonAlgorithms.isEulerian(ans))
				System.out.println("BADD."); //should never happen
				
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}
	public static void eulerAugment(WindyGraph g)
	{

		try
		{
			//the windy graph, but with edges that carry the average cost of each traversal.
			UndirectedGraph averageGraph = new UndirectedGraph();
			int n = g.getVertices().size();
			int m = g.getEdges().size();
			for (int i = 1; i < n+1; i++)
			{
				averageGraph.addVertex(new UndirectedVertex("orig"), i);
			}
			HashMap<Integer, UndirectedVertex> averageVertices = averageGraph.getInternalVertexMap();
			HashMap<Integer, WindyEdge> windyEdges = g.getInternalEdgeMap();
			WindyEdge e;
			//add the sum of the cost and reverse cost (so that it's still an integer).
			for(int i = 1; i < m+1; i++)
			{
				e = windyEdges.get(i);
				averageGraph.addEdge(new Edge("orig", new Pair<UndirectedVertex>(averageVertices.get(e.getEndpoints().getFirst().getId()), averageVertices.get(e.getEndpoints().getSecond().getId())), e.getCost() + e.getReverseCost()));
			}

			//solve shortest paths in averageGraph
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(averageGraph, dist, path, edgePath);

			//setup the complete graph composed entirely of the unbalanced vertices
			UndirectedGraph matchingGraph = new UndirectedGraph();

			//setup our graph of unbalanced vertices
			for (UndirectedVertex v: averageGraph.getVertices())
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

			//now add the corresponding edges back in the windy graph
			for(Pair<UndirectedVertex> p :matchingSolution)
			{
				CommonAlgorithms.addShortestPath(g, dist, path, edgePath, new Pair<Integer>(p.getFirst().getMatchId(), p.getSecond().getMatchId()));
			}

			//should be Eulerian now
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

	}
}
