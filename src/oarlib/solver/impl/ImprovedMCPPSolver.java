package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import oarlib.core.MixedEdge;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.MixedCPP;
import oarlib.vertex.impl.MixedVertex;

public class ImprovedMCPPSolver extends Solver {

	MixedCPP mInstance;
	public ImprovedMCPPSolver(MixedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}
	/**
	 * The first cost modification method proposed by Yaoyuenyong.  This reduces costs of edges / arcs added from evendegree's matching
	 * procedure.  These graphs are used for shortest path costs only.
	 * @param Gij - the graph to be modified ( in almost all cases, the original graph G )
	 * @param type - the ith entry holds the type/status of the ith edge in G as it appears in G*
	 * @param Em - An arraylist of edges (undirected) added to G during evendegree's matching phase.
	 * @param Am - An arraylist of arcs (directed) added to G during evendegree's matching.
	 */
	private MixedGraph CostMod1(MixedGraph Gij, String[] type, ArrayList<MixedEdge> Em, ArrayList<MixedEdge> Am) throws IllegalArgumentException
	{
		try {
			int K = -1; //labeled 'attractive cost'
			MixedGraph ans = Gij.getDeepCopy(); //the modified Gij we'll return.  We don't' want to modify the actual guy cuz we don't to actually change the edge costs
			HashMap<Integer, MixedEdge> ansEdges = ans.getInternalEdgeMap();
			MixedEdge temp;
			for(MixedEdge e: Em)
			{
				if(e.isDirected())
					throw new IllegalArgumentException("Em is malformed.");
				//reduce the cost of this edge
				temp = ansEdges.get(e.getMatchId());
				temp.setCost(K);
			}
			for(MixedEdge a: Am)
			{
				if(!a.isDirected())
					throw new IllegalArgumentException("Am is malformed.");
				//reduce the cost of this arc
				temp = ansEdges.get(a.getMatchId());
				if(type[a.getMatchId()] == "e")
				{
					temp.setCost(0);
				}
				else if(type[a.getMatchId()] == "f")
				{
					temp.setCost(K);
					ans.addEdge(new MixedEdge("", new Pair<MixedVertex>(temp.getHead(), temp.getTail()), K, true));
				}
				else
				{
					throw new IllegalArgumentException("Wrong type.");
				}
			} 
			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * The second cost modification method proposed by Yoayuenyong.  This reduces the cost of edges / arcs that stand to benefit from cycle
	 * elimination during the improvement procedure (in other words, if an added arc / edge gets deleted as a part of deleting a circuit, we want to 
	 * take that into consideration when thinking about which cycles to eliminate). 
	 * @param Gij
	 * @param G
	 * @param type
	 * @param i
	 * @param j
	 */
	private MixedGraph CostMod2(MixedGraph Gij, MixedGraph G, String[] type, int i, int j)
	{
		try {
			String temp;
			int cost;
			MixedEdge e, e2;
			MixedGraph ans = Gij.getDeepCopy();
			HashMap<Integer, MixedEdge> gEdges = G.getInternalEdgeMap();
			HashMap<Integer, MixedEdge> ansEdges = ans.getInternalEdgeMap();
			for(int k = 1; k < type.length; k++)
			{
				temp = type[k];
				if(temp == "a" || temp== "d")
				{
					e = gEdges.get(k);
					cost = e.getCost();
					ansEdges.get(k).setCost(-cost);

				}
				else if(temp == "cij" || temp == "cji" || temp == "f")
				{
					e = gEdges.get(k);
					e2 = ansEdges.get(k);
					cost = e.getCost();
					ansEdges.get(k).setCost(-cost);
					ans.addEdge(new MixedEdge("", new Pair<MixedVertex>(e2.getHead(), e2.getTail()), -cost, true));
				}
			}
			//now delete all links between vertex i and vertex j
			HashMap<Integer, MixedVertex> ansVertices = ans.getInternalVertexMap();
			List<MixedEdge> toRemove = ans.findEdges(new Pair<MixedVertex>(ansVertices.get(i), ansVertices.get(j)));
			toRemove.addAll(ans.findEdges(new Pair<MixedVertex>(ansVertices.get(j), ansVertices.get(i))));
			for(MixedEdge elim: toRemove)
			{
				ans.removeEdge(elim);
			}

			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	@Override
	protected Problem getInstance() {
		return mInstance;
	}
	@Override
	protected Collection<Route> solve() {
		//Compute the inputs G, Gm and G star
		MixedGraph G = mInstance.getGraph(); //original
		MixedGraph Gm = G.getDeepCopy();
		MixedGraph Gstar = G.getDeepCopy();

		//Vars for bookkeeping
		ArrayList<MixedEdge> U = new ArrayList<MixedEdge>();
		ArrayList<MixedEdge> M = new ArrayList<MixedEdge>();
		ArrayList<MixedEdge> Em = new ArrayList<MixedEdge>();
		ArrayList<MixedEdge> Am = new ArrayList<MixedEdge>();
		ArrayList<Boolean> inMdubPrime =  new ArrayList<Boolean>();
		int m = G.getEdges().size();
		String[] type = new String[m+1];

		CommonAlgorithms.evenDegree(Gm, Em, Am);
		CommonAlgorithms.inOutDegree(Gstar, U, M, inMdubPrime, type);

		//start SAPH
		boolean improvements = true; //whether or not improvements were made in in block 2
		boolean acdfImprovements = true; //whether or not we need to keep looking through the list for potential acdf improvements
		boolean bImprovements = true; //whether or not we need to keep looking through the list for potential b improvements
		MixedGraph Gij1;
		MixedGraph Gij2;
		String iStat = "";
		int idToImprove, i, j;
		int n = G.getVertices().size();
		int[][] dist;
		int[][] path, path2;
		int cost1, cost2;
		MixedEdge toImprove, toEdit;
		HashMap<Integer, MixedEdge> gEdges = G.getInternalEdgeMap();
		HashMap<Integer, MixedVertex> gij1Vertices;
		HashMap<Integer, MixedEdge> gij1Edges;
		List<MixedEdge> toRemove;
		MixedVertex u,v;
		int curr, next, end;
		while(improvements)
		{

			//SAPH Concept 1
			while(acdfImprovements) // links of type acdf in Gstar, so we carry out the first part of SAPH
			{
				//pick a random one
				idToImprove = 0;
				for(int k = 1; k < m+1; k++)
				{
					iStat = type[k];
					if(iStat == "a" || iStat == "cij" || iStat == "cji" || iStat == "d" || iStat == "f")
					{
						idToImprove = k;
						break;
					}
				}
				if(idToImprove == 0) //no acdf edges left
					break;
				toImprove = gEdges.get(idToImprove);
				i = toImprove.getEndpoints().getFirst().getId();
				j = toImprove.getEndpoints().getSecond().getId();
				//initialize
				Gij1 = G.getDeepCopy();
				Gij2 = G.getDeepCopy();
				gij1Vertices = Gij1.getInternalVertexMap();
				gij1Edges  = Gij1.getInternalEdgeMap();
				
				CostMod1(Gij1, type, Em, Am);
				CostMod2(Gij1, G, type, i, j);
				CostMod2(Gij2, G, type, i, j);
				
				//set Cij and Cji to infinity in Gij1
				toRemove = Gij1.findEdges(new Pair<MixedVertex>(gij1Vertices.get(i), gij1Vertices.get(j)));
				toRemove.addAll(Gij1.findEdges(new Pair<MixedVertex>(gij1Vertices.get(j), gij1Vertices.get(i))));
				for(MixedEdge elim: toRemove)
				{
					Gij1.removeEdge(elim);
				}
				
				//solve the shortest paths problem in Gij1
				dist = new int[n+1][n+1];
				path = new int[n+1][n+1];
				path2 = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(Gij1, dist, path);
				dist = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(Gij2, dist, path2);
				//now branch off; if we're type a or d, we need to check both directions SP ij, and SP ji; if we're type c or f, just SP ij
				if(iStat == "a" || iStat == "d")
				{
					cost1 = 0;
					cost2 = 0;
					//SP ij
					curr = i;
					end = j;
					do
					{
						next = path[curr][end];
						cost1 += dist[curr][next];
					} while((curr = next) != end);
					//SP ji
					curr = j;
					end = i;
					do
					{
						next = path[curr][end];
						cost2 += dist[curr][next];
					}while((curr = next) != end);
						
					//now do the cost comparisons to decide whether it's fruitful to replace
				}
				else //it's cij, cji, or f
				{
					cost1 = 0;
					curr = (iStat == "cji")?j:i;
					end = (iStat == "cji")?i:j;
					do
					{
						next = path[curr][end];
						cost1 += dist[curr][next];
					} while((curr = next) != end);
					
					//now do the cost comparisons to decide whether it's fruitful to replace
					
				}
				
				//to check for directed cycles, make a graph ONLY using the arcs in Mdubprime, as those are the only we 
				//can afford to delete
				
				//update the types by traversing our temp graph and checking the number of added edges parallel to it
			}
			
			while(bImprovements) // links of type b in Gstar, so we carry out the first part of SAPH
			{
				
			}
		}
		//replace any remaining type a's with  type d's
		


		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Type getProblemType() {
		return Problem.Type.MIXED_CHINESE_POSTMAN;
	}
}
