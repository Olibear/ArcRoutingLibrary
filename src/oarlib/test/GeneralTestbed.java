package oarlib.test;

import java.io.File;
import java.util.ArrayList;
import gurobi.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.MixedEdge;
import oarlib.core.Route;
import oarlib.graph.graphgen.DirectedGraphGenerator;
import oarlib.graph.graphgen.UndirectedGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.Format;
import oarlib.graph.io.GraphReader;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedCPP;
import oarlib.problem.impl.MixedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.solver.impl.DCPPSolver;
import oarlib.solver.impl.ImprovedMCPPSolver;
import oarlib.solver.impl.MCPPSolver;
import oarlib.solver.impl.ModifiedMCPPSolver;
import oarlib.solver.impl.UCPPSolver;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class GeneralTestbed {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		testFredericksons();
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
			Graph<?,?> g = gr.readGraph("/Users/oliverlum/Downloads/blossom5-v2.04.src/GRAPH1.TXT");
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
	private static void testCorberanGraphReader()
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			Graph<?,?> g = gr.readGraph("/Users/oliverlum/Downloads/MCPP/MA0532");
			if(g.getClass() == MixedGraph.class)
			{
				MixedGraph g2 = (MixedGraph)g;

				DirectedGraph g3 = new DirectedGraph();
				for(int i = 0; i < g2.getVertices().size(); i ++)
				{
					g3.addVertex(new DirectedVertex("checking connectedness"));
				}
				HashMap<Integer, DirectedVertex> g3Vertices = g3.getInternalVertexMap();
				HashMap<Integer, MixedEdge> g2Edges = g2.getInternalEdgeMap();
				MixedEdge e;

				for(int i = 1; i < g2.getEdges().size()+1; i++)
				{
					e = g2Edges.get(i);
					if(e.isDirected())
						g3.addEdge(new Arc("checking connectedness", new Pair<DirectedVertex>(g3Vertices.get(e.getTail().getId()), g3Vertices.get(e.getHead().getId())), e.getCost()));
					else
					{
						g3.addEdge(new Arc("checking connectedness", new Pair<DirectedVertex>(g3Vertices.get(e.getEndpoints().getFirst().getId()), g3Vertices.get(e.getEndpoints().getSecond().getId())), e.getCost()));
						g3.addEdge(new Arc("checking connectedness", new Pair<DirectedVertex>(g3Vertices.get(e.getEndpoints().getSecond().getId()), g3Vertices.get(e.getEndpoints().getFirst().getId())), e.getCost()));
					}
				}
				int n = g3.getVertices().size();
				int m = g3.getEdges().size();
				int[] nodei = new int[m+1];
				int[] nodej = new int[m+1];
				int[] component = new int[n+1];
				HashMap<Integer, Arc> g3Edges = g3.getInternalEdgeMap();
				Arc a;
				for(int i = 1; i < m+1; i++)
				{
					a = g3Edges.get(i);
					nodei[i] = a.getTail().getId();
					nodej[i] = a.getHead().getId();
				}
				CommonAlgorithms.stronglyConnectedComponents(n, m, nodei, nodej, component);

				System.out.println("check things");
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testFredericksons()
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			MixedCPP validInstance;
			MCPPSolver validSolver;
			Collection<Route> validAns;

			File testInstanceFolder = new File("/Users/oliverlum/Downloads/MCPP");
			long start;
			long end;

			for(final File testInstance: testInstanceFolder.listFiles())
			{
				String temp = testInstance.getName();
				System.out.println(temp);
				Graph<?,?> g = gr.readGraph("/Users/oliverlum/Downloads/MCPP/" + temp);
				if(g.getClass() == MixedGraph.class)
				{
					MixedGraph g2 = (MixedGraph)g;
					validInstance = new MixedCPP(g2);
					validSolver = new MCPPSolver(validInstance);
					start = System.nanoTime();
					validAns = validSolver.trySolve(); //my ans
					end = System.nanoTime();
					System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");

				}
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testYaoyuenyong()
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			MixedCPP validInstance;
			ImprovedMCPPSolver validSolver;
			Collection<Route> validAns;

			long start;
			long end;

			Graph<?,?> g = gr.readGraph("/Users/oliverlum/Downloads/MCPP/MA0532");
			if(g.getClass() == MixedGraph.class)
			{
				MixedGraph g2 = (MixedGraph)g;
				validInstance = new MixedCPP(g2);
				validSolver = new ImprovedMCPPSolver(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");

			}
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
			long startTime;
			long endTime;
			boolean tourOK;
			for(int i=10;i<150;i+=10)
			{
				tourOK = false;
				g = (UndirectedGraph)ugg.generateEulerianGraph(i, 10, true);
				System.out.println("Undirected graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g));
				startTime = System.nanoTime();
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g);
				endTime = System.nanoTime();
				System.out.println("It took " + (endTime-startTime)/(1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g.getEdges().size() + " edges.");

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
				startTime = System.nanoTime();
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g2);
				endTime = System.nanoTime();
				System.out.println("It took " + (endTime-startTime)/(1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g2.getEdges().size() + " edges.");

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
			long start;
			long end;
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
				start = System.nanoTime();
				HashMap<Pair<Integer>, Integer> myAns = CommonAlgorithms.cycleCancelingMinCostNetworkFlow(g, dist); //mine
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our cycle canceling min cost flow implementation on a graph with " + g.getEdges().size() + " edges.");

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
	private static void timeMinCostFlow()
	{
		try{
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g;
			long start;
			long end;
			long duration;
			double density;
			for(int i=10;i<300; i+=10)
			{
				density = (2.0*i)/(i*i/2.0); // we want roughly 4n edges
				g = (DirectedGraph)dgg.generateGraph(i, 10, true, density);

				//min cost flow not fruitful?
				if(CommonAlgorithms.isEulerian(g))
					continue;

				//set demands
				for(DirectedVertex v:g.getVertices())
				{
					v.setDemand(v.getDelta());
				}

				//set up for using flow methods
				int n = g.getVertices().size();

				duration  = 0;
				for(int j = 0; j < 10; j++) //time it out 100 times, and take average
				{
					int dist[][] = new int[n+1][n+1];
					int path[][] = new int[n+1][n+1];
					CommonAlgorithms.fwLeastCostPaths(g, dist, path);
					start = System.nanoTime();
					HashMap<Pair<Integer>, Integer> myAns = CommonAlgorithms.cycleCancelingMinCostNetworkFlow(g, dist); //mine
					end = System.nanoTime();
					duration += (end - start);
				}
				System.out.println(duration/(1e7) + "," + g.getEdges().size() + ";");
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	private static void validateMinCostFlow2()
	{
		try{
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g;
			long start;
			long end;
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
				start = System.nanoTime();
				int[] myAns = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(g); //mine
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our SSP min cost flow implementation on a graph with " + g.getEdges().size() + " edges.");

				int[][] ans = CommonAlgorithms.minCostNetworkFlow(g); //Lau's

				int cost = 0;
				HashMap<Integer, Arc> indexedArcs = g.getInternalEdgeMap();
				for(int j=1; j<myAns.length; j++)
				{
					cost += myAns[j] * indexedArcs.get(j).getCost();
				}
				//now check against ans
				boolean costOK = true;
				if(ans[0][0] != cost)
					costOK = false;
				System.out.println("true cost: " + ans[0][0]);
				System.out.println("my cost: " + cost);
				System.out.println("costOK: " + costOK);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void timeMinCostFlow2(){
		try{
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g;
			long start;
			long end;
			long duration;
			double density;
			for(int i=10;i<150; i+=10)
			{
				density = (2.0*i)/(i*i/2.0);
				g = (DirectedGraph)dgg.generateGraph(i, 10, true, density);

				//min cost flow not fruitful?
				if(CommonAlgorithms.isEulerian(g))
					continue;

				//set demands
				for(DirectedVertex v:g.getVertices())
				{
					v.setDemand(v.getDelta());
				}

				//set up for using flow methods
				int n = g.getVertices().size();
				duration =0;
				for(int j =0; j<10; j++)
				{
					int dist[][] = new int[n+1][n+1];
					int path[][] = new int[n+1][n+1];
					CommonAlgorithms.fwLeastCostPaths(g, dist, path);
					start = System.nanoTime();
					int[] myAns = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(g); //mine
					end = System.nanoTime();
					duration += (end-start);
				}
				System.out.println((duration)/(1e7) + "," + g.getEdges().size() + ";");

			}
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	private static void testMinCostFlow2()
	{
		DirectedGraph g = new DirectedGraph();
		//set up graph
		DirectedVertex v1 = new DirectedVertex("orig");
		v1.setDemand(5);
		DirectedVertex v2 = new DirectedVertex("orig");
		DirectedVertex v3 = new DirectedVertex("orig");
		DirectedVertex v4 = new DirectedVertex("orig");
		v4.setDemand(-3);
		DirectedVertex v5 = new DirectedVertex("orig");
		v5.setDemand(-2);

		Arc a1 = new Arc("orig", new Pair<DirectedVertex>(v1, v2), 1);
		a1.setCapacity(7);
		Arc a2 = new Arc("orig", new Pair<DirectedVertex>(v1, v3), 5);
		a2.setCapacity(7);
		Arc a3 = new Arc("orig", new Pair<DirectedVertex>(v2, v3), -2);
		a3.setCapacity(2);
		Arc a4 = new Arc("orig", new Pair<DirectedVertex>(v2, v4), 8);
		a4.setCapacity(3);
		Arc a5 = new Arc("orig", new Pair<DirectedVertex>(v3, v4), -3);
		a1.setCapacity(3);
		Arc a6 = new Arc("orig", new Pair<DirectedVertex>(v3, v5), 4);
		a1.setCapacity(2);

		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		g.addVertex(v5);
		try
		{
			g.addEdge(a1);
			g.addEdge(a2);
			g.addEdge(a3);
			g.addEdge(a4);
			g.addEdge(a5);
			g.addEdge(a6);
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		//solve
		int[] myAns = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(g); //mine
		//getcost
		int cost = 0;
		HashMap<Integer, Arc> indexedArcs = g.getInternalEdgeMap();
		for(int j=1; j<myAns.length; j++)
		{
			cost += myAns[j] * indexedArcs.get(j).getCost();
		}

		System.out.println("example cost: " + cost);
	}
	private static void validateAllPairsShortestPaths()
	{
		try{
			//for undirected graphs
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			long start;
			long end;
			for(int i=2;i<150; i+=10)
			{
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				int n = g.getVertices().size();
				int mDist[][] = new int[n+1][n+1];
				int mPath[][] = new int[n+1][n+1];
				start = System.nanoTime();
				CommonAlgorithms.fwLeastCostPaths(g, mDist, mPath); //mine
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our APSP implementation on a graph with " + g.getEdges().size() + " edges.");


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
	private static void validateDijkstras()
	{
		try{
			//for undirected graphs
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			long start;
			long end;
			for(int i=2;i<500; i+=10)
			{
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				int n = g.getVertices().size();
				int mDist[][] = new int[n+1][n+1];
				int mPath[][] = new int[n+1][n+1];
				start = System.nanoTime();
				CommonAlgorithms.fwLeastCostPaths(g, mDist, mPath); //mine
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our APSP implementation on a graph with " + g.getEdges().size() + " edges.");


				int dist[] = new int[n+1];
				int path[] = new int[n+1];
				int edgePath[] = new int[n+1];
				start = System.nanoTime();
				CommonAlgorithms.dijkstrasAlgorithm(g, 1, dist, path, edgePath);
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Dijkstras implementation on a graph with " + g.getEdges().size() + " edges.");

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

				int[] dist = new int[n+1];
				int[] path = new int[n+1];
				int[] edgePath = new int[n+1];
				CommonAlgorithms.dijkstrasAlgorithm(g2, 1, dist, path, edgePath);
				boolean distOK = true;
				int mCost = 0;
				for(int j=2;j<=n;j++)
				{
					if(dist[j] != mDist[1][j])
						distOK = false;
				}
				System.out.println("distOK: " + distOK);
			}

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void timeAPSP()
	{
		try{
			//for undirected graphs
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			long start;
			long end;
			long duration;
			double density;
			for(int i=2;i<500; i+=10)
			{
				density = (4.0*i)/(i*i/2.0);
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true, density);
				int n = g.getVertices().size();
				duration = 0;
				for(int j=0;j<10;j++)
				{
					int mDist[][] = new int[n+1][n+1];
					int mPath[][] = new int[n+1][n+1];
					start = System.nanoTime();
					CommonAlgorithms.fwLeastCostPaths(g, mDist, mPath); //mine
					end = System.nanoTime();
					duration+=(end-start);
				}
				System.out.println(duration/(1e7) + "," + g.getEdges().size() + ";");
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return;
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
		try {
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			UndirectedGraph g2;
			UndirectedCPP validInstance;
			UCPPSolver validSolver;
			Collection<Route> validAns;

			//timing stuff
			long start;
			long end;

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> oddVertices;

			//the answers
			int l;
			int myCost;
			int trueCost;

			for(int i=2;i<150; i+=10)
			{
				myCost = 0;
				trueCost = 0;
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				//copy for Gurobi to work on
				g2 = g.getDeepCopy();
				validInstance = new UndirectedCPP(g);
				validSolver = new UCPPSolver(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our UCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

				for(Route r: validAns)
				{
					myCost += r.getCost();
				}

				int n = g2.getVertices().size();
				int[][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

				//set up oddVertices
				oddVertices = new ArrayList<Integer>();
				for(UndirectedVertex v: g2.getVertices())
				{
					if(v.getDegree() %2 == 1)
						oddVertices.add(v.getId());
				}

				//Now set up the model in Gurobi and solve it, and see if you get the right answer
				model = new GRBModel(env);
				//put in the base cost of all the edges that we'll add to the objective
				for(Edge a: g2.getEdges())
					trueCost+=a.getCost();

				//create variables
				//after this snippet, element[j][k] contains the variable x_jk which represents the
				//number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
				l = oddVertices.size();
				varArray = new GRBVar[l][l];
				for(int j=0; j<l;j++)
				{
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						varArray[j][k] = model.addVar(0.0,1.0,dist[oddVertices.get(j)][oddVertices.get(k)], GRB.BINARY, "x" + oddVertices.get(j) + oddVertices.get(k));
					}
				}

				//update the model
				model.update();


				//create constraints
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each j, sum up the x_jk and make sure they equal 1
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
				}
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each k, sum up the x_jk and make sure they equal 1
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						expr.addTerm(1, varArray[k][j]);
					}
					model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
				}
				for(int j=0; j<l; j++)
				{
					if(j==0)
						continue;
					expr = new GRBLinExpr();
					//enforce symmetry
					for(int k=0; k<j; k++)
					{
						expr.addTerm(1, varArray[j][k]);
						expr.addTerm(-1, varArray[k][j]);
					}
					model.addConstr(expr, GRB.EQUAL, 0, "cj"+j);
				}
				model.optimize();
				trueCost+=model.get(GRB.DoubleAttr.ObjVal)/2;
				System.out.println("myCost = " + myCost + ", trueCost = " + trueCost);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void timeUCPPSolver()
	{
		try {
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			UndirectedGraph g2;
			UndirectedCPP validInstance;
			UCPPSolver validSolver;
			Collection<Route> validAns;

			//timing stuff
			long start;
			long end;
			double density;

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> oddVertices;

			//the answers
			int l;
			int myCost;
			int trueCost;

			for(int i=2;i<150; i+=10)
			{
				myCost = 0;
				trueCost = 0;
				density = (4.0*i)/(i*i/2.0);
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true, density);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				validInstance = new UndirectedCPP(g);
				validSolver = new UCPPSolver(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println((end-start)/(1e6) + "," + g.getEdges().size() + ";");
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}
	private static void validateDCPPSolver()
	{
		try{
			DirectedGraph g;
			DirectedGraph g2;
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedCPP validInstance;
			DCPPSolver validSolver;
			Collection<Route> validAns;

			//timing stuff
			long start;
			long end;

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> Dplus;
			ArrayList<Integer> Dminus;
			int l;
			int m;
			int myCost;
			double trueCost;
			for(int i=2;i<150; i+=10)
			{
				myCost=0;
				trueCost=0;
				g = (DirectedGraph)dgg.generateGraph(i, 10, true);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				//copy for gurobi to run on
				g2 = g.getDeepCopy();
				HashMap<Integer, DirectedVertex> indexedVertices = g2.getInternalVertexMap();
				System.out.println("Generated directed graph with n = " + i);

				validInstance = new DirectedCPP(g);
				validSolver = new DCPPSolver(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our DCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

				for(Route r: validAns)
				{
					myCost += r.getCost();
				}

				int n = g2.getVertices().size();
				int[][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

				//calculate Dplus and Dminus
				Dplus = new ArrayList<Integer>();
				Dminus = new ArrayList<Integer>();
				for(DirectedVertex v : g2.getVertices())
				{
					if (v.getDelta() < 0)
						Dminus.add(v.getId());
					else if(v.getDelta() > 0)
						Dplus.add(v.getId());
				}

				//Now set up the model in Gurobi and solve it, and see if you get the right answer
				model = new GRBModel(env);
				//put in the base cost of all the edges that we'll add to the objective
				for(Arc a: g2.getEdges())
					trueCost+=a.getCost();

				//create variables
				//after this snippet, element[j][k] contains the variable x_jk which represents the
				//number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
				l = Dplus.size();
				m = Dminus.size();
				varArray = new GRBVar[l][m];
				for(int j=0; j<l;j++)
				{
					for(int k=0; k<m; k++)
					{
						varArray[j][k] = model.addVar(0.0,Double.MAX_VALUE,dist[Dplus.get(j)][Dminus.get(k)], GRB.INTEGER, "x" + Dplus.get(j) + Dminus.get(k));
					}
				}

				//update the model with changes
				model.update();

				//create constraints
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each j, sum up the x_jk and make sure they take care of all the supply
					for(int k=0; k<m; k++)
					{
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, indexedVertices.get(Dplus.get(j)).getDelta(), "cj"+j);
				}
				for(int k=0;k<m;k++)
				{
					expr = new GRBLinExpr();
					//for each k, sum up the x_jk and make sure they take care of all the demand
					for(int j=0;j<l;j++)
					{
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, -1 * indexedVertices.get(Dminus.get(k)).getDelta(), "ck"+k);
				}
				model.optimize();
				trueCost+=model.get(GRB.DoubleAttr.ObjVal);
				System.out.println("myCost = " + myCost + ", trueCost = " + trueCost);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void timeDCPPSolver()
	{
		try{
			DirectedGraph g;
			DirectedGraph g2;
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedCPP validInstance;
			DCPPSolver validSolver;
			Collection<Route> validAns;

			//timing stuff
			long start;
			long end;

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> Dplus;
			ArrayList<Integer> Dminus;
			int l;
			int m;
			int myCost;
			double trueCost;
			double density;
			for(int i=2;i<150; i+=10)
			{
				myCost=0;
				trueCost=0;
				density = (2.0*i)/(i*i/2.0);
				g = (DirectedGraph)dgg.generateGraph(i, 10, true, density);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				//copy for gurobi to run on
				g2 = g.getDeepCopy();
				HashMap<Integer, DirectedVertex> indexedVertices = g2.getInternalVertexMap();

				validInstance = new DirectedCPP(g);
				validSolver = new DCPPSolver(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println((end-start)/(1e6) + "," + g.getEdges().size() + ";");
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return;
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
