package oarlib.graph.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Link;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.exceptions.GraphInfeasibleException;
import oarlib.exceptions.NoDemandSetException;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class CommonAlgorithms {

	/**
	 * Hierholzer's algorithm for determining an Euler tour through an directed Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a ArrayList object containing the tour (values are edge ids).
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static ArrayList<Integer> tryHierholzer(DirectedGraph eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		return hierholzer(eulerianGraph);
	}
	/**
	 * Hierholzer's algorithm for determining an Euler tour through an undirected Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a ArrayList object containing the tour (values are edge ids)
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static ArrayList<Integer> tryHierholzer(UndirectedGraph eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return hierholzer(eulerianGraph);
	}
	public static ArrayList<Integer> tryHierholzer(MixedGraph eulerianGraph) throws IllegalArgumentException{
		if(!isStronglyEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return hierholzer(eulerianGraph);
	}
	/**
	 * business logic for Hierholzer's algorithm
	 * @return the Eulerian cycle
	 */
	private static ArrayList<Integer> hierholzer(Graph<? extends Vertex,? extends Link<? extends Vertex>> graph)
	{
		ArrayList<Integer> edgeTrail = new ArrayList<Integer>();
		ArrayList<Integer> edgeCycle = new ArrayList<Integer>();
		ArrayList<Vertex> visitedVertices = new ArrayList<Vertex>();
		ArrayList<Vertex> simpleCycle = new ArrayList<Vertex>();
		int m = graph.getEdges().size();

		//pick an arbitrary start vertex
		Iterator<? extends Vertex> iter = graph.getVertices().iterator();
		Vertex start = iter.next();
		visitedVertices.add(start);
		simpleCycle.add(start);
		//initialize current position variables
		Map<? extends Vertex,? extends List<? extends Link<? extends Vertex>>> currNeighbors = start.getNeighbors();
		Vertex currVertex = start;
		Vertex prevVertex = null;
		Link<? extends Vertex> currEdge = new Arc("dummy",null,0);
		Iterator<Vertex> vertexIter;
		boolean nextStart = true;


		while(nextStart)
		{
			//greedily go until we've come back to start
			do {

				currEdge = currNeighbors.values().iterator().next().get(0); //grab anybody
				edgeCycle.add(currEdge.getId()); //add it to the trail
				//update the currVertex
				prevVertex = currVertex;
				currVertex = (currEdge.getEndpoints().getFirst().getId() == currVertex.getId())?currEdge.getEndpoints().getSecond():currEdge.getEndpoints().getFirst();
				simpleCycle.add(currVertex);

				//update the neighbors
				currNeighbors.get(currVertex).remove(currEdge);
				if(currNeighbors.get(currVertex).size() == 0)
					currNeighbors.remove(currVertex);

				currNeighbors = currVertex.getNeighbors();
				if(!currEdge.isDirected())
				{
					currNeighbors.get(prevVertex).remove(currEdge);
					if(currNeighbors.get(prevVertex).size() == 0)
						currNeighbors.remove(prevVertex);
				}

			} while (currVertex.getId() != start.getId());

			//join the trails
			int n = visitedVertices.size();
			for(int j = 0; j < n; j++)
			{
				//insert here
				if(visitedVertices.get(j) == start)
				{
					visitedVertices.remove(j);
					visitedVertices.addAll(j, simpleCycle);
					edgeTrail.addAll(j, edgeCycle);
					break;
				}
			}

			//reinitialize the simple cycle trackers for the next go
			simpleCycle = new ArrayList<Vertex>();
			edgeCycle = new ArrayList<Integer>();

			//look for a new start point
			nextStart = false;
			vertexIter = visitedVertices.iterator();
			while (vertexIter.hasNext())
			{
				start = vertexIter.next();
				if(start.getNeighbors().size() != 0)
				{
					simpleCycle.add(start);
					currVertex = start;
					currNeighbors = currVertex.getNeighbors();
					nextStart = true;
					break;
				}
			}
		}
		return edgeTrail;
	}
	/**
	 * FindRoute algorithm (alternative to Fleury's given in Dussault et al. Plowing with Precedence
	 * @return the Eulerian cycle
	 */
	public static Route findRoute(Graph<? extends Vertex,? extends Link<? extends Vertex>> graph)
	{
		return null;
	}
	/**
	 * Checks to see if the directed graph is weakly connected
	 * @return true if the graph is  weakly connected, false oth.
	 */
	public static boolean isWeaklyConnected(DirectedGraph graph) 
	{
		return false;
	}
	/**
	 * Checks to see if the directed graph is strongly connected
	 * @return true if the graph is strongly  connected, false oth.
	 */
	public static boolean isStronglyConnected(DirectedGraph graph)
	{
		int n = graph.getVertices().size();
		int m = graph.getEdges().size();
		int[] component = new int[n+1];
		int[] nodei = new int[m+1];
		int[] nodej = new int[m+1];
		int index = 1;
		Iterator<? extends Arc> iter = graph.getEdges().iterator();
		while (iter.hasNext())
		{
			Arc a = iter.next();
			nodei[index] = a.getTail().getId();
			nodej[index] = a.getHead().getId();
			index++;
		}
		stronglyConnectedComponents(n,m,nodei,nodej,component);
		if(component[0] == 1)
			return true;
		return false;
	}
	/* 
	 * Taken from Lau.  Returns the connected components of an undirected graph.  For the directed analog, see stronglyConnectedComponents
	 * @param n - the number of nodes in the graph
	 * @param m - the number of edges in the graph
	 * @param nodei - the pth entry holds one end of the pth edge
	 * @param nodej - the pth entry holds the other end of the pth edge
	 * @param component - 0th entry holds the number of connected components, while the pth entry holds the component that node p belongs to.
	 */
	public static void connectedComponents(int n, int m, int nodei[], int nodej[],
			int component[])
	{

		//check for no edges
		if (m == 0)
		{
			component[0] = n;
			for(int i=1; i<n+1;i++)
				component[i] = i;
			return;
		}
		int edges,i,j,numcomp,p,q,r,typea,typeb,typec,tracka,trackb;
		int compkey,key1,key2,key3,nodeu,nodev;
		int numnodes[] = new int[n+1];
		int aux[] = new int[n+1];
		int index[] = new int[3];

		typec=0;
		index[1] = 1;
		index[2] = 2;
		q = 2;
		for (i=1; i<=n; i++) {
			component[i] = -i;
			numnodes[i] = 1;
			aux[i] = 0;   
		}   
		j = 1;
		edges = m;
		do {
			nodeu = nodei[j];
			nodev = nodej[j];
			key1 = component[nodeu];
			if (key1 < 0) key1 = nodeu;
			key2 = component[nodev];
			if (key2 < 0) key2 = nodev;
			if (key1 == key2) {
				if(j >= edges) {
					edges--;
					break;
				}
				nodei[j] = nodei[edges];
				nodej[j] = nodej[edges];
				nodei[edges] = nodeu;
				nodej[edges] = nodev;
				edges--;
			}
			else {
				if (numnodes[key1] >= numnodes[key2]) {
					key3 = key1;
					key1 = key2;
					key2 = key3;
					typec = -component[key2];
				}
				else
					typec = Math.abs(component[key2]);
				aux[typec] = key1;
				component[key2] = component[key1];
				i = key1;
				do {
					component[i] = key2;
					i = aux[i];
				} while (i != 0);
				numnodes[key2] += numnodes[key1];
				numnodes[key1] = 0;
				j++;
				if (j > edges || j > n) break;
			}
		} while (true);
		numcomp = 0;
		for (i=1; i<=n; i++)
			if (numnodes[i] != 0) {
				numcomp++;
				numnodes[numcomp] = numnodes[i];
				aux[i] = numcomp;
			}
		for (i=1; i<=n; i++) {
			key3 = component[i];
			if (key3 < 0) key3 = i;
			component[i] = aux[key3];
		}      
		if (numcomp == 1) {
			component[0] = numcomp;
			return;
		}
		typeb = numnodes[1];
		numnodes[1] = 1;
		for (i=2; i<=numcomp; i++) {
			typea = numnodes[i];
			numnodes[i] = numnodes[i-1] + typeb - 1;
			typeb = typea;
		}
		for (i=1; i<=edges; i++) {
			typec = nodei[i];
			compkey = component[typec];
			aux[i] = numnodes[compkey];
			numnodes[compkey]++;
		}
		for (i=1; i<=q; i++) {
			typea = index[i];
			do {
				if (typea <= i) break;
				typeb = index[typea];
				index[typea] = -typeb;
				typea = typeb;
			} while (true);
			index[i] = -index[i];
		}
		if (aux[1] >= 0)
			for (j=1; j<=edges; j++) {
				tracka = aux[j];
				do {
					if (tracka <= j) break;
					trackb = aux[tracka];
					aux[tracka] = -trackb;
					tracka = trackb;
				} while (true);
				aux[j] = -aux[j];
			}
		for (i=1; i<=q; i++) {
			typea = -index[i];
			if(typea >= 0) {
				r = 0;
				do {
					typea = index[typea];
					r++;
				} while (typea > 0);
				typea = i;
				for (j=1; j<=edges; j++)
					if (aux[j] <= 0) {
						trackb = j;
						p = r;
						do {
							tracka = trackb;
							key1 = (typea == 1) ? nodei[tracka] : nodej[tracka];
							do {
								typea = Math.abs(index[typea]);
								key1 = (typea == 1) ? nodei[tracka] : nodej[tracka];
								tracka = Math.abs(aux[tracka]);
								key2 = (typea == 1) ? nodei[tracka] : nodej[tracka];
								if (typea == 1)
									nodei[tracka] = key1;
								else
									nodej[tracka] = key1;
								key1 = key2;
								if (tracka == trackb) {
									p--;
									if (typea == i) break; 
								}
							} while (true);
							trackb = Math.abs(aux[trackb]);
						} while (p != 0);
					}
			}
		}
		for (i=1; i<=q; i++)
			index[i] = Math.abs(index[i]);
		if (aux[1] > 0) {
			component[0] = numcomp;
			return;
		}
		for (j=1; j<=edges; j++)
			aux[j] = Math.abs(aux[j]);
		typea=1;
		for (i=1; i<=numcomp; i++) {
			typeb = numnodes[i];
			numnodes[i] = typeb - typea + 1;
			typea = typeb;
		}
		component[0] = numcomp;
	}
	/**
	 * Taken from Lau.  Gets the SCCs of a directed graph.  For the undirected analog, check connectedComponents.
	 * @param n - number of nodes in the graph
	 * @param m - number of edges in the graph
	 * @param nodei - the pth entry holds the tail of the pth edge
	 * @param nodej - the pth entry holds the head of the pth edge
	 * @param component - 0th entry is the number of SCCs, and the pth entry is the component that node p belongs to
	 */
	public static void stronglyConnectedComponents(int n, int m, int nodei[],
			int nodej[], int component[])
	{
		int i,j,k,series,stackpointer,numcompoents,p,q,r;
		int backedge[] = new int[n+1];
		int parent[] = new int[n+1];
		int sequence[] = new int[n+1];
		int stack[] = new int[n+1];
		int firstedges[] = new int[n+2];
		int endnode[] = new int[m+1];
		boolean next[] = new boolean[n+1];
		boolean trace[] = new boolean[n+1];
		boolean fresh[] = new boolean[m+1];
		boolean skip,found;

		// set up the forward star representation of the graph
		firstedges[1] = 0;
		k = 0;
		for (i=1; i<=n; i++) {
			for (j=1; j<=m; j++)
				if (nodei[j] == i) {
					k++;
					endnode[k] = nodej[j];
				}
			firstedges[i+1] = k;
		}
		for (j=1; j<=m; j++)
			fresh[j] = true;
		// initialize
		for (i=1; i<=n; i++) {
			component[i] = 0;
			parent[i] = 0;
			sequence[i] = 0;
			backedge[i] = 0;
			next[i] = false;
			trace[i] = false;
		}
		series = 0;
		stackpointer = 0;
		numcompoents = 0;
		// choose an unprocessed node not in the stack
		while (true) {
			p = 0;
			while (true) {
				p++;
				if (n < p) {
					component[0] = numcompoents;
					return;
				}
				if (!trace[p]) break;
			}
			series++;
			sequence[p] = series;
			backedge[p] = series;
			trace[p] = true;
			stackpointer++;
			stack[stackpointer] = p;
			next[p] = true;
			while (true) {
				skip = false;
				for (q=1; q<=n; q++) {
					// find an unprocessed edge (p,q)
					found = false;
					for (i=firstedges[p]+1; i<=firstedges[p+1]; i++)
						if ((endnode[i] == q) && fresh[i]) {
							// mark the edge as processed
							fresh[i] = false;
							found = true;
							break;
						}
					if (found) {
						if (!trace[q]) {
							series++;
							sequence[q] = series;
							backedge[q] = series;
							parent[q] = p;
							trace[q] = true;
							stackpointer++;
							stack[stackpointer] = q;
							next[q] = true;
							p = q;
						}
						else {
							if (trace[q]) {
								if (sequence[q] < sequence[p] && next[q]) {
									backedge[p] = (backedge[p] < sequence[q]) ? 
											backedge[p] : sequence[q];
								}
							}
						}
						skip = true;
						break;
					}  
				}
				if (skip) continue;
				if (backedge[p] == sequence[p]) {
					numcompoents++;
					while (true) {
						r = stack[stackpointer];
						stackpointer--;
						next[r] = false;
						component[r] = numcompoents;
						if (r == p) break;
					}
				}
				if (parent[p] != 0) {
					backedge[parent[p]] = (backedge[parent[p]] < backedge[p]) ? 
							backedge[parent[p]] : backedge[p];
							p = parent[p];
				}
				else
					break;
			}
		}
	}
	/**
	 * Checks to see if the undirected graph is connected
	 * @return true if the graph is connected (or empty), false oth.
	 */
	public static boolean isConnected(UndirectedGraph graph)
	{
		//start at an arbitrary vertex
		HashSet<UndirectedVertex> vertices = graph.getVertices();
		HashSet<UndirectedVertex> nextUp = new HashSet<UndirectedVertex>();
		if(vertices.size() <= 1)
			return true; //trivially connected
		nextUp.add(vertices.iterator().next());

		//DFS
		Iterator<UndirectedVertex> iter;
		while (vertices.size() > 0 && nextUp.size() > 0)
		{
			iter = nextUp.iterator();
			while (iter.hasNext())
			{
				UndirectedVertex v = iter.next();
				vertices.remove(v);
				nextUp.addAll(v.getNeighbors().keySet());
			}
		}
		if(vertices.size() == 0)
		{
			return true;
		}
		return false;
	}
	/**
	 * Checks to see if the directed graph is eulerian.
	 * @param graph
	 * @return true if the graph is eulerian, false oth.
	 */
	public static boolean isEulerian (DirectedGraph graph)
	{
		for(DirectedVertex v: graph.getVertices())  
		{
			if (v.getInDegree() != v.getOutDegree())
				return false;
		}
		return true;
	}
	/**
	 * Checks to see if the undirected graph is eulerian.
	 * @param graph
	 * @return true if the graph is eulerian, false oth.
	 */
	public static boolean isEulerian(UndirectedGraph graph) 
	{
		for (UndirectedVertex v:graph.getVertices()) 
		{
			if (v.getDegree() % 2 == 1)
				return false;
		}
		return true;
	}
	/**
	 * Checks to see if the mixed graph is strongly eulerian.  By 'strongly eulerian' we mean that all nodes are both
	 * balanced (in-degree = out-degree) and even (the parity of the number of incident undirected edges of each
	 * vertex is even).  This guarantees that the graph is eulerian, but it is strictly speaking not necessary.  However,
	 * the necessary and sufficient conditions are computationally very expensive to check, and all heuristics implemented
	 * use this notion of Eulerian anyways.
	 * @param graph
	 * @return true if the graph is eulerian, false oth.
	 */
	public static boolean isStronglyEulerian(MixedGraph graph)
	{
		for(MixedVertex v: graph.getVertices())
		{
			if(v.getDegree() % 2 == 1)
				return false;
			if(v.getDelta() != 0)
				return false;
		}
		return true;
	}
	/**
	 * Computes / encodes the shortest paths between all pairs of nodes in the network.  Taken from Lau:
	 * @param n - number of nodes in the graph.
	 * @param dist - a matrix where entry i,j is the cost of the edge between node i and node j, 0 if i = j, and big if there is no connecting link.
	 * After the conclusion of the algorithm, it holds shortest path costs.
	 * @param big - a number greater than the sum of all the edge costs.
	 * @param startnode - if a specific path is requested to be stored in path, this is the start node.  Send 0 for no request.
	 * @param endnode - if a specific path is requested to be stored in path, this is the end node
	 * @param path - holds the path if it is requested from startnode to endnode
	 * @param next - holds the matrix which encodes the information to reconstruct the shortest path between any two nodes.
	 * @return path will hold the requested path, if one is requested
	 *  next will hold the matrix from which it is possible to reconstruct the shortest path between any two nodes
	 *  
	 */
	public static void allPairsShortestPaths(int n, int dist[][], int big,
			int startnode, int endnode,int[] path, int[][] next)
	{
		int i,j,k,d,num,node;
		int order[] = new int[n+1];

		// compute the shortest path distance matrix
		for (i=1; i<=n; i++)
			for (j=1; j<=n; j++)
				next[i][j] = i;
		for (i=1; i<=n; i++)
			for (j=1; j<=n; j++)
				if (dist[j][i] < big)
					for (k=1; k<=n; k++)
						if (dist[i][k] < big) {
							d = dist[j][i] + dist[i][k];
							if (d < dist[j][k]) {
								dist[j][k] = d;
								next[j][k] = next[i][k];
							}
						}
		// find the shortest path from startnode to endnode
		if (startnode == 0) return;
		j = endnode;
		num = 1;
		order[num] = endnode;
		while (true) {
			node = next[startnode][j];
			num++;
			order[num] = node;
			if (node == startnode) break;
			j = node;
		}
		for (i=1; i<=num; i++)
			path[i] = order[num-i+1];
		path[0] = num;
	}
	/**
	 * Implements the Floyd-Warshall shortest paths algorithm.
	 * @param g - the graph in which the shortest paths should be calculated
	 * @param dist - an [n+1][n+1] matrix that will be filled with shortest paths at the end: the 0th column and row 
	 * will be filled with Integer.MAX, and dist[i][j] will hold the shortest path cost between node i and node j.
	 * @param path - an [n+1][n+1] matrix that will tell us how to reconstruct the shortest path: the 0th column and row
	 * will be filled with Integer.MAX, and path[i][j] holds the id of the node to go to next in the shortest path from node i t node j.
	 */
	public static void fwLeastCostPaths(Graph<? extends Vertex, ? extends Link<? extends Vertex>> g, int[][] dist, int[][] path) throws IllegalArgumentException
	{
		//initialize dist and path
		int n = g.getVertices().size();
		if(dist.length != n+1 || path.length != n+1)
			throw new IllegalArgumentException();

		//initialize dist and path
		for(int i=0;i<=n;i++)
		{
			for(int j=0;j<=n;j++)
			{
				dist[i][j] = Integer.MAX_VALUE;
			}
			path[0][i] = Integer.MAX_VALUE;
			path[i][0] = Integer.MAX_VALUE;
		}

		Vertex vi;
		int min;
		for(int i=1;i<=n;i++)
		{
			vi = g.getInternalVertexMap().get(i);
			for(Vertex v :vi.getNeighbors().keySet())
			{
				List<? extends Link<?extends Vertex>> l = vi.getNeighbors().get(v);
				min = Integer.MAX_VALUE;
				for (Link<? extends Vertex> link: l)
				{
					if(link.getCost() < min)
						min = link.getCost();
				}
				dist[vi.getId()][v.getId()] = min;
				path[vi.getId()][v.getId()] = v.getId();
			}
		}

		//business logic
		for (int k = 1; k <= n; k++ )
		{
			for( int i = 1; i <= n; i ++)
			{
				//if there is an edge from i to k
				if (dist[i][k] < Integer.MAX_VALUE)
					for (int j = 1; j <= n; j++)
					{
						//if there is an edge from k to j
						if(dist[k][j] < Integer.MAX_VALUE
								&& (!(dist[i][j] < Integer.MAX_VALUE) || dist[i][j] > dist[i][k] + dist[k][j]) )
						{
							path[i][j] = path[i][k];
							dist[i][j] = dist[i][k] + dist[k][j];
							if (i==j && dist[i][j] < 0)
								return; //negative cycle
						}
					}

			}
		}
	}
	public static void fwLeastCostPaths(Graph<? extends Vertex, ? extends Link<? extends Vertex>> g, int[][] dist, int[][] path, int[][] edgePath) throws IllegalArgumentException
	{
		//initialize dist and path
		int n = g.getVertices().size();
		if(dist.length != n+1 || path.length != n+1 || edgePath.length != n+1)
			throw new IllegalArgumentException();

		//initialize dist and path
		for(int i=0;i<=n;i++)
		{
			for(int j=0;j<=n;j++)
			{
				dist[i][j] = Integer.MAX_VALUE;
			}
			path[0][i] = Integer.MAX_VALUE;
			path[i][0] = Integer.MAX_VALUE;
			edgePath[0][i] = Integer.MAX_VALUE;
			edgePath[i][0] = Integer.MAX_VALUE;
		}

		Vertex vi;
		int min;
		for(int i=1;i<=n;i++)
		{
			vi = g.getInternalVertexMap().get(i);
			for(Vertex v :vi.getNeighbors().keySet())
			{
				List<? extends Link<?extends Vertex>> l = vi.getNeighbors().get(v);
				min = Integer.MAX_VALUE;
				Link<? extends Vertex> edge = null;
				for (Link<? extends Vertex> link: l)
				{
					if(link.getCost() < min)
					{
						min = link.getCost();
						edge = link;
					}
				}
				dist[vi.getId()][v.getId()] = min;
				path[vi.getId()][v.getId()] = v.getId();
				edgePath[vi.getId()][v.getId()] = edge.getId();
			}
		}

		//business logic
		for (int k = 1; k <= n; k++ )
		{
			for( int i = 1; i <= n; i ++)
			{
				//if there is an edge from i to k
				if (dist[i][k] < Integer.MAX_VALUE)
					for (int j = 1; j <= n; j++)
					{
						//if there is an edge from k to j
						if(dist[k][j] < Integer.MAX_VALUE
								&& (!(dist[i][j] < Integer.MAX_VALUE) || dist[i][j] > dist[i][k] + dist[k][j]) )
						{
							path[i][j] = path[i][k];
							edgePath[i][j] = edgePath[i][k];
							dist[i][j] = dist[i][k] + dist[k][j];
							if (i==j && dist[i][j] < 0)
								return; //negative cycle
						}
					}

			}
		}
	}
	/**
	 * adds the shortest path from p1 to p2 to g.
	 * @param g - the directed graph in which to add the paths
	 * @param dist - the dist matrix (probably output from fwLeastCostPaths)
	 * @param path - the path matrix (probably output from fwLeastCostPaths)
	 * @param p - the ids (in g) of the vertices you want to add the shortest path from (to)
	 */
	public static void addShortestPath(DirectedGraph g, int[][] dist, int[][]path, Pair<Integer> p)
	{
		try {
			int curr = p.getFirst();
			int end = p.getSecond();
			int next = 0;
			int cost = 0;
			DirectedVertex u,v;
			do {
				next = path[curr][end];
				cost = dist[curr][next];
				u = g.getInternalVertexMap().get(curr);
				v = g.getInternalVertexMap().get(next);
				g.addEdge(new Arc("from addShortestPath", new Pair<DirectedVertex>(u,v), cost));
			} while ( (curr =next) != end);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void addShortestPath(DirectedGraph g, int[][] dist, int[][]path, int[][] edgePath, Pair<Integer> p)
	{
		try {
			int curr = p.getFirst();
			int end = p.getSecond();
			int next = 0;
			int nextEdge = 0;
			int cost = 0;
			DirectedVertex u,v;
			do {
				next = path[curr][end];
				nextEdge = edgePath[curr][end];
				cost = dist[curr][next];
				u = g.getInternalVertexMap().get(curr);
				v = g.getInternalVertexMap().get(next);
				g.addEdge(new Arc("from addShortestPath", new Pair<DirectedVertex>(u,v), cost), nextEdge);
			} while ( (curr =next) != end);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * adds the shortest path from p1 to p2 to g.
	 * @param g - the undirected graph in which to add the paths
	 * @param dist - the dist matrix (probably output from fwLeastCostPaths)
	 * @param path - the path matrix (probably output from fwLeastCostPaths)
	 * @param p - the ids (in g) of the vertices you want to add the shortest path from (to)
	 */
	public static void addShortestPath(UndirectedGraph g, int[][] dist, int[][]path, Pair<Integer> p)
	{
		try {
			int curr = p.getFirst();
			int end = p.getSecond();
			int next = 0;
			int cost = 0;
			UndirectedVertex u,v;
			do {
				next = path[curr][end];
				cost = dist[curr][next];
				u = g.getInternalVertexMap().get(curr);
				v = g.getInternalVertexMap().get(next);
				g.addEdge(new Edge("from addShortestPath", new Pair<UndirectedVertex>(u,v), cost));
			} while ( (curr =next) != end);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void addShortestPath(UndirectedGraph g, int[][] dist, int[][]path, int[][] edgePath, Pair<Integer> p)
	{
		try {
			int curr = p.getFirst();
			int end = p.getSecond();
			int next = 0;
			int nextEdge = 0;
			int cost = 0;
			UndirectedVertex u,v;
			do {
				next = path[curr][end];
				nextEdge = edgePath[curr][end];
				cost = dist[curr][next];
				u = g.getInternalVertexMap().get(curr);
				v = g.getInternalVertexMap().get(next);
				g.addEdge(new Edge("from addShortestPath", new Pair<UndirectedVertex>(u,v), cost), nextEdge);
			} while ( (curr =next) != end);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static DirectedGraph getResidualGraph(DirectedGraph g, HashMap<Integer, Integer> f)
	{
		try {
			DirectedGraph ans = g.getDeepCopy();
			HashSet<Arc> origEdges = new HashSet<Arc>();
			origEdges.addAll(ans.getEdges());
			for(Arc a: origEdges)
			{
				if(f.containsKey(a.getMatchId()) && f.get(a.getMatchId()) > 0)
				{
					ans.addEdge(new Arc("artificial residual edge", new Pair<DirectedVertex>(a.getHead(), a.getTail()), -a.getCost()));
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
	 * Implements the cycle cancelling algorithm to calculate a min cost flow through the graph g with distance matrix given by dist.
	 * @param g
	 */
	public static HashMap<Pair<Integer>, Integer> cycleCancelingMinCostNetworkFlow(DirectedGraph g, int[][] dist) throws IllegalArgumentException
	{
		HashMap<Pair<Integer>, Integer> ans = new HashMap<Pair<Integer>, Integer>();
		HashMap<Integer, Integer> flow = new HashMap<Integer, Integer>();
		ArrayList<DirectedVertex> Dplus = new ArrayList<DirectedVertex>();
		ArrayList<DirectedVertex> Dminus = new ArrayList<DirectedVertex>();
		//vars to check for valid demand setting
		int supply = 0;
		int demand = 0;
		for (DirectedVertex v: g.getVertices())
		{
			//only nonzero demands are set
			try {
				if (v.getDemand() > 0)
				{
					Dplus.add(v);
					supply += v.getDemand();
				}
				else
				{
					Dminus.add(v);
					demand += v.getDemand();
				}
			} catch(NoDemandSetException e) {
				//do nothing
			}
		}

		if(demand > supply || demand >= 0)
			throw new IllegalArgumentException();

		//greedily establish an initial feasible flow
		try {
			DirectedVertex u = Dplus.get(0);
			DirectedVertex v = Dminus.get(0); // holds the current Dplus and Dminus nodes respectively
			int i = 0; //counter for Dplus
			int j = 0; //counter for Dminus
			int k = 0; //amount of flow to push from u to v
			int leftover = u.getDemand();
			int leftover2 = v.getDemand(); //counters for remaining supply/demand
			while(j < Dminus.size())
			{
				k = (-leftover2 < leftover) ? -leftover2 : leftover;
				ans.put(new Pair<Integer>(u.getId(),v.getId()), k);
				leftover -= k;
				leftover2 += k;

				if (leftover == 0 && ++i != Dplus.size())
				{
					u = Dplus.get(i);
					leftover = u.getDemand();
				}
				if (leftover2 == 0 && ++j != Dminus.size())
				{
					v = Dminus.get(j);
					leftover2 = v.getDemand();
				}
			}

			//set up the reduced graph (the base for the residual)
			ArrayList<DirectedVertex> DallResid;
			DirectedVertex temp;
			Arc toAdd;
			DirectedGraph reduced = new DirectedGraph();
			DallResid = new ArrayList<DirectedVertex>();
			for(DirectedVertex v1: Dplus)
			{
				temp = new DirectedVertex("resid plus");
				reduced.addVertex(temp, v1.getId());
				DallResid.add(temp);
			}
			for(DirectedVertex v1: Dminus)
			{
				temp = new DirectedVertex("resid minus");
				reduced.addVertex(temp, v1.getId());
				DallResid.add(temp);
			}
			//add all the normal edges
			for (i = 0; i < DallResid.size(); i++)
			{
				u = DallResid.get(i);
				for(j = 0; j < DallResid.size(); j++)
				{
					if(i==j)
						continue;
					v = DallResid.get(j);
					toAdd = new Arc("reduced original", new Pair<DirectedVertex>(u,v), 1+dist[u.getMatchId()][v.getMatchId()]);
					reduced.addEdge(toAdd);
					//if it's part of our greedy solution, then record the amount of flow in flow
					Pair<Integer> key = new Pair<Integer>(u.getMatchId(),v.getMatchId());
					if (ans.containsKey(key) )
					{
						flow.put(toAdd.getId(), ans.get(key));
					}
				}
			}


			//setup the residual graph
			boolean improvements = true;
			DirectedGraph resid;
			HashMap<Integer, DirectedVertex> reducedVertexMap = reduced.getInternalVertexMap();

			//while there's a negative cycle
			while (improvements)
			{
				improvements = false;
				//preliminaries
				resid = getResidualGraph(reduced, flow);

				//solve the all pairs shortest paths
				int n = resid.getVertices().size();
				int[][] residDist = new int[n+1][n+1];
				int [][] residPath = new int[n+1][n+1];
				fwLeastCostPaths(resid, residDist, residPath);

				//cancel negative cycles
				Pair<Integer> pair;
				for (i = 1; i <= n; i++)
				{
					if(residDist[i][i] < 0) //negative cycle detected
					{
						k = 0;
						int b, c;
						int fvu = 0;
						boolean kunset = true;
						b = i;
						//calculate how much flow we can push around it
						do {
							c = residPath[b][i];
							u = resid.getInternalVertexMap().get(b);
							v = resid.getInternalVertexMap().get(c);
							List<Arc> connections = v.getNeighbors().get(u);
							for (Arc a: connections)
							{
								if(a.getCost() == -residDist[b][c] && residDist[b][c] < 0)
								{
									fvu = flow.get(a.getMatchId());
									break;
								}
							}
							if (residDist[b][c] < 0 && (kunset || k > fvu))
							{
								k = fvu;
								kunset = false;
							}
						} while ((b = c) != i);
						//cancel k units of flow around the cycle
						b = i;
						do {
							c = residPath[b][i];
							u = resid.getInternalVertexMap().get(b);
							v = resid.getInternalVertexMap().get(c);
							if (residDist[b][c]  < 0)
							{
								pair = new Pair<Integer>(reducedVertexMap.get(v.getMatchId()).getMatchId(),reducedVertexMap.get(u.getMatchId()).getMatchId());
								List<Arc> connections = v.getNeighbors().get(u); //to find the correct guy to cancel
								for(Arc a:connections)
								{
									if(a.getCost() == -residDist[b][c])
									{
										flow.put(a.getMatchId(), flow.get(a.getMatchId()) - k);
										break;
									}
								}
								if(ans.get(pair) == k)
									ans.remove(pair);
								else
									ans.put(pair, ans.get(pair) - k);
							}
							else 
							{
								pair = new Pair<Integer>(reducedVertexMap.get(u.getMatchId()).getMatchId(),reducedVertexMap.get(v.getMatchId()).getMatchId());
								List<Arc> connections = u.getNeighbors().get(v);
								for (Arc a: connections)
								{
									if(a.getCost() == residDist[b][c])
									{
										if(!flow.containsKey(a.getMatchId()))
											flow.put(a.getMatchId(), k);
										else
											flow.put(a.getMatchId(), flow.get(a.getMatchId()) + k);
										break;
									}
								}
								if(!ans.containsKey(pair))
									ans.put(pair, k);
								else
									ans.put(pair, ans.get(pair) + k);
							}


						} while ((b = c) != i);
						improvements = true;
						break;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return ans;
	}
	/**
	 * Solves a min cost flow problem defined on this graph.  Demands must be set, or else we get an error here.
	 * @param g
	 * @return answer - entry [0][0] holds the final cost, and the edge from node 
	 * [i][0] to [i][1] that has cost [i][2] has flow [i][3] in the optimal solution.
	 */
	public static int[][] minCostNetworkFlow(DirectedGraph g) throws GraphInfeasibleException, UnsupportedFormatException
	{
		int nodes = g.getVertices().size();
		int edges = g.getEdges().size()+1;
		int numdemand = 0;

		//setup inputs to helper method
		for (Vertex v: g.getVertices())
		{
			try {
				v.getDemand();
				numdemand++;
			} catch(NoDemandSetException e) {
				//do nothing
			}
		}
		int nodedemand[][] = new int[numdemand + 1][2];
		int i = 1;
		int temp;
		for (Vertex v: g.getVertices())
		{
			try {
				temp = v.getDemand();
				nodedemand[i][1] = temp;
				nodedemand[i][0] = v.getId();
				i++;
			} catch(NoDemandSetException e) {
				//do nothing
			}
		}


		int nodei[] = new int[edges + 1];
		int nodej[] = new int[edges + 1];
		int arccost[] = new int[edges + 1];
		int upbound[] = new int[edges + 1];
		int lowbound[] = new int[edges + 1];
		int arcsol[][]= new int[2][edges + 1];
		int flowsol[] = new int[edges + 1]; 

		for (Link<? extends Vertex> l : g.getEdges())
		{
			nodei[l.getId()] = l.getEndpoints().getFirst().getId();
			nodej[l.getId()] = l.getEndpoints().getSecond().getId();
			arccost[l.getId()] = l.getCost();
			lowbound[l.getId()] = 0;
			upbound[l.getId()] = 0;

		}

		int success = minCostNetworkFlow(nodes,edges,numdemand,nodedemand,nodei,nodej,arccost,upbound,lowbound,arcsol,flowsol);

		//check for errors
		if(success == 1 || success ==  4)
		{
			throw new GraphInfeasibleException("The graph was marked as infeasible during the solution to the flow problem.");
		}
		else if( success == 2 || success == 3)
		{
			throw new UnsupportedFormatException("There was a problem setting up the min cost flow problem.  This probably indicates an " +
					"error in the minCostNetworkFlow method.");
		}

		//everything is probably okay
		int answer[][] = new int[arcsol[0][0]+1][4];
		for(int j=1;j<arcsol[0][0]+1;j++)
		{
			answer[j][0] = arcsol[0][j];
			answer[j][1] = arcsol[1][j];
			answer[j][2] =arccost[j];
			answer[j][3]= flowsol[j];
		}
		//store the final cost in [0][0]
		answer[0][0] = arccost[0];

		return answer;
	}
	/**
	 * Solves a min cost flow problem defined on the network ( directed graph ) using a primal dual method.
	 * Implementation is taken from Lau:
	 * Return codes:
	 * 0 - optimal solution found
	 * 1 - infeasible, net required flow is negative
	 * 2 - need to increase the size of internal edge-length arrays
	 * 3 - error in the input of the arc list, arc cost, and / or arc flow
	 * 4 - infeasible, net required flow imposed by arc flow lower bounds is negative
	 * @param nodes - the number of nodes in the graph
	 * @param edges - the number of edges in the graph
	 * @param numdemand - the number of nodes that have nonzero demands (number of supplies and sources)
	 * @param nodedemand - entry i,1 is the demand for the node with label in entry i,0
	 * @param nodei - the pth entry holds the first endpoint of the pth edge
	 * @param nodej - the pth entry holds the second endpoint of the pth edge
	 * @param arccost - entry i holds the cost of edge i, entry 0 holds the optimal cost at the end.
	 * @param upbound - the ith entry holds the flow capacity of edge i
	 * @param lowbound - the ith entry holds the min flow for edge i
	 * @param arcsol - entry 0,0 is the number of edges with nonzero flow, and the ith of these edges connects vertices
	 * arcsol[0][i] and arcsol[1][i].
	 * @param flowsol - the amount of flow on the ith edge.
	 */
	public static int minCostNetworkFlow(int nodes, int edges, int numdemand,
			int nodedemand[][], int nodei[], int nodej[], int arccost[],
			int upbound[], int lowbound[], int arcsol[][], int flowsol[])
	{
		int i, j, k, l, m, n, lastslackedge, solarc, temp, tmp, u, v, remain, rate;
		int arcnam, tedges, tedges1, nodes1, nodes2, nzdemand, value, valuez;
		int tail, ratez, tailz, trial, distdiff, olddist, treenodes, iterations;
		int right, point, part, jpart, kpart, spare, sparez, lead, otherend, sedge;
		int orig, load, curedge, p, q, r, vertex1, vertex2, track, spointer, focal;
		int newpr, newlead, artedge, maxint, artedge1, ipart, distlen;
		int after=0, other=0, left=0, newarc=0, newtail=0;
		int pred[] = new int[nodes + 2];
		int succ[] = new int[nodes + 2];
		int dist[] = new int[nodes + 2];
		int sptpt[] = new int[nodes + 2];
		int flow[] = new int[nodes + 2];
		int dual[] = new int[nodes + 2];
		int arcnum[] = new int[nodes + 1];
		int head[] = new int[edges * 2];
		int cost[] = new int[edges * 2];
		int room[] = new int[edges * 2];
		int least[] = new int[edges * 2];
		int rim[] = new int[3];
		int ptr[] = new int[3]; 
		boolean infeasible;
		boolean flowz=false, newprz=false, artarc=false, removelist=false;
		boolean partz=false, ipartout=false, newprnb=false;

		for (p=0; p<=nodes; p++)
			arcnum[p] = 0;
		maxint = 0;
		for (p=1; p<=edges; p++) {
			arcnum[nodej[p]]++;
			if (arccost[p] > 0) maxint += arccost[p];
			if (upbound[p] > 0) maxint += upbound[p];
		}
		artedge = 1;
		artedge1 = artedge + 1;
		tedges =  (edges * 2) - 2;
		tedges1 = tedges + 1;
		nodes1 = nodes + 1;
		nodes2 = nodes + 2;
		dual[nodes1] = 0;
		for (p=1; p<=nodes1; p++) {
			pred[p] = 0;
			succ[p] = 0;
			dist[p] = 0;
			sptpt[p] = 0;
			flow[p] = 0;
		}
		head[artedge] = nodes1;
		cost[artedge] = maxint;
		room[artedge] = 0;
		least[artedge] = 0;
		remain = 0;
		nzdemand = 0;
		sedge = 0;

		// initialize supply and demand lists
		succ[nodes1] = nodes1;
		pred[nodes1] = nodes1;
		for (p=1; p<=numdemand; p++) {
			flow[nodedemand[p][0]] = nodedemand[p][1];
			remain += nodedemand[p][1];
			if (nodedemand[p][1] <= 0) continue;
			nzdemand++;
			dist[nodedemand[p][0]] = nodedemand[p][1];
			succ[nodedemand[p][0]] = succ[nodes1];
			succ[nodes1] = nodedemand[p][0];
		}
		if (remain < 0)  return 1;
		for (p=1; p<=nodes; p++)
			dual[p] = arcnum[p];
		i = 1;
		j = artedge;
		for (p=1; p<=nodes; p++) {
			i = -i;
			tmp = Math.max(1, dual[p]);
			if (j + tmp > tedges) return 2;
			dual[p] = (i >= 0 ? j+1 : -(j+1));
			for (q=1; q<=tmp; q++) {
				j++;
				head[j] = (i >= 0 ? p : -p);
				cost[j] = 0;
				room[j] = -maxint;
				least[j] = 0;
			}
		}

		// check for valid input data
		sedge = j + 1;
		if (sedge > tedges)  return 2;
		head[sedge] = (-i >= 0 ? nodes1 : -nodes1);
		valuez = 0;
		for (p=1; p<=edges; p++) {
			if ((nodei[p] > nodes) || (nodej[p] > nodes) || (upbound[p] >= maxint))
				return 3;
			if (upbound[p] == 0) upbound[p] = maxint;
			if (upbound[p] < 0) upbound[p] = 0;
			if ((lowbound[p] >= maxint) || (lowbound[p] < 0) || 
					(lowbound[p] > upbound[p]))
				return 3;
			u = dual[nodej[p]];
			v = Math.abs(u);
			temp = (u >= 0 ? nodes1 : -nodes1);
			if ((temp ^ head[v]) <= 0) {
				sedge++;
				tmp = sedge - v;
				r = sedge;
				for (q=1; q<=tmp; q++) {
					temp = r - 1;
					head[r]  = head[temp];
					cost[r]  = cost[temp];
					room[r] = room[temp];
					least[r] = least[temp];
					r = temp;
				}
				for (q=nodej[p]; q<=nodes; q++)
					dual[q] += (dual[q] >= 0 ? 1 : -1);
			}

			// insert new edge
			head[v] = (u >= 0 ? nodei[p] : -nodei[p]);
			cost[v] = arccost[p];
			valuez += arccost[p] * lowbound[p];
			room[v] = upbound[p] - lowbound[p];
			least[v] = lowbound[p];
			flow[nodei[p]] -= lowbound[p];
			flow[nodej[p]] += lowbound[p];
			dual[nodej[p]] = (u >= 0 ? v+1 : -(v+1));
			sptpt[nodei[p]] = -1;
		}
		i = nodes1;
		k = artedge;
		l = 0;
		sedge--;
		for (p=artedge1; p<=sedge; p++) {
			j = head[p];
			if ((i ^ j) <= 0) {
				i = -i;
				l++;
				dual[l] = k + 1;
			}
			else
				if (Math.abs(j) == l) continue;
			k++;
			if (k != p) {
				head[k]  = head[p];
				cost[k]  = cost[p];
				room[k]  = room[p];
				least[k] = least[p];
			}
		}
		sedge = k;
		if (sedge + Math.max(1,nzdemand) + 1 > tedges) return 2;
		// add regular slacks
		i = -head[sedge];
		focal = succ[nodes1];
		succ[nodes1] = nodes1;
		if (focal == nodes1) {
			sedge++;
			head[sedge]  = (i >= 0 ? nodes1 : -nodes1);  
			cost[sedge]  = 0;
			room[sedge]  = -maxint;
			least[sedge] = 0;
		}
		else
			do {
				sedge++;
				head[sedge] = (i >= 0 ? focal : -focal);
				cost[sedge] = 0;
				room[sedge] = dist[focal];
				dist[focal] = 0;
				least[sedge] = 0;
				after = succ[focal];
				succ[focal] = 0;
				focal = after;
			} while (focal != nodes1);
		lastslackedge = sedge;
		sedge++;
		head[sedge] = (-i >= 0 ? nodes2 : -nodes2);
		cost[sedge] = maxint;
		room[sedge] = 0;
		least[sedge] = 0;
		// locate sources and sinks
		remain = 0;
		treenodes = 0;
		focal = nodes1;
		for (p=1; p<=nodes; p++) {
			j = flow[p];
			remain += j;
			if (j == 0) continue;
			if (j < 0) {
				flow[p] = -j;
				right = nodes1;
				do {
					after = pred[right];
					if (flow[after]+j <= 0) break;
					right = after;
				} while (true);
				pred[right] = p;
				pred[p] = after;
				dist[p] = -1;
			}
			else {
				treenodes++;
				sptpt[p] = -sedge;
				flow[p] = j;
				succ[focal] = p;
				pred[p] = nodes1;
				succ[p] = nodes1;
				dist[p] = 1;
				dual[p] = maxint;
				focal = p;
			}
		}
		if (remain < 0) return 4;
		do {
			// select highest rank demand
			tail = pred[nodes1];
			if (tail == nodes1) break;
			do {
				// set link to artificial
				newarc = artedge;
				newpr = maxint;
				newprz = false;
				flowz = false;
				if (flow[tail] == 0) {
					flowz = true;
					break;
				}
				// look for sources
				trial = dual[tail];
				lead = head[trial];
				other = (lead >= 0 ? nodes1 : -nodes1);
				do {
					if (room[trial] > 0) {
						orig = Math.abs(lead);
						if (dist[orig] == 1) {
							if (sptpt[orig] != artedge) {
								rate = cost[trial];
								if (rate < newpr) {
									if (room[trial] <= flow[tail]) {
										if (flow[orig] >= room[trial]) {
											newarc = -trial;
											newpr = rate;
											if (newpr == 0) {
												newprz = true;
												break;
											}
										}
									}
									else {
										if (flow[orig] >= flow[tail]) {
											newarc = trial;
											newpr = rate;
											if (newpr == 0) {
												newprz = true;
												break;
											}
										}
									}
								}
							}
						}
					}
					trial++;
					lead = head[trial];
				} while ((lead ^ other) > 0);
				if (!newprz) {
					artarc = false;
					if (newarc == artedge) {
						artarc = true;
						break;
					}
				} else
					newprz = false;
				if (newarc > 0) break;
				newarc = -newarc;
				orig = Math.abs(head[newarc]);
				load = room[newarc];
				// mark unavailable
				room[newarc] = -load;
				// adjust flows
				flow[orig] -= load;
				flow[tail] -= load;
			} while (true);
			if (!flowz) {
				removelist = false;
				if (!artarc) {
					room[newarc] = -room[newarc];
					orig = Math.abs(head[newarc]);
					flow[orig] -= flow[tail];
					k = maxint;
					removelist = true;
				}
				else {
					// search for transshipment nodes
					artarc = false;
					trial = dual[tail];
					lead = head[trial];
					newprz = false;
					do {
						if (room[trial] > 0) {
							orig = Math.abs(lead);
							// is it linked
							if (dist[orig] == 0) {
								rate = cost[trial];
								if (rate < newpr) {
									newarc = trial;
									newpr = rate;
									if (newpr == 0) {
										newprz = true;
										break;
									}
								}
							}
						}
						trial++;
						lead = head[trial];
					} while ((lead ^ other) > 0);
					artarc = false;
					if (!newprz) {
						if(newarc == artedge)
							artarc = true;
					}
					else
						newprz = false;
					if (!artarc) {
						orig = Math.abs(head[newarc]);
						if (room[newarc] <= flow[tail]) {
							// get capacity
							load = room[newarc];
							// mark unavailable
							room[newarc] = -load;
							// adjust flows
							flow[orig] = load;                                             
							flow[tail] -= load;
							pred[orig] = tail;
							pred[nodes1] = orig;
							dist[orig] = -1;
							continue;
						}
						// mark unavailable
						room[newarc] = -room[newarc];
						flow[orig] = flow[tail];
						pred[orig] = pred[tail];
						pred[tail] = orig;
						pred[nodes1] = orig;
						succ[orig] = tail;
						sptpt[tail] = newarc;  
						dist[orig] = dist[tail] - 1;
						dual[tail] = newpr;
						treenodes++;
						continue;
					}
					else
						artarc = false;
				}
			}
			flowz = false;
			if (!removelist)
				k = 0;
			else
				removelist = false;
			pred[nodes1] = pred[tail];
			orig = Math.abs(head[newarc]);
			sptpt[tail] = newarc;
			dual[tail] = newpr;
			pred[tail] = orig;                                                     
			i = succ[orig];
			succ[orig] = tail;
			j = dist[orig] - dist[tail] + 1;
			focal = orig;
			do {
				// adjust dual variables
				focal = succ[focal];
				l = dist[focal];
				dist[focal] = l + j;
				k -= dual[focal];
				dual[focal] = k;
			} while (l != -1);
			succ[focal] = i;
			treenodes++;
		}  while (true);

		// set up the expand tree
		tail = 1;
		trial = artedge1;
		lead = head[trial];
		do {
			if (treenodes == nodes) break;
			tailz = tail;
			newpr = maxint;
			do {
				// search for least cost connectable edge
				otherend = dist[tail];
				other = (lead >= 0 ? nodes1 : -nodes1);
				do {
					if (room[trial] > 0) {
						m = cost[trial];
						if (newpr >= m) {
							orig = Math.abs(lead);
							if (dist[orig] != 0) {
								if (otherend == 0) {
									i = orig;
									j = tail;
									k = m;
									l = trial;
									newpr = m;
								}
							}
							else {
								if (otherend != 0) {
									i = tail;
									j = orig;
									k = -m;
									l = -trial;
									newpr = m;
								}
							}
						}
					}
					trial++;
					lead = head[trial];
				} while ((lead ^ other) > 0);
				// prepare the next 'tail' group
				tail++;
				if (tail == nodes1) {
					tail = 1;
					trial = artedge1;
					lead = head[trial];
				}
				newprnb = false;
				if (newpr != maxint) {
					newprnb = true;
					break;
				}
			} while (tail != tailz);
			if (!newprnb) {
				for (p=1; p<=nodes; p++) {
					if (dist[p] != 0) continue;
					// add artificial
					sptpt[p] = artedge;
					flow[p] = 0;
					succ[p] = succ[nodes1];
					succ[nodes1] = p;
					pred[p] = nodes1;
					dist[p] = 1;
					dual[p] = -maxint;
				}
				break;
			}
			newprnb = false;
			sptpt[j] = l;
			pred[j] = i;
			succ[j] = succ[i];
			succ[i] = j;
			dist[j] = dist[i] + 1;
			dual[j] = dual[i] - k;
			newarc = Math.abs(l);
			room[newarc] = -room[newarc];
			treenodes++;
		} while (true);
		for (p=1; p<=nodes; p++) {
			q = Math.abs(sptpt[p]);
			room[q] = -room[q];
		}
		for (p=1; p<=sedge; p++)
			if (room[p] + maxint == 0)  room[p] = 0;
		room[artedge] = maxint;
		room[sedge] = maxint;

		// initialize price
		tail = 1;
		trial = artedge1;
		lead = head[trial];
		iterations = 0;

		// new iteration
		do {
			iterations++;
			// pricing basic edges
			tailz = tail;
			newpr = 0;
			do {
				ratez = -dual[tail];
				other = (lead >= 0 ? nodes1 : -nodes1);
				do {
					orig = Math.abs(lead);
					rate = dual[orig] + ratez - cost[trial];
					if (room[trial] < 0) rate = -rate;
					if (room[trial] != 0) {
						if (rate > newpr) {
							newarc = trial;
							newpr = rate;
							newtail = tail;
						}
					}
					trial++;
					lead = head[trial];
				} while ((lead ^ other) > 0);
				tail++;
				if (tail == nodes2) {
					tail = 1;
					trial = artedge1;
					lead = head[trial];
				}
				newprz = true;
				if (newpr != 0) {
					newprz = false;
					break;
				}
			} while (tail != tailz);
			if (newprz) {
				for (p=1; p<=edges; p++)
					flowsol[p] = 0;
				// prepare summary
				infeasible = false;
				value = valuez;
				for (p=1; p<=nodes; p++) {
					i = Math.abs(sptpt[p]);
					if ((flow[p] != 0) && (cost[i] == maxint)) infeasible = true;
					value += cost[i] * flow[p];
				}
				for (p=1; p<=lastslackedge; p++)
					if (room[p] < 0) {
						q = -room[p];
						value += cost[p] * q;
					}
				if (infeasible) return 4;
				arccost[0] = value;
				for (p=1; p<=nodes; p++) {
					q = Math.abs(sptpt[p]);
					room[q] = -flow[p];
				}
				solarc = 0;
				tail = 1;
				trial = artedge1;
				lead = head[trial];
				do {
					other = (lead >= 0 ? nodes1 : -nodes1);
					do {
						load = Math.max(0, -room[trial]) + least[trial];
						if (load != 0) {
							orig = Math.abs(lead);
							solarc++;
							arcsol[0][solarc] = orig;
							arcsol[1][solarc] = tail;
							flowsol[solarc] = load;
						}
						trial++;
						lead = head[trial];
					} while ((lead ^ other) > 0);
					tail++;
				} while (tail != nodes1);
				arcsol[0][0] = solarc;
				return 0;
			}

			// ration test
			newlead = Math.abs(head[newarc]);
			part = Math.abs(room[newarc]);
			jpart = 0;

			// cycle search
			ptr[2] = (room[newarc] >= 0 ? tedges1 : -tedges1);
			ptr[1] = -ptr[2];
			rim[1] = newlead;
			rim[2] = newtail;
			distdiff = dist[newlead] - dist[newtail];
			kpart = 1;
			if (distdiff < 0) kpart = 2;
			if (distdiff != 0) {
				right = rim[kpart];
				point = ptr[kpart];
				q = Math.abs(distdiff);
				for (p=1; p<=q; p++) {
					if ((point ^ sptpt[right]) <= 0) {
						// increase flow
						i = Math.abs(sptpt[right]);
						spare = room[i] - flow[right];
						sparez = -right;
					}
					else {
						// decrease flow
						spare = flow[right];
						sparez = right;
					}
					if (part > spare) {
						part = spare;
						jpart = sparez;
						partz = false;
						if (part == 0) {
							partz = true;
							break;
						}
					}
					right = pred[right];
				}
				if (!partz) rim[kpart] = right;
			}
			if (!partz) {
				do {
					if (rim[1] ==  rim[2]) break;
					for (p=1; p<=2; p++) {
						right = rim[p];
						if ((ptr[p] ^ sptpt[right]) <= 0) {
							// increase flow
							i = Math.abs(sptpt[right]);
							spare = room[i] - flow[right];
							sparez = -right;
						}
						else {
							// decrease flow
							spare = flow[right];
							sparez = right;
						}
						if (part > spare) {
							part = spare;
							jpart = sparez;
							kpart = p;
							partz = false;
							if (part == 0) {
								partz = true;
								break;
							}
						}
						rim[p] = pred[right];
					}
				} while (true);
				if (!partz) left = rim[1];
			}
			partz = false;
			if (part != 0) {
				// update flows
				rim[1] = newlead;
				rim[2] = newtail;
				if (jpart != 0)  rim[kpart] = Math.abs(jpart);
				for (p=1; p<=2; p++) {
					right = rim[p];
					point = (ptr[p] >= 0 ? part : -part);
					do {
						if (right == left) break;
						flow[right] -= point * (sptpt[right] >= 0 ? 1 : -1);
						right = pred[right];
					} while (true);
				}
			}
			if (jpart == 0) {
				room[newarc] = -room[newarc];
				continue;
			}
			ipart = Math.abs(jpart);
			if (jpart <= 0) {
				j = Math.abs(sptpt[ipart]);
				// set old edge to upper bound
				room[j] = -room[j];
			}
			load = part;
			if (room[newarc] <= 0) {
				room[newarc] = -room[newarc];
				load = room[newarc] - load;
				newpr = -newpr;
			}
			if (kpart != 2) {
				vertex1 = newlead;
				vertex2 = newtail;
				curedge = -newarc;
				newpr = -newpr;
			}
			else {
				vertex1 = newtail;
				vertex2 = newlead;
				curedge = newarc;
			}

			// update tree
			i = vertex1;
			j = pred[i];
			distlen = dist[vertex2] + 1;
			if (part != 0) {
				point = (ptr[kpart]  >= 0 ? part: -part);
				do {
					// update dual variable
					dual[i] += newpr;
					n = flow[i];
					flow[i] = load;
					track = (sptpt[i] >= 0 ? 1 : -1);
					spointer = Math.abs(sptpt[i]);
					sptpt[i] = curedge;
					olddist = dist[i];
					distdiff = distlen - olddist;
					dist[i] = distlen;
					focal = i;
					do {
						after = succ[focal];
						if (dist[after] <= olddist) break;
						dist[after] += distdiff;
						dual[after] += newpr;
						focal = after;
					} while (true);
					k = j;
					do {
						l = succ[k];
						if (l == i) break;
						k = l;
					} while (true);
					ipartout = false;
					if (i == ipart) {
						ipartout = true;
						break;
					}
					load = n - point * track;
					curedge = -(track >= 0 ? spointer : -spointer);            
					succ[k] = after;
					succ[focal] = j;
					k = i;
					i = j;
					j = pred[j];
					pred[i] = k;
					distlen++;
				} while (true);
			}
			if (!ipartout) {
				do {
					dual[i] += newpr;
					n = flow[i];
					flow[i] = load;
					track = (sptpt[i] >= 0 ? 1 : -1);
					spointer = Math.abs(sptpt[i]);
					sptpt[i] = curedge;
					olddist = dist[i];
					distdiff = distlen - olddist;
					dist[i] = distlen;
					focal = i;
					do {
						after = succ[focal];
						if (dist[after] <= olddist) break;
						dist[after] += distdiff;
						// udpate dual variable
						dual[after] += newpr;
						focal = after;
					} while (true);
					k = j;
					do {
						l = succ[k];
						if (l == i) break;
						k = l;
					} while (true);
					// test for leaving edge
					if (i == ipart) break;
					load = n;
					curedge = -(track >= 0 ? spointer : -spointer);
					succ[k] = after;
					succ[focal] = j;
					k = i;
					i = j;
					j = pred[j];
					pred[i] = k;
					distlen++;
				} while (true);
			}
			ipartout = false;
			succ[k] = after;
			succ[focal] = succ[vertex2];
			succ[vertex2] = vertex1;
			pred[vertex1] = vertex2;
		} while (true);
	}
	/* Performs a maximal weighted matching on the graph.
	 * @param graph
	 * @return a set containing pairs which are coupled in the maximal matching.
	 */
	public static Set<Pair<Vertex>> maxWeightedMatching(Graph<Vertex, Link<Vertex>> graph)
	{
		return null;
	}
	/**
	 * Performs  min-cost perfect matching using Kolmogorov's publicly available Blossom V C code.
	 * @param graph
	 * @return
	 */
	public static Set<Pair<UndirectedVertex>> minCostMatching(UndirectedGraph graph) throws UnsupportedFormatException
	{	
		HashSet<Pair<UndirectedVertex>> matching = new HashSet<Pair<UndirectedVertex>>();
		HashMap<Integer, UndirectedVertex> indexedVertices = graph.getInternalVertexMap();

		//setup our input to Kolmogorov's Blossom V code
		int n = graph.getVertices().size();
		int m = graph.getEdges().size();
		int[]edges = new int[2*m];
		int[]weights = new int[m];

		//edges[2m] and edges[2m+1] hold the endpoints of each edge, weight[m] holds the weight between them.
		for(Edge e: graph.getEdges())
		{
			//the c code indexes things by zero, so we need to be compliant
			edges[2*e.getId()-2] = e.getEndpoints().getFirst().getId() - 1;
			edges[2*e.getId()-1] = e.getEndpoints().getSecond().getId() - 1;
			weights[e.getId()-1] = e.getCost();
		}
		int[] ans = CAlgorithms.blossomV(n, m, edges, weights);

		//to make sure we only report unique pairs, (and not, say 0-1 and 1-0).
		ArrayList<Integer> matched = new ArrayList<Integer>();

		//now reinterpret the results
		for (int i=0; i<ans.length;i++)
		{
			if(matched.contains(i))
				continue;
			matching.add(new Pair<UndirectedVertex>(indexedVertices.get(ans[i]+1), indexedVertices.get(i+1)));
			matched.add(ans[i]);

		}
		return matching;
	}

}
