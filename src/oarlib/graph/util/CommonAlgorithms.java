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
	 */
	public static void allPairsShortestPaths(int n, int dist[][], int big,
			int startnode, int endnode, int path[])
	{
		int i,j,k,d,num,node;
		int next[][] = new int[n+1][n+1];
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

	/**
	 * Performs a maximal weighted matching on the graph.
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
	public static Set<Pair<Vertex>> minCostMatching(Graph<Vertex,Link<Vertex>> graph)
	{
		return null;
	}

}
