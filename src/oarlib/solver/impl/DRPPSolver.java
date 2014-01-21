package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedRPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class DRPPSolver extends Solver{

	DirectedRPP mInstance;

	public DRPPSolver(DirectedRPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected Collection<Route> solve() {

		try
		{
			DirectedGraph copy  = mInstance.getGraph();

			//form the complete graph Gc1 = (Nr, Ar U As)
			DirectedGraph Gc1 = formGc1(copy);

			//simplify Gc1 to get Gc2
			DirectedGraph Gc2 = formGc2(Gc1);

			//Collapse the graph to its connected components with min cost arcs joining them
			DirectedGraph Gc = collapseGraph(Gc2);

			//compute a shortest spanning arborescence and re/expand
			DirectedGraph Gfinal = connectAndExpand(Gc);

			//solve an uncapacitated min-cost flow problem, and then add the appropriate arcs
			for(DirectedVertex v: Gfinal.getVertices())
			{
				v.setDemand(v.getDelta());
			}
			int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(Gfinal);
			HashMap<Integer, Arc> indexedArcs = Gfinal.getInternalEdgeMap();
			Arc temp;
			//add the solution to the graph (augment)
			for(int i = 1; i < flowanswer.length; i++)
			{
				temp = indexedArcs.get(i);
				for(int j = 0; j < flowanswer[i]; j++)
				{
					Gfinal.addEdge(new Arc("added from flow", temp.getEndpoints(), temp.getCost()));
				}
			}

		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		// TODO Auto-generated method stub
		return null;
	}

	private static DirectedGraph connectAndExpand(DirectedGraph g)
	{
		//TODO
		return null;
	}

	private static DirectedGraph formGc1(DirectedGraph g)
	{

		try
		{
			DirectedGraph ans = new DirectedGraph();
			//figure out Nr from Ar
			HashSet<Integer> nrIds = new HashSet<Integer>();
			ArrayList<Arc> reqArcs = new ArrayList<Arc>();
			for(Arc a: g.getEdges())
			{
				if(a.isRequired())
				{
					reqArcs.add(a);
					nrIds.add(a.getHead().getId());
					nrIds.add(a.getTail().getId());
				}
			}
			HashMap<Integer, DirectedVertex> gVertices = g.getInternalVertexMap();
			int i = 1;
			for(Integer id: nrIds)
			{
				gVertices.get(id).setMatchId(i++);
				ans.addVertex(new DirectedVertex("req node"),id);
			}
			for(Arc a: reqArcs)
			{
				ans.addEdge(a.getTail().getMatchId(), a.getHead().getMatchId(), "req arc", a.getCost());
			}

			//now make it complete
			int n = g.getVertices().size();
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);
			Arc toAdd;
			for(DirectedVertex v1: ans.getVertices())
			{
				for(DirectedVertex v2: ans.getVertices())
				{
					if(v1.getId() == v2.getId())
						continue;
					toAdd = ans.constructEdge(v1.getId(), v2.getId(), "shortest path costs", dist[v1.getId()][v2.getId()]);
					toAdd.setRequired(false);
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
	private static DirectedGraph formGc2(DirectedGraph g)
	{
		DirectedGraph copy = g.getDeepCopy();
		List<Arc> temp;
		//first eliminate same cost, parallel arcs
		for(Arc a: copy.getEdges())
		{
			temp = copy.findEdges(a.getEndpoints());
			for(Arc a2: temp)
			{
				if(a2.getId() != a.getId() && a.getCost() == a2.getCost() && a.getHead().getId() == a2.getHead().getId())
				{
					if(!a2.isRequired())
						copy.removeEdge(a2); //maybe concurrent mod issues
				}
			}
		}

		//next eliminate redundant arcs, (cij = cik + ckj)
		int n = copy.getVertices().size();
		int[][] dist = new int[n+1][n+1];
		int[][] path = new int[n+1][n+1];
		int[][] edgePath = new int[n+1][n+1];
		CommonAlgorithms.fwLeastCostPaths(copy, dist, path, edgePath);

		int tempCost, tailId,headId;
		for(Arc a: copy.getEdges())
		{
			if(a.isRequired())
				continue;
			tempCost = a.getCost();
			tailId = a.getTail().getId();
			headId = a.getHead().getId();
			for(int i = 0; i < n; i++)
			{
				if(tempCost == dist[tailId][i] + dist[i][headId])
					copy.removeEdge(a);
			}
		}
		return copy;
	}
	private static DirectedGraph collapseGraph(DirectedGraph g)
	{
		//form the undirected graph with only required arcs corresponding to edges,
		//then figure out connected components.  Finally, collapse around these sets,
		//and choose the min cost arc joining two clusters to be the representative.
		try
		{
			int n = g.getVertices().size();
			int numReqArcs = 0;
			for(Arc a: g.getEdges())
			{
				if(a.isRequired())
				{
					numReqArcs++;
				}
			}
			int[] edgei = new int[numReqArcs+1];
			int[] edgej = new int[numReqArcs+1];
			int[] component = new int[n+1];
			int i = 1;
			for(Arc a: g.getEdges())
			{
				if(a.isRequired())
				{
					edgei[i] = a.getTail().getId();
					edgej[i++] = a.getHead().getId();
				}
			}

			CommonAlgorithms.connectedComponents(n, numReqArcs, edgei, edgej, component);

			//now collapse them
			DirectedGraph ans = new DirectedGraph();
			for(int j = 0; j < component[0]; j++)
			{
				ans.addVertex(new DirectedVertex("collapsed"));
			}

			//figure out who to collapse
			int tempCost, tailId, headId;
			Pair<Integer> tempKey;
			//keys are the components its connecting, and values are the best cost / arc id pair going between them
			HashMap<Pair<Integer>,Pair<Integer>> bestConnections = new HashMap<Pair<Integer>, Pair<Integer>>();
			for(Arc a: g.getEdges())
			{
				//we only want arcs that aren't internal to the connected components.
				if(!a.isRequired())
				{
					tempCost = a.getCost();
					tailId = component[a.getTail().getId()];
					headId = component[a.getHead().getId()];
					tempKey = new Pair<Integer>(tailId, headId);
					if(!bestConnections.containsKey(tempKey) || tempCost < bestConnections.get(tempKey).getFirst())
					{
						bestConnections.put(tempKey, new Pair<Integer>(tempCost,a.getId()));
					}
				}
			}

			for(Pair<Integer> key: bestConnections.keySet())
			{
				ans.addEdge(key.getFirst(), key.getSecond(), "components", bestConnections.get(key).getFirst(), bestConnections.get(key).getSecond());
			}

			return ans;

		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_RURAL_POSTMAN;
	}

	@Override
	protected Problem getInstance() {
		return mInstance;
	}

}
