package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Edge;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.core.WindyEdge;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedRPP;
import oarlib.problem.impl.WindyCPP;
import oarlib.problem.impl.WindyRPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;

public class ImprovedWRPPSolver extends Solver{

	WindyRPP mInstance;
	private static final double K = .2; //parameter fixed by computational experiments done by Benavent

	public ImprovedWRPPSolver(WindyRPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance=instance;
	}

	@Override
	protected Problem getInstance() {
		return mInstance;
	}

	@Override
	protected Collection<Route> solve(){
		/**
		 * TODO: Is the beginning of this guy supposed to be done on copy or windyReq
		 */
		try
		{
			WindyGraph copy = mInstance.getGraph().getDeepCopy();
			WindyGraph windyReq = WRPPSolver.connectRequiredComponents(copy);
			
			
			//START REGULAR WINDY SOLVER
			//calculate average cost
			double averageCost = calculateAverageCost(windyReq);

			//construct E1 and E2
			HashSet<Integer> E1 = new HashSet<Integer>();
			HashSet<Integer> E2 = new HashSet<Integer>();
			buildEdgeSets(E1, E2, windyReq, averageCost);

			// build Gdr
			DirectedGraph Gdr = buildGdr(copy, E1);
			HashSet<Integer> L = new HashSet<Integer>();
			if(!CommonAlgorithms.isEulerian(Gdr))
			{

				//build Gaux
				DirectedGraph Gaux = buildGaux(copy, E1);

				//set up the flow problem on Gaux using demands from Gdr
				HashMap<Integer, DirectedVertex> indexedVertices = Gdr.getInternalVertexMap();
				for(DirectedVertex v: Gaux.getVertices())
				{
					v.setDemand(indexedVertices.get(v.getId()).getDelta());
				}


				//solve the flow problem on Gaux with demands from Gdr
				int flowanswer[] = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(Gaux);

				//create L
				L = buildL(Gaux, E1, flowanswer);
			}

			//euler augment
			eulerAugment(copy, windyReq, L);
			WPPSolver.constructOptimalWindyTour(windyReq);
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void eulerAugment(WindyGraph fullGraph, WindyGraph g, HashSet<Integer> L)
	{

		try
		{
			//the windy graph, but with edges that carry the average cost of each traversal.
			UndirectedGraph averageGraph = new UndirectedGraph();
			int n = fullGraph.getVertices().size();
			int m = fullGraph.getEdges().size();
			for (int i = 1; i < n+1; i++)
			{
				averageGraph.addVertex(new UndirectedVertex("orig"), i);
			}
			HashMap<Integer, UndirectedVertex> averageVertices = averageGraph.getInternalVertexMap();
			HashMap<Integer, WindyEdge> windyEdges = fullGraph.getInternalEdgeMap();
			WindyEdge e;
			//add the sum of the cost and reverse cost (so that it's still an integer).
			for(int i = 1; i < m+1; i++)
			{
				e = windyEdges.get(i);
				if(L.contains(e.getId()))
					averageGraph.addEdge(new Edge("orig", new Pair<UndirectedVertex>(averageVertices.get(e.getEndpoints().getFirst().getId()), averageVertices.get(e.getEndpoints().getSecond().getId())), 0));
				else
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
			for (WindyVertex v: g.getVertices())
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
			int curr, end, next, nextEdge;
			HashMap<Integer, WindyEdge> indexedEdges = fullGraph.getInternalEdgeMap();
			WindyEdge temp;
			for(Pair<UndirectedVertex> p :matchingSolution)
			{
				curr = p.getFirst().getMatchId();
				end = p.getSecond().getMatchId();
				next = 0;
				nextEdge = 0;
				do {
					next = path[curr][end];
					nextEdge = edgePath[curr][end];
					temp = indexedEdges.get(nextEdge);
					g.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "to make even", temp.getCost(), temp.getReverseCost() ,nextEdge);
				} while ( (curr = next) != end);
			}

			//should be Eulerian now
			if(!CommonAlgorithms.isEulerian(g))
				System.out.println("The UCPP augmentation failed.");
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

	}

	private static HashSet<Integer> buildL(DirectedGraph gaux, HashSet<Integer> e1, int[] flowanswer)
	{
		HashSet<Integer> ans = new HashSet<Integer>();
		Arc temp;
		HashMap<Integer, Arc> indexedArcs = gaux.getInternalEdgeMap();
		int tempMatchId;
		for(int i = 1; i< flowanswer.length; i++)
		{
			temp = indexedArcs.get(i);
			tempMatchId = temp.getMatchId();
			if(temp.isCapacitySet())
				continue;
			if(flowanswer[i] >= 1 && e1.contains(tempMatchId)) //in e1, and flow >= 1
				ans.add(tempMatchId);
			else if(flowanswer[i] >= 2 && !e1.contains(tempMatchId))
				ans.add(tempMatchId);
		}
		return ans;
	}

	private static DirectedGraph buildGdr(WindyGraph g, HashSet<Integer> unbalancedEdges)
	{
		try {
			DirectedGraph ans = new DirectedGraph();
			//the vertex set is the same as g
			int n = g.getVertices().size();
			for(int i = 0; i < n; i++)
			{
				ans.addVertex(new DirectedVertex("Gdr"));
			}

			HashMap<Integer, WindyEdge> indexedEdges = g.getInternalEdgeMap();
			WindyEdge temp;
			//add an arc in the cheaper direction of the unbalanced edges
			for(Integer id: unbalancedEdges)
			{
				temp = indexedEdges.get(id);
				if(temp.getCost() < temp.getReverseCost())
				{
					ans.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "Gdr", temp.getCost());
				}
				else
				{
					ans.addEdge(temp.getEndpoints().getSecond().getId(), temp.getEndpoints().getFirst().getId(), "Gdr", temp.getReverseCost());
				}
			}
			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	private static DirectedGraph buildGaux(WindyGraph fullGraph, HashSet<Integer> unbalancedEdges)
	{
		try {
			DirectedGraph ans = new DirectedGraph();
			int n = fullGraph.getVertices().size();
			for(int i = 0; i < n; i++)
			{
				ans.addVertex(new DirectedVertex("Gaux"));
			}

			//put in an arc for each of the edges in g
			int i,j,tempCost;
			for(WindyEdge e: fullGraph.getEdges())
			{
				i = e.getEndpoints().getFirst().getId();
				j = e.getEndpoints().getSecond().getId();
				//double cost so we can keep everything integer
				ans.addEdge(i,j,"Gaux",2*e.getCost(), e.getId());
				ans.addEdge(j,i,"Gaux",2*e.getReverseCost(), e.getId());
			}

			WindyEdge temp;
			HashMap<Integer, WindyEdge> indexedEdges = fullGraph.getInternalEdgeMap();
			HashMap<Integer, DirectedVertex> indexedVertices = ans.getInternalVertexMap();
			Arc toAdd;
			//add an arc in the high cost direction for each of the unbalanced edges
			for(Integer id: unbalancedEdges)
			{
				temp = indexedEdges.get(id);
				i = temp.getEndpoints().getFirst().getId();
				j = temp.getEndpoints().getSecond().getId();

				if(temp.getCost() < temp.getReverseCost())
				{
					tempCost = temp.getReverseCost() - temp.getCost();
					toAdd = new Arc("Gaux",new Pair<DirectedVertex>(indexedVertices.get(j), indexedVertices.get(i)), tempCost);
					toAdd.setCapacity(2);
					ans.addEdge(toAdd);
				}
				else
				{
					tempCost = temp.getCost() - temp.getReverseCost();
					toAdd = new Arc("Gaux",new Pair<DirectedVertex>(indexedVertices.get(i), indexedVertices.get(j)), tempCost);
					toAdd.setCapacity(2);
					ans.addEdge(toAdd);
				}

			}
			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}


	private static void buildEdgeSets(HashSet<Integer> e1, HashSet<Integer> e2, WindyGraph g, double averageCost)
	{
		double costDiff;
		for(WindyEdge e: g.getEdges())
		{
			costDiff = Math.abs(e.getCost() - e.getReverseCost());
			if(costDiff > K * averageCost )
				e1.add(e.getMatchId());
			else
				e2.add(e.getMatchId());
		}
	}

	private static double calculateAverageCost(WindyGraph g)
	{
		double ans = 0;
		int m = 2 * g.getEdges().size();
		for(WindyEdge e: g.getEdges())
		{
			ans += (e.getCost() + e.getReverseCost());
		}
		return ans/m;
	}
	

	@Override
	public Type getProblemType() {
		return Problem.Type.WINDY_CHINESE_POSTMAN;
	}


}
