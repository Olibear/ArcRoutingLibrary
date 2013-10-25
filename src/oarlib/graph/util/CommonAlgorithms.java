package oarlib.graph.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Link;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class CommonAlgorithms {

	/**
	 * Fleury's algorithm for determining an Euler tour through an directed Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a Route object containing the tour.
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static Route tryFleury(DirectedGraph<Arc> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		return fleury(eulerianGraph);
	}
	/**
	 * Fleury's algorithm for determining an Euler tour through an undirected Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a Route object containing the tour.
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static Route tryFleury(UndirectedGraph<Edge> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return fleury(eulerianGraph);
	}
	/**
	 * business logic for Fleury's algorithm
	 * @return the Eulerian cycle
	 */
	private static Route fleury(Graph<?,?> graph)
	{
		return null;
	}
	/**
	 * FindRoute algorithm (alternative to Fleury's given in Dussault et al. Plowing with Precedence
	 * @return the Eulerian cycle
	 */
	public static Route findRoute(Graph<?,?> graph)
	{
		return null;
	}
	/**
	 * Checks to see if the directed graph is weakly connected
	 * @return true if the graph is  weakly connected, false oth.
	 */
	public static boolean isWeaklyConnected(DirectedGraph<?> graph) 
	{
		return false;
	}
	/**
	 * Checks to see if the directed graph is strongly connected
	 * @return true if the graph is strongly  connected, false oth.
	 */
	public static boolean isStronglyConnected(DirectedGraph<?> graph)
	{
		return false;
	}
	/**
	 * Checks to see if the undirected graph is connected
	 * @return true if the graph is connected (or empty), false oth.
	 */
	public static boolean isConnected(UndirectedGraph<?> graph)
	{
		//start at an arbitrary vertex
		HashSet<UndirectedVertex> vertices = graph.getVertices();
		HashSet<? extends Edge> edges = graph.getEdges();
		HashSet<UndirectedVertex> nextUp = new HashSet<UndirectedVertex>();
		
	}
	/**
	 * Checks to see if the directed graph is eulerian.
	 * @param graph
	 * @return true if the graph is eulerian, false oth.
	 */
	public static boolean isEulerian (DirectedGraph<Arc> graph)
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
	public static boolean isEulerian(UndirectedGraph<Edge> graph) 
	{
		for (UndirectedVertex v:graph.getVertices()) 
		{
			if (v.getDegree() % 2 == 1)
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
	 * @return path will hold the requested path, if one is requested
	 *  next will hold the matrix from which it is possible to reconstruct the shortest path between any two nodes
	 *  
	 * 
	 */
	public static void allPairsShortestPaths(int n, int dist[][], int big, int startnode, int endnode, int path[])
	{
		graph.
		//TODO: setup the dist matrix to hold the naive distances between nodes

		//Implementation taken / modified from Lau.
		int i,j,k,d,num,node;
		int next[][] = new int[n+1][n+1];
		int order[] = new int[n+1];

		//compute the shortest path distance matrix
		for (i=1;i<=n;i++)
		{
			for(j=1;j<=n;j++)
			{
				next[i][j] = i;
			}
		}

		for (i=1;i<=n;i++)
		{
			for(j=1;j<=n;j++)
			{
				//if there's an edge here
				if(dist[j][i] < big)
				{
					for(k=1;k<=n;k++)
					{
						if(dist[i][k] < big) {
							d = dist[j][k] + dist[i][k];
							if(d<dist[j][k]) {
								dist[j][k] = d;
								next[j][k] = next[i][k];
							}
						}
					}
				}
			}
		}

		// find the shortest path from startnode to end node
		//TODO: here should just return the shortest path cost matrix
		if (startnode == 0) 
			return null;
		j = endnode;
		num = 1;
		order[num] = endnode;
		while (true) {
			node = next[startnode][j];
			num++;
			order[num] = node;
			if (node == startnode)
				break;
			j = node;
		}
		for (i=1;i<=num;i++)
		{
			path[i] = order[num-i+1];
			path[0] = num;
		}

		return;
	}
	
	/**
	 * Solves a min cost flow problem defined on the network.  Implementation is a modified version taken from Lau:
	 * Return codes:
	 * 0 - optimal solution found
	 * 1 - infeasible, net required flow is negative
	 * 2 - need to increase the size of internal edge-length arrays
	 * 3 - error in the input of the arc list, arc cost, and arc flow
	 * 4 - infeasible, net required flow imposed by arc flow lower bounds is negative
	 * @param nodes - the number of nodes in the graph
	 * @param edges - the number of edges in the graph
	 * @param numdemand - the number of nodes that have nonzero demands (number of supplies and sources)
	 * @param nodedemand - entry i,1 is the demand for the node with label in entry i,0
	 * @param nodei - the pth entry holds the first endpoint of the pth node
	 * @param nodej - the pth entry holds the second endpoint of the pth node
	 * @param arccost - entry i holds the cost of edge i, entry 0 holds the optimal cost at the end.
	 * @param upbound - the ith entry holds the flow capacity of edge i
	 * @param lowbound - the ith entry holds the min flow for edge i
	 * @param arcsol - entry 0,0 is the number of edges with nonzero flow, and the ith of these edges connects vertices
	 * arcsol[0][i] and arcsol[1][i].
	 * @param flowsol - the amount of flow on the ith edge.
	 */
	public static int minCostNetworkFlow(int nodes, int edges, int numdemand, int nodedemand[][], int nodei[], int nodej[], int arccost[],
			int upbound[], int lowbound[], int arcsol[][], int flowsol[])
	{
		int i;
		int j;
		int k;
		int l;
		int m;
		int n;
		int lastslackedge;
		int solarc;
		int temp;
		int tmp;
		int u;
		int v;
		int remain;
		int rate;
		int arcnam;
		int tedges;
		int tedges1;
		int nodes1;
		int nodes2;
		int nzdemand;
		int value;
		int valuez;
		int tail;
		int ratez;
		int tailz;
		int trial;
		int distdiff;
		int olddist;
		int treenodes;
		int iterations;
		int right;
		int point;
		int part;
		int jpart;
		int kpart;
		int spare;
		int sparez;
		int lead;
		int otherend;
		int sedge;
		int orig;
		int load;
		int curedge;
		int p;
		int q;
		int r;
		int vertex1;
		int vertex2;
		int track;
		int spointer;
		int focal;
		int newpr;
		int newlead;
		int artedge;
		int maxint;
		int artedge1;
		int ipart;
		int distlen;
		int after = 0;
		int other = 0;
		int left = 0;
		int newarc = 0;
		int newtail = 0;
		
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
		boolean flowz = false;
		boolean newprz = false;
		boolean artarc = false;
		boolean removelist = false;
		boolean partz = false;
		boolean ipartout = false;
		boolean newprnb = false;
		
		for (p = 0; p <= nodes; p++)
		{
			arcnum[p] = 0;
		}
		maxint = 0;
		for (p = 1;p<=edges; p++)
		{
			arcnum[nodej[p]]++;
			if(arccost[p] > 0) 
				maxint += arccost[p];
			if(upbound[p] > 0)
				maxint+=upbound[p];
		}
		artedge = 1;
		artedge1 = artedge + 1;
		tedges = (edges * 2) - 2;
		tedges1 = tedges + 1;
		nodes1 = nodes + 1;
		nodes2 = nodes + 2;
		dual[nodes1] = 0;
		for (p = 1; p <= nodes1; p++)
		{
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
		
		//initialize supply and demand lists
		succ[nodes1] = nodes1;
		pred[nodes1] = nodes1;
		for(p=1;p<=numdemand;p++)
		{
			flow[nodedemand[p][0]] = nodedemand[p][1];
			remain += nodedemand[p][1];
			if (nodedemand[p][1] <= 0)
			{
				continue;
			}
			nzdemand++;
			dist[nodedemand[p][0]] = nodedemand[p][1];
			succ[nodedemand[p][0]] = succ[nodes1];
			succ[nodes1] = nodedemand[p][0];
		}
		if (remain < 0)
			return 1;
		for(p=1;p<=nodes;p++)
		{
			dual[p] = arcnum[p];
		}
		i = 1;
		j = artedge;
		for (p=1;p<=nodes;p++)
		{
			i = -i;
			tmp = Math.max(1, dual[p]);
			if (j + tmp > tedges)
				return 2;
			dual[p] = (i >= 0 ? p: -p);
			for(q=1;q<=tmp;q++)
			{
				j++;
				head[j] = (i >= 0 ? p : -p);
				cost[j] = 0;
				room[j] = -maxint;
				least[j] = 0;
			}
		}
		
		//check for valid input data
		sedge = j + 1;
		if (sedge > tedges)
			return 2;
		head[sedge] = (-i >= 0 ? nodes1 : -nodes1);
		valuez = 0;
		for ( p=1; p<=edges; p++)
		{
			if ((nodei[p] > nodes) || (nodej[p] > nodes) || (upbound[p] >= maxint))
				return 3;
			if (upbound[p] == 0) 
				upbound[p] = maxint;
			if (upbound[p] < 0)
				upbound[p] = 0;
			if ((lowbound[p] >= maxint) || (lowbound[p] < 0) || (lowbound[p] > upbound[p]))
				return 3;
			u = dual[nodej[p]];
			v = Math.abs(u);
			temp = (u >= 0 ? nodes1: -nodes1);
			if ((temp ^ head[v]) <= 0)
			{
				sedge++;
				tmp = sedge - v;
				r = sedge;
				for (q=1;q<=tmp;q++)
				{
					temp = r - 1;
					head[r] = head[temp];
					cost[r] = cost[temp];
					room[r] = room[temp];
					least[r] = least[temp];
					r = temp;
				}
				for(q=nodej[p];q<=nodes;q++)
				{
					dual[q] += (dual[q] >= 0 ? 1 : -1);
				}
			}
			
			//insert new edge
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
		for (p = artedge1;p<=sedge; p++)
		{
			j = head[p];
			if ((i ^ j) <= 0)
			{
				i = -i;
				l++;
				dual[l] = k + 1;
			}
			else if (Math.abs(j) == 1)
				continue;
			k++;
			if (k!=p)
			{
				head[k] = head[p];
				cost[k] = cost[p];
				room[k] = room[p];
				least[k] = least[p];
			}
		}
		sedge = k;
		if (sedge + Math.max(1, nzdemand) + 1 > tedges)
			return 2;
		//add regular slacks
		i = -head[sedge];
		focal = succ[nodes1];
		succ[nodes1] = nodes1;
		if(focal == nodes1)
		{
			sedge++;
			head[sedge] = (i >= 0 ? nodes1 : -nodes1);
			cost[sedge] = 0;
			room[sedge] = -maxint;
			least[sedge] = 0;
		}
		else
		{
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
		}
		lastslackedge = sedge;
		sedge++;
		head[sedge] = (-i >= 0 ? nodes2 : -nodes2);
		cost[sedge] = maxint;
		room[sedge] = 0;
		least[sedge] = 0;
		//locate sources and sinks
		remain = 0;
		treenodes = 0;
		focal = nodes1;
		for (p = 0 ; p <= nodes; p++)
		{
			j = flow[p];
			remain += j;
			if (j == 0)
				continue;
			if (j < 0)
			{
				flow[p] = -j;
				right = nodes1;
				do {
					after = pred[right];
					if (flow[after] + j <= 0)
						break;
					right = after;
				} while (true);
				pred[right] = p;
				pred[p] = after;
				dist[p] = -1;
			}
			else
			{
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
		if (remain > 0)
			return 4;
		do {
			//select highest rank demand
			tail = pred[nodes1];
			if (tail == nodes1)
				break;
			//set link to artificial
			newarc = artedge;
			newpr = maxint;
			newprz = false;
			flowz = false;
			if (flow[tail] == 0)
			{
				flowz = true;
				break;
			}
			//look for sources
			trial = dual[tail];
			lead = head[trial];
			other = (lead >= 0 ? nodes1 : -nodes1);
			do {
				if (room[trial] > 0)
				{
					orig = Math.abs(lead);
					if (dist[orig] == 1)
					{
						if (sptpt[orig] != artedge)
						{
							rate = cost[trial];
							if (rate < newpr)
							{
								if (room[trial] <= flow[tail])
								{
									if (flow[orig] >= room[trial])
									{
										newarc = -trial;
										newpr = rate;
										if (newpr ==0)
										{
											newprz = true;
											break;
										}
									}
								}
								else
								{
									if (flow[orig] >= flow[tail])
									{
										newarc = trial;
										newpr = rate;
										if (newpr == 0)
										{
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
			} while((lead ^ other) > 0);
			if (!newprz)
			{
				artarc = false;
				if (newarc == artedge)
				{
					artarc = true;
					break;
				}
			}
			else
				newprz = false;
			if (newarc > 0)
				break;
			newarc = -newarc;
			orig = Math.abs(head[newarc]);
			load = room[newarc];
			//mark unavailable
			room[newarc] = -load;
			//adjust flows
			flow[orig] -= load;
			flow[tail] -= load;
		} while(true);
		if (!flowz)
		{
			removelist = false;
			if (!artarc)
			{
				room[newarc] -= room[newarc];
				orig = Math.abs(head[newarc]);
				flow[orig] -= flow[tail];
				k = maxint;
				removelist = true;
			}
			else
			{
				//search for transshipment nodes
				artarc = false;
				trial = dual[tail];
				lead = head[trial];
				newprz = false;
				do
				{
					if (room[trial] > 0)
					{
						orig = Math.abs(lead);
						//is it linked
						if (dist[orig] == 0)
						{
							rate = cost[trial];
							if (rate < newpr)
							{
								newarc = trial;
								newpr = rate;
								if (newpr == 0)
								{
									newprz = true;
									break;
								}
							}
						}
					}
					trial++;
					lead = head[trial];
				} while((lead^other) > 0);
				artarc = false;
				if (!newprz){
					if (newarc == artedge)
						artarc = true;
				}
				else
					newprz = false;
				if (!artarc) {
					orig = Math.abs(head[newarc]);
					if (room[newarc] <= flow[tail])
					{
						//get capacity
						load = room[newarc];
						//mark unavailable
						room[newarc] = -load;
						//adjust flows
						flow[orig] = load;
						flow[tail] -= load;
						pred[orig] = tail;
						
					}
				}
			}
		}
		
		
		
		return 0;
	}
	/**
	 * Performs a maximal weighted matching on the graph.
	 * @param graph
	 * @return a set containing pairs which are coupled in the maximal matching.
	 */
	public static Set<Pair<Vertex>> maxWeightedMatching(Graph<Vertex, Link<Vertex>> graph)
	{
		return null;
	}

}
