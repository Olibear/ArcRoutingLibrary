package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Arc;
import oarlib.core.MixedEdge;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.core.WindyEdge;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;


public class WRPP2Solver extends Solver{

	WindyRPP mInstance;

	public WRPP2Solver(WindyRPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected WindyRPP getInstance() {
		return mInstance;
	}

	@Override
	protected Collection<Route> solve() {
		try
		{

			//Copy the graph
			WindyGraph copy = mInstance.getGraph().getDeepCopy();

			//Phase 1
			MixedGraph G3 = Phase1(copy);

			DirectedGraph G6;
			//Phase 2
			if(!isEven(G3))
			{
				DirectedGraph G5 = Phase2(G3, copy);
				if(!CommonAlgorithms.isStronglyConnected(G5))
				{
					//Phase 3
					G6 = Phase3(G5, copy);
				}
				else
					G6 = G5;
	
				//done
				//return the answer
				ArrayList<Route> ret = new ArrayList<Route>();
				ArrayList<Integer> tour;
				tour = CommonAlgorithms.tryHierholzer(G6);
				Tour eulerTour = new Tour();
				HashMap<Integer, Arc> indexedEdges = G6.getInternalEdgeMap();
				for (int i=0;i<tour.size();i++)
				{
					eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
				}
				ret.add(eulerTour);
				return ret;
			}
			else
			{
				//G5 is just the graph ignoring the undirected edges in G3
				DirectedGraph G5 = trimEdges(G3);
				G6 = Phase3(G5, copy);
				
				//return the answer
				ArrayList<Route> ret = new ArrayList<Route>();
				ArrayList<Integer> tour;
				tour = CommonAlgorithms.tryHierholzer(G6);
				Tour eulerTour = new Tour();
				HashMap<Integer, Arc> indexedEdges = G6.getInternalEdgeMap();
				for (int i=0;i<tour.size();i++)
				{
					eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
				}
				ret.add(eulerTour);
				return ret;
			}
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Solves phase 1 of WRPP2, where we solve a min cost flow problem on the Gdr graph, and create
	 * a symmetric mixed graph.
	 * @param fullGraph - the original windy graph the problem is to be solved on
	 * @return - a symmetric mixed graph ready for Phase 2
	 */
	private static MixedGraph Phase1(WindyGraph fullGraph)
	{
		try
		{
			int n = fullGraph.getVertices().size();
			int m = fullGraph.getEdges().size();
			DirectedGraph G1 = new DirectedGraph();
			DirectedGraph H2 = new DirectedGraph();
			HashMap<Integer, WindyEdge> fullGraphEdges = fullGraph.getInternalEdgeMap();
			WindyEdge temp;
			Arc toAdd;
			int artId;

			//Set up Gdr
			for(int i = 1; i <=n; i++)
			{
				G1.addVertex(new DirectedVertex("G1"));
				H2.addVertex(new DirectedVertex("H2"));
			}
			for(int i = 1; i <=m; i ++)
			{
				temp = fullGraphEdges.get(i);
				if(temp.isRequired())
				{
					if(temp.getCost() < temp.getReverseCost())
					{
						G1.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "G1", temp.getCost());
						toAdd = H2.constructEdge(temp.getEndpoints().getSecond().getId(), temp.getEndpoints().getFirst().getId(), "H2art", temp.getReverseCost()-temp.getCost());
						toAdd.setCapacity(2);
						toAdd.setMatchId(i);
						H2.addEdge(toAdd);
						artId = toAdd.getId();
					}
					else
					{
						G1.addEdge(temp.getEndpoints().getSecond().getId(), temp.getEndpoints().getFirst().getId(), "G1", temp.getReverseCost());
						toAdd = H2.constructEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "H2art", temp.getCost()-temp.getReverseCost());
						toAdd.setCapacity(2);
						toAdd.setMatchId(i);
						H2.addEdge(toAdd);
						artId = toAdd.getId();
					}

					H2.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "H2", 2*temp.getCost(), artId);
					H2.addEdge(temp.getEndpoints().getSecond().getId(), temp.getEndpoints().getFirst().getId(), "H2", 2*temp.getReverseCost(), artId);
				}
			}

			//set demands according to status in G1
			HashMap<Integer, DirectedVertex> G1vertices = G1.getInternalVertexMap();
			HashMap<Integer, DirectedVertex> H2vertices = H2.getInternalVertexMap();
			HashMap<Integer, Arc> H2arcs = H2.getInternalEdgeMap();
			DirectedVertex g1v, h2v;
			Arc H2temp, artTemp;
			for(int i = 1; i <= n; i ++)
			{
				g1v = G1vertices.get(i);
				h2v = H2vertices.get(i);
				h2v.setDemand(g1v.getDelta());
			}

			int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(H2);

			MixedGraph G3 = new MixedGraph();
			for(int i = 1; i <= n; i ++)
			{
				G3.addVertex(new MixedVertex("G3"));
			}

			MixedEdge mixedToAdd;
			//Compose G3 based on the flow solution
			for(int i = 1; i < flowanswer.length; i ++)
			{
				H2temp = H2arcs.get(i);
				if(H2temp.isCapacitySet() && flowanswer[i] == 1) //add an edge to determine the odd vertices later
				{
					mixedToAdd = G3.constructEdge(H2temp.getEndpoints().getFirst().getId(), H2temp.getEndpoints().getSecond().getId(), "dummy", 100000, false);
					mixedToAdd.setMatchId(H2temp.getMatchId());
					G3.addEdge(mixedToAdd);
				}
				else if(!H2temp.isCapacitySet()) //we're real
				{
					artTemp = H2arcs.get(H2temp.getMatchId()); //get the artificial guy associated with this
					if(H2temp.getTail().getId() == artTemp.getHead().getId() && flowanswer[artTemp.getId()] == 0)
					{
						for(int j = 0; j <= flowanswer[i]; j++)
						{
							G3.addEdge(H2temp.getTail().getId(), H2temp.getHead().getId(), "A4", H2temp.getCost()/2, true);
						}
					}
					else if (H2temp.getTail().getId() == artTemp.getTail().getId() && flowanswer[artTemp.getId()] == 2)
					{
						for(int j = 0; j <= flowanswer[i]; j++)
						{
							G3.addEdge(H2temp.getTail().getId(), H2temp.getHead().getId(), "A4", H2temp.getCost()/2, true);
						}
					}

				}
			}

			HashMap<Integer, MixedVertex> G3vertices = G3.getInternalVertexMap();
			MixedVertex symmetryCheck;
			for(int i = 1; i <= n; i++)
			{
				symmetryCheck = G3vertices.get(i);
				if(symmetryCheck.getInDegree() != symmetryCheck.getOutDegree())
					System.out.println("debug");
			}
			return G3;

		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Solves the min-cost matching problem over G3 in order to make it even.
	 * @param G3 - the output of Phase1
	 * @return - a DirectedGraph that is symmetric and covers all required edges 
	 * at least once, (maybe not connected)
	 */
	private static DirectedGraph Phase2(MixedGraph G3, WindyGraph fullGraph)
	{
		try
		{
			int n = G3.getVertices().size();
			int m = G3.getEdges().size();
			HashMap<Integer, MixedVertex> G3vertices = G3.getInternalVertexMap();
			HashMap<Integer, WindyEdge> fullGraphEdges = fullGraph.getInternalEdgeMap(); 
			MixedVertex mvTemp;

			//first put E1 into G4
			WindyGraph G4 = new WindyGraph();
			WindyEdge windyTemp;
			for(int i = 1; i<= n; i ++)
			{
				G4.addVertex(new WindyVertex("G4"));
			}
			for(MixedEdge me: G3.getEdges())
			{
				if(!me.isDirected())
				{
					windyTemp = fullGraphEdges.get(me.getMatchId());
					G4.addEdge(windyTemp.getEndpoints().getFirst().getId(), windyTemp.getEndpoints().getSecond().getId(), "from G3", windyTemp.getCost(), windyTemp.getReverseCost(), true);
				}
			}


			//setup matching graph
			UndirectedGraph matchingGraph = new UndirectedGraph();
			for(int i = 1; i <= n; i++)
			{
				mvTemp = G3vertices.get(i);
				if((mvTemp.getDegree() % 2 )== 1)
				{
					matchingGraph.addVertex(new UndirectedVertex("odd"), mvTemp.getId());
				}
			}

			//compute the shortest paths in the original graph 
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];

			CommonAlgorithms.fwLeastCostPaths(fullGraph, dist, path, edgePath);

			double avgPathCost1, avgPathCost2;
			HashMap<Pair<Integer>, Boolean> traverseIj = new HashMap<Pair<Integer>, Boolean>(); //key is (i,j) where i < j, and value is true if the shortest average path cost is i to j, false if it's j to i
			for(UndirectedVertex odd: matchingGraph.getVertices())
			{
				for(UndirectedVertex odd2: matchingGraph.getVertices())
				{
					if(odd.getId() >= odd2.getId())
						continue;
					//now we know odd's id is less than odd2's
					avgPathCost1 = calculateAveragePathCost(fullGraph, odd.getMatchId(), odd2.getMatchId(), path, edgePath);
					avgPathCost2 = calculateAveragePathCost(fullGraph, odd2.getMatchId(), odd.getMatchId(), path, edgePath);
					if(avgPathCost1 < avgPathCost2)
					{
						matchingGraph.addEdge(odd.getId(), odd2.getId(), "matching", (int)(2*avgPathCost1));
						traverseIj.put(new Pair<Integer>(odd.getId(), odd2.getId()), true);
					}
					else
					{
						matchingGraph.addEdge(odd2.getId(), odd.getId(), "matching", (int)(2*avgPathCost2));
						traverseIj.put(new Pair<Integer>(odd.getId(), odd2.getId()), false);
					}
				}
			}

			Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(matchingGraph);

			//now add the corresponding edges back in the windy graph
			int curr, end, next, nextEdge;
			HashMap<Integer, WindyEdge> indexedEdges = fullGraph.getInternalEdgeMap();
			WindyEdge temp;
			for(Pair<UndirectedVertex> p :matchingSolution)
			{
				//minCostMatching doesn't discriminate between 1 - 2 and 2 - 1 so we need to
				if(p.getFirst().getId() < p.getSecond().getId())
				{
					if(traverseIj.get(new Pair<Integer>(p.getFirst().getId(), p.getSecond().getId())))
					{
						curr = p.getFirst().getMatchId();
						end = p.getSecond().getMatchId();
					}
					else
					{
						curr = p.getSecond().getMatchId();
						end = p.getFirst().getMatchId();
					}
				}
				else
				{
					if(traverseIj.get(new Pair<Integer>(p.getSecond().getId(), p.getFirst().getId())))
					{
						curr = p.getSecond().getMatchId();
						end = p.getFirst().getMatchId();
					}
					else
					{
						curr = p.getFirst().getMatchId();
						end = p.getSecond().getMatchId();
					}
				}

				next = 0;
				nextEdge = 0;
				do {
					next = path[curr][end];
					nextEdge = edgePath[curr][end];
					temp = indexedEdges.get(nextEdge);
					G4.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "to make even", temp.getCost(), temp.getReverseCost() ,nextEdge, temp.isRequired());
				} while ( (curr = next) != end);
			}	

			//now run Win's on G4
			if(!CommonAlgorithms.isEulerian(G4))
				System.out.println("debug");
			DirectedGraph G5 = WRPPSolver.constructOptimalWindyTour(G4);
			
			//now add back the arcs from G3
			for(MixedEdge me: G3.getEdges())
			{
				if(me.isDirected())
				{
					G5.addEdge(me.getTail().getId(), me.getHead().getId(), "from G3", me.getCost(), true);					
				}
			}
			
			if(!CommonAlgorithms.isEulerian(G5))
				System.out.println("debug");
			return G5;


		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static DirectedGraph Phase3(DirectedGraph G5, WindyGraph fullGraph)
	{
		try
		{
		int n = G5.getVertices().size();
		int m = G5.getEdges().size();
		
		WindyGraph connectMe = new WindyGraph();
		DirectedGraph G6 = new DirectedGraph();
		
		for(int i = 1; i <= n; i++)
		{
			connectMe.addVertex(new WindyVertex("connectMe"));
			G6.addVertex(new DirectedVertex("G6"));
		}
		for(Arc a: G5.getEdges())
		{
			connectMe.addEdge(a.getTail().getId(), a.getHead().getId(), "connectMe", 10000 * a.getCost(), 10000 * a.getCost(), true);
			G6.addEdge(a.getTail().getId(), a.getHead().getId(), "G6", a.getCost(), a.isRequired());
		}
		for(WindyEdge we: fullGraph.getEdges())
		{
			connectMe.addEdge(we.getEndpoints().getFirst().getId(), we.getEndpoints().getSecond().getId(), "connectMe", we.getCost(), we.getReverseCost(), we.isRequired());
		}
		WindyGraph nowConnected = WRPPSolver.connectRequiredComponents(connectMe);
		
		for(WindyEdge connecting: nowConnected.getEdges())
		{
			if(!connecting.isRequired())
			{
				G6.addEdge(connecting.getEndpoints().getFirst().getId(), connecting.getEndpoints().getSecond().getId(), "from MST G6", connecting.getCost(), false);
				G6.addEdge(connecting.getEndpoints().getSecond().getId(), connecting.getEndpoints().getFirst().getId(), "from MST G6", connecting.getReverseCost(), false);
			}
		}
		if(!CommonAlgorithms.isEulerian(G6))
			System.out.println("debug");
		return G6;
		
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private static double calculateAveragePathCost(WindyGraph g, int i, int j, int[][] path, int[][] edgePath)
	{
		int curr, end, next, ans;
		curr = i;
		end = j;
		ans = 0;
		WindyEdge temp;
		HashMap<Integer, WindyEdge> indexedWindyEdges = g.getInternalEdgeMap();
		do
		{
			next = path[curr][end];
			temp = indexedWindyEdges.get(edgePath[curr][end]);
			ans += temp.getCost() + temp.getReverseCost();
		}while((curr = next) != end);
		return ans/2.0;
	}

	private static boolean isEven(MixedGraph test)
	{
		for(MixedVertex mv : test.getVertices())
		{
			if(mv.getDegree() %2 == 1)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the directed graph resulting from ignoring all the edges in
	 * the mixed input.
	 * @param G3 - the mixed graph (probably the output of Phase 1) that we want to remove the edges from.
	 * @return - The graph induced by the arc set that makes up G3's arcs.
	 */
	private static DirectedGraph trimEdges(MixedGraph G3)
	{
		try
		{
			int n = G3.getVertices().size();
			DirectedGraph G5 = new DirectedGraph();
			for(int i = 1; i<=n; i ++)
			{
				G5.addVertex(new DirectedVertex("G5"));
			}
			for(MixedEdge me: G3.getEdges())
			{
				if(me.isDirected())
				{
					G5.addEdge(me.getTail().getId(), me.getHead().getId(), "G5", me.getCost(), true);
				}
			}
			return G5;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Type getProblemType() {
		return Problem.Type.WINDY_RURAL_POSTMAN;
	}
}