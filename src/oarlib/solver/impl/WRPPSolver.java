package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import oarlib.problem.impl.WindyRPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;

public class WRPPSolver extends Solver{

	WindyRPP mInstance;

	public WRPPSolver(WindyRPP instance) throws IllegalArgumentException {
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

			WindyGraph copy = mInstance.getGraph().getDeepCopy();
			WindyGraph windyReq = connectRequiredComponents(copy);
			eulerAugment(copy, windyReq);
			DirectedGraph ans = constructOptimalWindyTour(windyReq);
			eliminateRedundantCycles(ans, windyReq, copy);
			return null;
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

	/**
	 * Carries out the connection procedure contained in Benavent's paper, solving an MST problem on the conncted components
	 * induced by the required edges of g.
	 * @param g - the original windy graph representing the WRP Problem.
	 * @return - the connected 'required' graph on which we solve the WPP
	 */
	public static WindyGraph connectRequiredComponents(WindyGraph g)
	{
		try
		{

			int n = g.getVertices().size();
			int m = g.getEdges().size();

			WindyGraph windyReq = new WindyGraph();
			for(int i = 0; i < n; i++)
			{
				windyReq.addVertex(new WindyVertex("original"));
			}

			HashMap<Integer, WindyEdge> indexedWindyEdges = g.getInternalEdgeMap();
			WindyEdge temp;
			int mreq = 0;
			ArrayList<Integer> edge1 = new ArrayList<Integer>();
			ArrayList<Integer> edge2 = new ArrayList<Integer>();
			edge1.add(null);
			edge2.add(null);

			for(int i = 1; i < m+1; i++)
			{
				temp = indexedWindyEdges.get(i);
				if(temp.isRequired())
				{
					mreq++;
					edge1.add(temp.getEndpoints().getFirst().getId());
					edge2.add(temp.getEndpoints().getSecond().getId());
					windyReq.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "original", temp.getCost(), temp.getReverseCost(), i, true);
				}
			}

			//For WRPP, we can start here
			//now figure out the connected components
			int[] component = new int[n+1];
			int[] nodei = new int[mreq+1];
			int[] nodej = new int[mreq+1];
			Integer[] e1 = edge1.toArray(new Integer[edge1.size()]);
			Integer[] e2 = edge2.toArray(new Integer[edge2.size()]);

			int limi = mreq+1;
			for(int i = 1; i < limi; i++)
			{
				nodei[i] = e1[i];
				nodej[i] = e2[i];
			}

			CommonAlgorithms.connectedComponents(n, mreq, nodei, nodej, component);

			//now find shortest paths
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);

			//now create a complete collapsed graph over which we shall solve an MST problem
			UndirectedGraph mstGraph = new UndirectedGraph();
			for(int i = 0; i < component[0]; i++)
			{
				mstGraph.addVertex(new UndirectedVertex("original"));
			}

			int comp1, comp2;
			Double averagePathLength;
			HashMap<Pair<Integer>, Integer> minCostPathVal = new HashMap<Pair<Integer>, Integer>(); //key is components being connected, value is best cost btw them.
			HashMap<Pair<Integer>, Pair<Integer>> minCostPathNodes = new HashMap<Pair<Integer>, Pair<Integer>>();
			Pair<Integer> tempKey;
			//figure out the min cost path from each component to each component
			for(int i = 1; i <= n; i++)
			{
				for(int j = 1; j <= n; j++)
				{	
					comp1 = component[i];
					comp2 = component[j];
					if(comp1 == comp2)
						continue; //dun care about internal distances

					averagePathLength = calculateAveragePathCost(g, i, j, path, edgePath); 
					if(comp1<comp2)
						tempKey = new Pair<Integer>(comp1,comp2);
					else
						tempKey = new Pair<Integer>(comp2,comp1);
					if(!minCostPathVal.containsKey(tempKey) || ((int)2*averagePathLength) < minCostPathVal.get(tempKey))
					{
						minCostPathVal.put(tempKey, (int)(2*averagePathLength));
						minCostPathNodes.put(tempKey, new Pair<Integer>(i,j));
					}
				}
			}

			//now set up the mst graph
			for(Pair<Integer> key : minCostPathVal.keySet())
			{
				mstGraph.addEdge(key.getFirst(), key.getSecond(), "MST Graph", minCostPathVal.get(key));
			}

			int[] mst = CommonAlgorithms.minCostSpanningTree(mstGraph);

			//now add back the mst paths to the windy graph
			limi = mst.length;
			Edge selected;
			WindyEdge toAdd;
			HashMap<Integer, Edge> mstEdges = mstGraph.getInternalEdgeMap();
			Pair<Integer> pathToAdd;
			int curr, next, end;
			for(int i = 0; i < limi; i++)
			{
				if(mst[i] == 1)
				{
					selected = mstEdges.get(i);
					comp1 = selected.getEndpoints().getFirst().getId();
					comp2 = selected.getEndpoints().getSecond().getId();
					if(comp1<comp2)
						tempKey = new Pair<Integer>(comp1, comp2);
					else
						tempKey = new Pair<Integer>(comp2, comp1);
					pathToAdd = minCostPathNodes.get(tempKey);
					//now add to windy copy the new 'required' edges
					curr = pathToAdd.getFirst();
					end = pathToAdd.getSecond();
					do
					{
						next = path[curr][end];
						toAdd = indexedWindyEdges.get(edgePath[curr][end]);
						windyReq.addEdge(curr, next, "mst added", toAdd.getCost(), toAdd.getReverseCost(), edgePath[curr][end], toAdd.isRequired());
					} while((curr = next) !=  end);
				}
			}

			return windyReq;
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method to remove any cycles of non-required edges, or added edges
	 * @param g - the WindyGraph over which we are attempting to construct the optimal windy tour
	 * @param ans - the directed graph representing that optimal windy tour.
	 */
	public static void eliminateRedundantCycles(DirectedGraph ans, WindyGraph windyReq, WindyGraph orig)
	{
		try
		{
			//Improvement Procedure 1
			HashMap<Integer, DirectedVertex> ansVertices = ans.getInternalVertexMap();
			int n = ans.getVertices().size();
			DirectedVertex v;
			HashMap<DirectedVertex, ArrayList<Arc>> vNeighbors, dvNeighbors;
			ArrayList<Arc> vdvConnections, dvvConnections;
			int xij, xji, toRemove;
			for(int i = 1; i <= n; i++)
			{
				v = ansVertices.get(i);
				vNeighbors = v.getNeighbors();
				for(DirectedVertex dv: vNeighbors.keySet())
				{
					dvNeighbors = dv.getNeighbors();
					if(v.getId() < dv.getId() && dvNeighbors.containsKey(v)) //to avoid redundant checking
					{
						vdvConnections = vNeighbors.get(dv);
						xij = vdvConnections.size();
						dvvConnections = dvNeighbors.get(v);
						xji = dvvConnections.size();
						if(xij + xji >= 3)
						{
							if(xij == xji)
							{
								toRemove = xij-1;
							}
							else
							{
								toRemove = Math.min(xij, xji);
							}

							//remove toRemove arcs in each direction
							for(int j = 0; j < toRemove; j++)
							{
								ans.removeEdge(vdvConnections.get(0)); // I think this should work, so long as this vdvConnections and dvvConnections are passed by reference
								ans.removeEdge(dvvConnections.get(0));
							}
						}
					}
				}
			}

			//Improvement Procedure 2
			HashMap<Integer, WindyEdge> windyReqEdges = windyReq.getInternalEdgeMap();
			DirectedGraph flowGraph = new DirectedGraph();
			for(int i = 0; i < n; i ++)
			{
				flowGraph.addVertex(new DirectedVertex("flow"));
			}
			Arc temp;
			WindyEdge windyReqTemp;
			int tempCost;
			boolean isReq;
			for(int i = 1; i <= n; i ++)
			{
				v = ansVertices.get(i);
				vNeighbors = v.getNeighbors();
				for(DirectedVertex dv: vNeighbors.keySet())
				{
					vdvConnections = vNeighbors.get(dv);
					isReq = vdvConnections.get(0).isRequired();
					xij = vdvConnections.size();
					tempCost = vdvConnections.get(0).getCost();
					if(xij >= 3 && isReq) //a
					{
						temp = flowGraph.constructEdge(dv.getId(), v.getId(), "a", -2 * tempCost);
						temp.setCapacity((int)Math.floor((xij-1)/2.0));
						flowGraph.addEdge(temp);
					}
					else if(xij >= 2 && !isReq) //b
					{
						temp = flowGraph.constructEdge(dv.getId(), v.getId(), "b", -2 * tempCost);
						temp.setCapacity((int)Math.floor((xij)/2.0));
						flowGraph.addEdge(temp);
					}
					else if(xij > 0) //c
					{
						windyReqTemp = windyReqEdges.get(vdvConnections.get(0).getMatchId());
						if(windyReqTemp.getCost() == tempCost)
						{
							tempCost = windyReqTemp.getReverseCost() - tempCost;
						}
						else
							tempCost = windyReqTemp.getCost() - tempCost;

						temp = flowGraph.constructEdge(dv.getId(), v.getId(), "c", tempCost);
						temp.setMatchId(vdvConnections.get(0).getMatchId());
						temp.setCapacity(xij);
						flowGraph.addEdge(temp);
					}

				}
			}

			//I don't know how to solve a min cost flow with no demands or supplies...we'll just push flow along all negative cycles
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			boolean foundImprovement = true;
			int curr, end, next, cost;
			HashMap<Integer, Arc> flowArcs = flowGraph.getInternalEdgeMap();
			ArrayList<Arc> removeCandidates;
			Arc changeDir;
			WindyEdge replaceDir;
			while(foundImprovement)
			{
				foundImprovement = false;
				CommonAlgorithms.fwLeastCostPaths(flowGraph, dist, path, edgePath);
				for(int i = 1; i <= n; i++)
				{
					if(dist[i][i] < 0)
					{
						foundImprovement = true;
						//push flow along the negative cycle

						curr = i;
						end = i;
						next = 0;
						cost = 0;
						DirectedVertex u1,u2;
						do {
							next = path[curr][end];
							temp = flowArcs.get(edgePath[curr][next]);
							cost = dist[curr][next];
							u1 = ansVertices.get(curr);
							u2 = ansVertices.get(next);

							if(temp.getLabel().equals("a") || temp.getLabel().equals("b"))
							{
								//remove two copies of arc ji from ans
								removeCandidates = u2.getNeighbors().get(u1);
								ans.removeEdge(removeCandidates.get(0));
								ans.removeEdge(removeCandidates.get(0));
							}
							else //c
							{
								//change the orientation of arc ji
								removeCandidates = u2.getNeighbors().get(u1);
								changeDir = removeCandidates.get(0);
								replaceDir = windyReqEdges.get(changeDir.getMatchId());
								if(changeDir.getCost() == replaceDir.getCost())
									cost = replaceDir.getReverseCost();
								else
									cost = replaceDir.getCost();
								ans.addEdge(changeDir.getHead().getId(), changeDir.getTail().getId(), "swapDir", cost, replaceDir.isRequired());
								ans.removeEdge(removeCandidates.get(0));
							}
							//update flow
							temp.setCapacity(temp.getCapacity()-1);
							if(temp.getCapacity()==0)
								flowGraph.removeEdge(temp);
						} while ( (curr =next) != end);
						break;
					}
				}

			}

			//now reconnect with MST
			if(!CommonAlgorithms.isStronglyConnected(ans))
			{
				//create a windy graph out of the ans graph 
				//each arc gets an edge; overdoing it, but should be okay
				//then add windy edges from the original as not required
				//call connectRequiredComponents
				//then check for non-required edges, and add accordingly
				WindyGraph connectMe = new WindyGraph();
				for(int i = 1; i <= n; i++)
				{
					connectMe.addVertex(new WindyVertex("reconnection step"));
				}
				for(Arc a: ans.getEdges())
				{
					connectMe.addEdge(a.getTail().getId(), a.getHead().getId(), "connection step", a.getCost(), a.getCost(), true);
				}
				for(WindyEdge we: orig.getEdges())
				{
					connectMe.addEdge(we.getEndpoints().getFirst().getId(), we.getEndpoints().getSecond().getId(), "connection step", we.getCost(), we.getReverseCost(), we.isRequired());
					;				}
				WindyGraph windyReq2 = connectRequiredComponents(connectMe);
				for(WindyEdge we: windyReq2.getEdges())
				{
					if(!we.isRequired())
					{
						ans.addEdge(we.getEndpoints().getFirst().getId(), we.getEndpoints().getSecond().getId(), "forward", we.getCost(), false);
						ans.addEdge(we.getEndpoints().getSecond().getId(), we.getEndpoints().getFirst().getId(), "backward", we.getReverseCost(), false);
					}
				}
			}

			//Improvement Procedure 3
			//compute an euler tour, and then replace non-req paths with shortest paths from the full graph
			ArrayList<Integer> tour = CommonAlgorithms.tryHierholzer(ans);
			HashMap<Integer, Arc> ansArcs = ans.getInternalEdgeMap();
			HashMap<Integer, WindyEdge> origEdges = orig.getInternalEdgeMap();
			int m = ans.getEdges().size();
			int startId = -1;
			int endId = -1;
			boolean midPath = false;
			int nextEdge;
			WindyEdge origTemp;
			dist = new int[n+1][n+1];
			path = new int [n+1][n+1];
			edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(orig, dist, path, edgePath);

			for(int i = 1; i<= m; i++)
			{
				temp = ansArcs.get(tour.get(i-1));
				if(!temp.isRequired() && startId < 0) //start non-req path
				{
					startId = temp.getTail().getId();
					midPath = true;

					//remove the edge
					ans.removeEdge(temp);
				}
				else if(temp.isRequired() && startId > 0 && endId < 0) //end non-req path
				{
					endId = temp.getTail().getId();
					//add the shortest path from startId to endId
					curr = startId;
					end = endId;
					do
					{
						next = path[curr][end];
						nextEdge = edgePath[curr][end];
						origTemp = origEdges.get(nextEdge);
						if(origTemp.getEndpoints().getFirst().getId() == next)
							cost = origTemp.getReverseCost();
						else
							cost = origTemp.getCost();
						ans.addEdge(curr, next, "final", cost);

					} while((curr = next) != end);


					//reset
					startId = -1;
					endId = -1;
					midPath = false;
				}
				else if(midPath)
				{
					//remove the edge
					ans.removeEdge(temp);
				}
			}


			//compute cost after improvement
			HashSet<Arc> arcSet = ans.getEdges();
			int cost2 = 0;
			for(Arc a: arcSet)
			{
				cost2 += a.getCost();
			}
			System.out.println("Cost after improvement is: " + cost2);

		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	public static DirectedGraph constructOptimalWindyTour(WindyGraph g) throws IllegalArgumentException
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
			int tempCost;
			for(int i = 1; i < m+1; i++)
			{
				e = windyEdges.get(i);
				tempCost = Math.abs(e.getCost() - e.getReverseCost());
				//add an artificial one in the greater cost direction
				if(e.getCost() > e.getReverseCost())
				{
					temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), tempCost);
					temp.setCapacity(2);
					temp.setRequired(e.isRequired());
					temp.setMatchId(e.getId());
					flowGraph.addEdge(temp);
					artID = temp.getId();
				}
				else
				{
					temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), tempCost);
					temp.setCapacity(2);
					temp.setRequired(e.isRequired());
					temp.setMatchId(e.getId());
					flowGraph.addEdge(temp);
					artID = temp.getId();
				}
				//add one in each direction
				flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), 2 * e.getCost()), artID);
				flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), 2 * e.getReverseCost()), artID);

			}

			if(CommonAlgorithms.isEulerian(flowGraph))
			{
				DirectedGraph ans = new DirectedGraph();
				for (int i = 1; i < n+1; i++)
				{
					ans.addVertex(new DirectedVertex("ans"));
				}
				for(int i = 1; i < m+1; i++)
				{
					e = windyEdges.get(i);
					//add an arc in the least cost direction
					if(e.getCost() > e.getReverseCost())
					{
						ans.addEdge(e.getEndpoints().getSecond().getId(), e.getEndpoints().getFirst().getId(), "ans", e.getReverseCost(), e.getId(), e.isRequired());
					}
					else
					{
						ans.addEdge(e.getEndpoints().getFirst().getId(), e.getEndpoints().getSecond().getId(), "ans",e.getCost(), e.getId(), e.isRequired());
					}
				}
				//compute cost
				HashSet<Arc> arcSet = ans.getEdges();
				int cost = 0;
				for(Arc a: arcSet)
				{
					cost += a.getCost();
				}
				System.out.println("Cost is: " + cost);
				return ans;
			}
			for(DirectedVertex v: flowGraph.getVertices())
			{
				if(v.getDelta() != 0)
					v.setDemand(-1 * v.getDelta());
			}
			int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(flowGraph);

			//now parse the result
			HashMap<Integer, Arc> flowEdges = flowGraph.getInternalEdgeMap();
			Arc artificial;
			DirectedGraph ans = new DirectedGraph();
			for (int i = 1; i < n+1; i++)
			{
				ans.addVertex(new DirectedVertex("ans"));
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
						ans.addEdge(temp.getTail().getId(),temp.getHead().getId(),"ans",temp.getCost()/2, artificial.getMatchId(), artificial.isRequired());
					}
				}
				else if(artificial.getHead().getId() == temp.getTail().getId() && flowanswer[temp.getMatchId()] == 0)
				{
					for(int j = 0; j <= flowanswer[i]; j++)
					{
						ans.addEdge(temp.getTail().getId(),temp.getHead().getId(),"ans",temp.getCost()/2, artificial.getMatchId(), artificial.isRequired());
					}
				}
			}

			//should be done now
			if(!CommonAlgorithms.isEulerian(ans))
				System.out.println("The flow augmentation failed."); //should never happen

			//compute cost
			HashSet<Arc> arcSet = ans.getEdges();
			int cost = 0;
			for(Arc a: arcSet)
			{
				cost += a.getCost();
			}
			System.out.println("Cost is: " + cost);

			return ans;

		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static void eulerAugment(WindyGraph fullGraph, WindyGraph g)
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
					g.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "to make even", temp.getCost(), temp.getReverseCost() ,nextEdge, temp.isRequired());
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
		return ans/2;
	}
}
