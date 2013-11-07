package oarlib.graph.graphgen;

import java.util.HashMap;
import java.util.HashSet;

import oarlib.core.Arc;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

public class DirectedGraphGenerator extends GraphGenerator{

	public DirectedGraphGenerator(){ super(); }
	@Override
	public DirectedGraph generateGraph(int n, int maxCost, boolean connected,
			double density) {
		try {
			//ans graph
			DirectedGraph ans = new DirectedGraph();

			//set up the vertices
			for (int i=0; i<n; i++)
			{
				ans.addVertex(new DirectedVertex("Original"));
			}

			HashMap<Integer,DirectedVertex> indexedVertices = ans.getInternalVertexMap();

			//figure out what is set
			maxCost = (maxCost<0)?Integer.MAX_VALUE:maxCost;
			density = (density > 0 && density < 1)? density:Math.random();

			//add arcs randomly
			for(int j = 1; j <= n; j++)
			{
				for(int k = 1; k <= n; k++)
				{
					if(j==k)
						continue;
					//add the arc with probability density
					if(Math.random() < density)
						ans.addEdge(new Arc("Original", new Pair<DirectedVertex>(indexedVertices.get(k), indexedVertices.get(j)), (int)Math.round(maxCost * Math.random())));
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
				for(Arc a: ans.getEdges())
				{
					nodei[a.getId()] = a.getEndpoints().getFirst().getId();
					nodej[a.getId()] = a.getEndpoints().getSecond().getId();
				}
				CommonAlgorithms.stronglyConnectedComponents(n, m, nodei, nodej, component);
				//if we need to connect guys
				if (component[0] != 1)
				{
					//keep track of who we've already connected up.  If we haven't connected vertex i yet, then add connections to/from lastcandidate
					//(the last guy we connected) to currcandidate (whichever vertex belongs to a CC we haven't connected yet.
					HashSet<Integer> alreadyIntegrated = new HashSet<Integer>();
					DirectedVertex lastCandidate = indexedVertices.get(1);
					DirectedVertex currCandidate;
					for (int i=1; i<component.length;i++)
					{
						if(alreadyIntegrated.contains(component[i]))
							continue;
						alreadyIntegrated.add(component[i]);
						currCandidate = indexedVertices.get(i);
						ans.addEdge(new Arc("To ensure connectivity.", new Pair<DirectedVertex>(lastCandidate, currCandidate), (int)Math.round(Math.random()*maxCost)));
						ans.addEdge(new Arc("To ensure connectivity.", new Pair<DirectedVertex>(currCandidate, lastCandidate), (int)Math.round(Math.random()*maxCost)));
					}
				}
			}

			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
