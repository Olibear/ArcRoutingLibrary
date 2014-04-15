package oarlib.graph.graphgen;

import java.util.HashMap;
import java.util.HashSet;

import oarlib.core.WindyEdge;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.WindyVertex;

public class WindyGraphGenerator extends GraphGenerator{

	public WindyGraphGenerator(){super();}
	@Override
	public WindyGraph generateGraph(int n, int maxCost, boolean connected,
			double density) throws IllegalArgumentException{

		
		//edge cases
		if(n < 0)
			throw new IllegalArgumentException();
		if(n == 0)
			return new WindyGraph();
		
		try {
			//ans graph
			WindyGraph ans = new WindyGraph();

			//set up the vertices
			for (int i=0; i<n; i++)
			{
				ans.addVertex(new WindyVertex("Original"));
			}

			if(n == 1)
				return ans;
			
			HashMap<Integer,WindyVertex> indexedVertices = ans.getInternalVertexMap();

			//figure out what is set
			maxCost = (maxCost<0)?Integer.MAX_VALUE:maxCost;
			density = (density > 0 && density < 1)? density:Math.random();


			//randomly add edges
			int cost, reverseCost;
			for(int j = 2; j <= n; j++)
			{
				for(int k = 1; k < j; k++)
				{
					//add the arc with probability density
					if(Math.random() < density)
					{
						cost = (int)Math.round(maxCost * Math.random());
						reverseCost = (int)Math.round(maxCost * Math.random());
						ans.addEdge(new WindyEdge("Original", new Pair<WindyVertex>(indexedVertices.get(k), indexedVertices.get(j)), cost, reverseCost));
					}
				}
			}

			//enforce connectedness
			if(connected)
			{
				//get the Strongly Connected Components of the graph, and add an arc for each direction between them.
				int[] component = new int[n+1];
				int m = ans.getEdges().size();
				int[] nodei = new int[m+1];
				int[] nodej = new int[m+1];
				for(WindyEdge e: ans.getEdges())
				{
					nodei[e.getId()] = e.getEndpoints().getFirst().getId();
					nodej[e.getId()] = e.getEndpoints().getSecond().getId();
				}
				CommonAlgorithms.connectedComponents(n, m, nodei, nodej, component);
				//if we need to connect guys
				if (component[0] != 1)
				{
					//keep track of who we've already connected up.  If we haven't connected vertex i yet, then add connections to/from lastcandidate
					//(the last guy we connected) to currcandidate (whichever vertex belongs to a CC we haven't connected yet.
					HashSet<Integer> alreadyIntegrated = new HashSet<Integer>();
					WindyVertex lastCandidate = indexedVertices.get(1);
					WindyVertex currCandidate;
					for (int i=1; i<component.length;i++)
					{
						if(alreadyIntegrated.contains(component[i]))
							continue;
						alreadyIntegrated.add(component[i]);
						currCandidate = indexedVertices.get(i);
						cost =(int)Math.round(Math.random()*maxCost);
						reverseCost = (int)Math.round(Math.random()*maxCost);
						ans.addEdge(new WindyEdge("To ensure connectivity.", new Pair<WindyVertex>(lastCandidate, currCandidate), cost, reverseCost));
					}
				}
			}

			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public WindyGraph generateEulerianGraph(int n, int maxCost,
			boolean connected, double density) {
		try {
			WindyGraph g = this.generateGraph(n, maxCost, connected, density);
			//make Eulerian
			WindyVertex temp = null;
			boolean lookingForPartner = false;
			int cost, reverseCost;
			for(WindyVertex v: g.getVertices())
			{
				//if odd degree
				if(v.getDegree() % 2 == 1)
				{
					//either set temp, or connect it with temp
					if(lookingForPartner)
					{
						cost =(int)Math.round(maxCost * Math.random());
						reverseCost = (int)Math.round(maxCost * Math.random());
						g.addEdge(new WindyEdge("to make Eulerian", new Pair<WindyVertex>(temp, v), cost, reverseCost));
						lookingForPartner = false;
					}
					else
					{
						temp = v;
						lookingForPartner = true;
					}
				}
					
			}
			return g;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
