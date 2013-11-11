package oarlib.test;

import java.util.ArrayList;
import gurobi.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.graph.graphgen.DirectedGraphGenerator;
import oarlib.graph.graphgen.UndirectedGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.Format;
import oarlib.graph.io.GraphReader;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.solver.impl.DCPPSolver;
import oarlib.solver.impl.UCPPSolver;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class GeneralTestbed {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		validateUCPPSolver();
	}
	private static void check(Link<?> a)
	{
		if (a.getClass() == Arc.class)
			System.out.println("WEEEE");
	}
	private static void testDirectedGraphGenerator()
	{
		long start = System.currentTimeMillis();
		DirectedGraphGenerator dgg = new DirectedGraphGenerator();
		DirectedGraph g = (DirectedGraph) dgg.generateGraph(1000, 10, true);
		long end = System.currentTimeMillis();
		System.out.println(end-start);
		System.out.println("check things");
	}

	private static void testUndirectedGraphGenerator()
	{
		long start = System.currentTimeMillis();
		UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
		UndirectedGraph g = (UndirectedGraph) ugg.generateGraph(1000, 10, true);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
		System.out.println("check things");
	}
	private static void testGraphReader()
	{
		GraphReader gr = new GraphReader(Format.Name.Simple);
		try 
		{
			Graph<?,?> g = gr.readDirectedGraph("/Users/oliverlum/Downloads/blossom5-v2.04.src/GRAPH1.TXT");
			if(g.getClass() == DirectedGraph.class)
			{
				DirectedGraph g2 = (DirectedGraph)g;
			}
			System.out.println("check things");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void validateEulerTour()
	{
		try{
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			boolean tourOK;
			for(int i=10;i<150;i+=10)
			{
				tourOK = false;
				g = (UndirectedGraph)ugg.generateEulerianGraph(i, 10, true);
				System.out.println("Undirected graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g));
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g);

				if(ans.size() != g.getEdges().size())
				{
					System.out.println("tourOK: " + tourOK);
					continue;
				}
				HashSet<Integer> used = new HashSet<Integer>();
				HashMap<Integer, Edge> indexedEdges = g.getInternalEdgeMap();
				Edge curr = null;
				Edge prev = null;
				//make sure it's a real tour
				for(int j = 0; j < ans.size(); j++)
				{
					// can't walk the same edge
					if(used.contains(ans.get(j)))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
					//make sure endpoints match up
					prev = curr;
					curr = indexedEdges.get(ans.get(j));
					if(prev == null)
						continue;
					if(!(prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getFirst().getId() ||
							prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getFirst().getId() ||
							prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getSecond().getId() ||
							prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getSecond().getId()))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
				}
				tourOK = true;
				System.out.println("tourOK: " + tourOK);
			}

			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g2;
			for(int i=10; i<150; i+=10)
			{
				tourOK = false;
				g2 = (DirectedGraph)dgg.generateEulerianGraph(i, 10, true);
				System.out.println("Directed graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g2));
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g2);

				if(ans.size() != g2.getEdges().size())
				{
					System.out.println("tourOK: " + tourOK);
					continue;
				}
				HashSet<Integer> used = new HashSet<Integer>();
				HashMap<Integer, Arc> indexedEdges = g2.getInternalEdgeMap();
				Arc curr = null;
				Arc prev = null;
				//make sure it's a real tour
				for(int j = 0; j < ans.size(); j++)
				{
					// can't walk the same edge
					if(used.contains(ans.get(j)))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
					//make sure endpoints match up
					prev = curr;
					curr = indexedEdges.get(ans.get(j));
					if(prev == null)
						continue;
					if(!(prev.getHead().getId() == curr.getTail().getId()))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
				}
				tourOK = true;
				System.out.println("tourOK: " + tourOK);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Compare solutions to the methods provided by Lau.
	 */
	private static void validateMinCostFlow()
	{
		try{
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g;
			for(int i=10;i<150; i+=10)
			{
				g = (DirectedGraph)dgg.generateGraph(i, 10, true);

				//min cost flow not fruitful?
				if(CommonAlgorithms.isEulerian(g))
					continue;

				//set demands
				for(DirectedVertex v:g.getVertices())
				{
					v.setDemand(v.getDelta());
				}
				System.out.println("Generated directed graph with n = " + i);

				//set up for using flow methods
				int n = g.getVertices().size();
				int dist[][] = new int[n+1][n+1];
				int path[][] = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g, dist, path);
				HashMap<Pair<Integer>, Integer> myAns = CommonAlgorithms.cycleCancelingMinCostNetworkFlow(g, dist); //mine
				int[][] ans = CommonAlgorithms.minCostNetworkFlow(g); //Lau's
				
				int cost = 0;
				for(Pair<Integer> p: myAns.keySet())
				{
					cost += dist[p.getFirst()][p.getSecond()] * myAns.get(p);
				}
				//now check against ans
				boolean costOK = true;
				if(ans[0][0] != cost)
					costOK = false;
				System.out.println("costOK: " + costOK);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void validateAllPairsShortestPaths()
	{
		try{
			//for undirected graphs
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			for(int i=2;i<150; i+=10)
			{
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				int n = g.getVertices().size();
				int mDist[][] = new int[n+1][n+1];
				int mPath[][] = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g, mDist, mPath); //mine

				int dist[][] = new int[n+1][n+1];
				int path[][] = new int[n+1][n+1];
				for(int j=0;j<n+1;j++)
					for(int k=0;k<n+1;k++)
						dist[j][k] = Integer.MAX_VALUE;
				int l;
				int m;
				for(Edge e: g.getEdges())
				{
					l = e.getEndpoints().getFirst().getId();
					m = e.getEndpoints().getSecond().getId();
					dist[l][m] = e.getCost();
					dist[m][l] = e.getCost();
				}
				CommonAlgorithms.allPairsShortestPaths(n, dist, Integer.MAX_VALUE, 0, 0, null, path); //Lau's
				boolean distOK = true;
				boolean pathOK = true;
				for(int j=1;j<=n;j++)
				{
					for(int k = 1; k<=n; k++)
					{
						if(dist[j][k] != mDist[j][k]) 
							distOK = false;
						if(path[j][k] != mPath[k][j]) 
							pathOK = false;
					}
				}
				System.out.println("distOK: " + distOK);
				System.out.println("pathOK: " + pathOK);


			}
			//for directed graphs
			DirectedGraph g2;
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			for(int i=2;i<150; i+=10)
			{
				g2 = (DirectedGraph)dgg.generateGraph(i, 10, true);
				System.out.println("Generated directed graph with n = " + i);
				int n = g2.getVertices().size();
				int[][] mDist = new int[n+1][n+1];
				int[][] mPath = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g2, mDist, mPath); //mine

				int[][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				for(int j=0;j<n+1;j++)
					for(int k=0;k<n+1;k++)
						dist[j][k] = Integer.MAX_VALUE;
				int l;
				int m;
				for(Arc a: g2.getEdges())
				{
					l = a.getEndpoints().getFirst().getId();
					m = a.getEndpoints().getSecond().getId();
					if(a.getCost() < dist[l][m])
						dist[l][m] = a.getCost();
				}
				CommonAlgorithms.allPairsShortestPaths(n, dist, Integer.MAX_VALUE, 0, 0, null, path); //Lau's
				boolean distOK = true;
				boolean pathOK = true;
				int mCost = 0;
				for(int j=1;j<=n;j++)
				{
					for(int k = 1; k<=n; k++)
					{
						if(dist[j][k] != mDist[j][k]) 
							distOK = false;
						//paths might be different, we want to make sure they have the same cost
						if(j==k)
							continue;
						l = j;
						m = k;
						mCost = 0;
						while(l != m)
						{
							mCost += mDist[l][mPath[l][m]];
							l = mPath[l][m];
						}
						if(mCost != mDist[j][k])
							pathOK = false;
					}
				}
				System.out.println("distOK: " + distOK);
				System.out.println("pathOK: " + pathOK);
			}

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void validateMinCostMatching()
	{
		try{
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			for(int i=1;i<150; i+=10)
			{
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				CommonAlgorithms.minCostMatching(g); //Kolmogorov's
				//TODO: Lau's
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Compare solutions to gurobi / cplex solvers
	 */
	private static void validateUCPPSolver()
	{
		UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
		UndirectedGraph g;
		UndirectedCPP validInstance;
		UCPPSolver validSolver;
		Collection<Route> validAns;
		for(int i=2;i<150; i+=10)
		{
			g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
			System.out.println("Generated undirected graph with n = " + i);

			validInstance = new UndirectedCPP(g);
			validSolver = new UCPPSolver(validInstance);
			validAns = validSolver.trySolve();
		}
	}
	private static void validateDCPPSolver()
	{
		DirectedGraph g;
		DirectedGraphGenerator dgg = new DirectedGraphGenerator();
		DirectedCPP validInstance;
		DCPPSolver validSolver;
		Collection<Route> validAns;
		for(int i=2;i<150; i+=10)
		{
			g = (DirectedGraph)dgg.generateGraph(i, 10, true);
			System.out.println("Generated directed graph with n = " + i);

			validInstance = new DirectedCPP(g);
			validSolver = new DCPPSolver(validInstance);
			validAns = validSolver.trySolve();

		}
	}
	/**
	 * make sure the machinery is working on toy problem.
	 */
	private static void testUCPPSolver()
	{
		try{
			long start = System.currentTimeMillis();
			UndirectedGraph test = new UndirectedGraph();

			UndirectedVertex v1 = new UndirectedVertex("dummy");
			UndirectedVertex v2 = new UndirectedVertex("dummy2");
			UndirectedVertex v3 = new UndirectedVertex("dummy3");

			Pair<UndirectedVertex> ep = new Pair<UndirectedVertex>(v1, v2);
			Pair<UndirectedVertex> ep2 = new Pair<UndirectedVertex>(v2, v1);
			Pair<UndirectedVertex> ep3 = new Pair<UndirectedVertex>(v2, v3);
			Pair<UndirectedVertex> ep4 = new Pair<UndirectedVertex>(v3,v1);

			Edge e = new Edge("stuff", ep, 10);
			Edge e2 = new Edge("more stuff", ep2, 20);
			Edge e3 = new Edge("third stuff", ep3, 5);
			Edge e4 = new Edge("fourth stuff", ep4, 7);

			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(e);
			test.addEdge(e2);
			test.addEdge(e3);
			test.addEdge(e4);

			UndirectedCPP testInstance = new UndirectedCPP(test);
			UCPPSolver testSolver = new UCPPSolver(testInstance);
			Collection<Route> testAns = testSolver.trySolve();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testDCPPSolver()
	{
		try {
			long start = System.currentTimeMillis();
			DirectedGraph test = new DirectedGraph();

			DirectedVertex v1 = new DirectedVertex("dummy");
			DirectedVertex v2 = new DirectedVertex("dummy2");
			DirectedVertex v3 = new DirectedVertex("dummy3");

			Pair<DirectedVertex> ep = new Pair<DirectedVertex>(v1, v2);
			Pair<DirectedVertex> ep2 = new Pair<DirectedVertex>(v2, v1);
			Pair<DirectedVertex> ep3 = new Pair<DirectedVertex>(v2, v3);
			Pair<DirectedVertex> ep4 = new Pair<DirectedVertex>(v3, v1);

			Arc a = new Arc("stuff", ep, 10);
			Arc a2 = new Arc("more stuff", ep2, 20);
			Arc a3 = new Arc("third stuff", ep3, 5);
			Arc a4 = new Arc("fourth stuff", ep4, 7);
			Arc a5 = new Arc("fifth stuff", ep3,  8);


			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(a);
			test.addEdge(a2);
			test.addEdge(a3);
			test.addEdge(a4);
			test.addEdge(a5);

			DirectedCPP testInstance = new DirectedCPP(test);
			DCPPSolver testSolver = new DCPPSolver(testInstance);
			Collection<Route> testAns = testSolver.trySolve();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
