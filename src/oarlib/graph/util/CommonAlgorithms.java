package oarlib.graph.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Link;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.exceptions.GraphInfeasibleException;
import oarlib.exceptions.NoDemandSetException;
import oarlib.exceptions.SetupException;
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
	public static int[] tryFleury(DirectedGraph eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		return fleury(eulerianGraph, true);
	}
	/**
	 * Fleury's algorithm for determining an Euler tour through an undirected Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a Route object containing the tour.
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static int[] tryFleury(UndirectedGraph eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return fleury(eulerianGraph, false);
	}
	/**
	 * business logic for Fleury's algorithm
	 * @return the Eulerian cycle
	 */
	private static int[] fleury(Graph<? extends Vertex,? extends Link<? extends Vertex>> graph, boolean directed)
	{
		int n = graph.getVertices().size();
		Collection<? extends Link<? extends Vertex>> edges  = graph.getEdges();
		int m = edges.size();
		int[] nodei = new int[m+1];
		int[] nodej = new int[m+1];
		int[] trail = new int[m+1];

		for (Link<? extends Vertex> l: edges)
		{
			nodei[l.getId()] = l.getEndpoints().getFirst().getId();
			nodej[l.getId()] = l.getEndpoints().getSecond().getId();
		}
		EulerCircuit(n,m,directed,nodei,nodej,trail);
		return trail;
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
	/**
	 * 
	 * @param neighbor
	 * @param next
	 * @param idxb
	 * @param idxc
	 * @param tmparg
	 */
	static private void cpt_DuplicateEdges(int neighbor[], int next[], 
			int idxb, int idxc, int tmparg[])
	{
		/* this method is used internally by ChinesePostmanTour */

		// Duplicate matching edges

		int p,q,r;

		p = tmparg[0];
		q = idxb;
		r = idxc;
		while (true) {
			p = next[q];
			while (true) {
				if (neighbor[p] == r) break;
				p++;
			}
			neighbor[p] = -r;
			if (q == idxc) break;
			q = idxc;
			r = idxb;
		}
		tmparg[0] = p;
	}
	/**
	 * 
	 * @param core
	 * @param aux1
	 * @param aux3
	 * @param wk1
	 * @param wk2
	 * @param tmparg
	 * @param idxd
	 */
	static private void cpt_ExpandBlossom(int core[], int aux1[], int aux3[],
			float wk1[], float wk2[], int tmparg[], int idxd)
	{
		/* this method is used internally by ChinesePostmanTour */

		// Expanding a blossom

		int p,q,r;
		float work;

		r = tmparg[0];
		p = r;
		do {
			r = p;
			q = aux3[r];
			work = wk1[r];
			while (true) {
				core[p] = r;
				wk2[p] -= work;
				if (p == q) break;
				p = aux1[p];
			}
			p = aux1[q];
			aux1[q] = r;
		} while (p != idxd);
		tmparg[0] = r;
	}
	/**
	 * 
	 * @param neighbor
	 * @param weight
	 * @param next
	 * @param core
	 * @param aux1
	 * @param aux2
	 * @param aux3
	 * @param aux4
	 * @param wk1
	 * @param wk2
	 * @param wk3
	 * @param wk4
	 * @param locb
	 */
	static private void cpt_FirstScan(int neighbor[], int weight[], int next[],
			int core[], int aux1[], int aux2[], int aux3[], int aux4[],
			float wk1[], float wk2[], float wk3[], float wk4[], int locb)
	{
		/* this method is used internally by ChinesePostmanTour */

		// Node scanning

		int i,p,q,r,s,t,u,v;
		float work1,work2,work3,work4,work5;

		work3 = wk3[locb] - wk1[locb];
		q = locb;
		r = aux4[locb];
		t = -1;
		if (r > 0) t = core[r];
		do {
			i = next[q];
			v = next[q+1] - 1;
			work1 = wk2[q];
			for (p=i; p<=v; p++) {
				s = neighbor[p];
				u = core[s];
				if (locb != u) {
					if (t != u) {
						work4 = wk4[u];
						work2 = wk1[u] + wk2[s];
						work5 = (float)(weight[p]);
						work5 += work3 - work1 - work2;
						if (work4 > work5) {
							wk4[u] = work5;
							aux2[u] = q;
							aux3[u] = s;
						}
					}
				}
			}
			q = aux1[q];
		} while (q != locb);
	}  
	/**
	 * 
	 * @param neighbor
	 * @param weight
	 * @param next
	 * @param high
	 * @param core
	 * @param aux1
	 * @param aux2
	 * @param aux3
	 * @param aux4
	 * @param wk1
	 * @param wk2
	 * @param wk3
	 * @param wk4
	 * @param tmparg
	 * @param v
	 */
	static private void cpt_SecondScan(int neighbor[], int weight[],
			int next[], int high, int core[], int aux1[], int aux2[],
			int aux3[], int aux4[], float wk1[], float wk2[],
			float wk3[], float wk4[], int tmparg[], int v)
	{
		/* this method is used internally by ChinesePostmanTour */

		// Node scanning

		int i,p,q,r,s,t,u;
		float work1,work2,work3,work4,work5;

		u = tmparg[0];
		do {
			r = core[u];
			if (r == u) {
				work4 = high;
				work2 = wk1[u];
				do {
					i = next[r];
					s = next[r+1] - 1;
					work1 = wk2[r];
					for (p=i; p<=s; p++) {
						q = neighbor[p];
						t = core[q];
						if (t != u) {
							if (aux4[t] >= 0) {
								work3 = wk3[t] - wk1[t] - wk2[q];
								work5 = (float)(weight[p]);
								work5 += work3 - work2 - work1;
								if (work4 > work5) {
									work4 = work5;
									aux2[u] = q;
									aux3[u] = r;
								}
							}
						}
					}
					r = aux1[r];
				} while (r != u);
				wk4[u] = work4;
			}
			u++;
		} while (u <= v);
		tmparg[0] = u;
	}
	/**
	 * 
	 * @param core
	 * @param aux1
	 * @param wk1
	 * @param wk2
	 * @param locb
	 * @param tmparg
	 */
	static private void cpt_ShrinkBlossom(int core[], int aux1[], 
			float wk1[], float wk2[], int locb, int tmparg[])
	{
		/* this method is used internally by ChinesePostmanTour */

		// Shrinking of a blossom

		int p,q,r;
		float work;

		p = tmparg[0];
		q = p;
		work = wk1[p];
		while (true) {
			core[p] = locb;
			wk2[p] += work;
			r = aux1[p];
			if (r == q) {
				tmparg[0] = p;
				return;
			}
			p = r;
		}
	}
	/**
	 * 
	 * @param n
	 * @param neighbor
	 * @param weight
	 * @param next
	 * @param aux3
	 * @param core
	 * @param startnode
	 */
	static private void cpt_Trail(int n, int neighbor[], int weight[],
			int next[], int aux3[], int core[], int startnode)
	{
		/* this method is used internally by ChinesePostmanTour */

		// Determine an Eulerian trail

		int i,nplus,p,q,r,t,u,v;
		boolean finish;

		nplus = n + 1;
		u = next[nplus];
		if (startnode <= 0 || startnode > n) startnode = 1;
		for (p=1; p<=n; p++) {
			i = next[p] - 1;
			aux3[p] = i;
			core[p] = i;
		}
		p = startnode;
		iterate:
			while (true) {
				i = core[p];
				while (true) {
					v = next[p+1] - 1;
					while (true) {
						i++;
						if (i > v) break;
						q = neighbor[i];
						if (q > n) continue;
						if (q >= 0) {
							t = core[q];
							do {
								t++;
							} while (neighbor[t] != p);
							neighbor[t] = nplus;
							t = aux3[q] + 1;
							aux3[q] = t;
							weight[t] = p;
							core[p] = i;
							p = q;
							continue iterate;
						}
						r = -p;
						q= -q;
						t = core[q];
						do {
							t++;
						} while (neighbor[t] != r);
						neighbor[t] = nplus;
						t = aux3[q] + 1;
						aux3[q] = t;
						weight[t] = p;
						t = aux3[p] + 1;
						aux3[p] = t;
						weight[t] = q;
					}
					core[p] = u;
					finish = true;
					for (p=1; p<=n; p++) {
						i = core[p];
						t = aux3[p];
						if ((t >= next[p]) && (i < u)) {
							finish = false;
							break;
						}
					}
					if (finish) return;
				}
			}
	}
	/**
	 * 
	 * @param n
	 * @param m
	 * @param directed
	 * @param nodei
	 * @param nodej
	 * @param trail
	 */
	public static void EulerCircuit(int n, int m, boolean directed,
			int nodei[], int nodej[], int trail[])
	{
		int i,j,k,p,index,len,traillength,stacklength;
		int endnode[] = new int[m+1];
		int stack[] = new int[m+m+1];
		boolean candidate[] = new boolean[m+1];

		// check for connectedness
		if (!connected(n,m,nodei,nodej)) {
			trail[0] = 1;
			return;
		}

		for (i=1; i<=n; i++) {
			trail[i] = 0;
			endnode[i] = 0;
		}
		if (directed) {
			// check if the directed graph is eulerian
			for (i=1; i<=m; i++) {
				j = nodei[i];
				trail[j]++;
				j = nodej[i];
				endnode[j]++;
			}
			for (i=1; i<=n; i++)
				if (trail[i] != endnode[i]) {
					trail[0] = 1;
					return;
				}
		}
		else {
			// check if the undirected graph is eulerian
			for (i=1; i<=m; i++) {
				j = nodei[i];
				endnode[j]++;
				j = nodej[i];
				endnode[j]++;
			}
			for (i=1; i<=n; i++)
				if ((endnode[i] - ((endnode[i] / 2) * 2)) != 0) {
					trail[0] = 1;
					return;
				}
		}
		// the input graph is eulerian
		trail[0] = 0;
		traillength = 1;
		stacklength = 0;
		// find the next edge
		while (true) {
			if (traillength == 1) {
				endnode[1] = nodej[1];
				stack[1] = 1;
				stack[2] = 1;
				stacklength = 2;
			}
			else {
				p = traillength - 1;
				if (traillength != 2)
					endnode[p] = nodei[trail[p]] + nodej[trail[p]] - endnode[p - 1];
				k = endnode[p];
				if (directed)
					for (i=1; i<=m; i++) 
						candidate[i] = k == nodei[i];
				else
					for (i=1; i<=m; i++)
						candidate[i] = (k == nodei[i]) || (k == nodej[i]);
				for (i=1; i<=p; i++) 
					candidate[trail[i]] = false;
				len = stacklength;
				for (i=1; i<=m; i++)
					if (candidate[i]) {
						len++;
						stack[len] = i;
					}
				stack[len + 1] = len - stacklength;
				stacklength = len + 1;
			}
			//  search further
			while (true) {
				index = stack[stacklength];
				stacklength--;
				if (index == 0) {
					traillength--;
					if (traillength != 0) continue;
					return;
				}
				else {
					trail[traillength] = stack[stacklength];
					stack[stacklength] = index - 1;
					if (traillength == m) return;
					traillength++;
					break;
				}
			}
		}
	}
	/**
	 * 
	 * @param n
	 * @param m
	 * @param nodei
	 * @param nodej
	 * @return
	 */
	public static boolean connected(int n, int m, int nodei[], int nodej[])
	{
		int i,j,k,r,connect;
		int neighbor[] = new int[m + m + 1];
		int degree[] = new int[n + 1];
		int index[] = new int[n + 2];
		int aux1[] = new int[n + 1];
		int aux2[] = new int[n + 1];

		for (i=1; i<=n; i++)
			degree[i] = 0;
		for (j=1; j<=m; j++) {
			degree[nodei[j]]++;
			degree[nodej[j]]++;
		}
		index[1] = 1;
		for (i=1; i<=n; i++) {
			index[i+1] = index[i] + degree[i];
			degree[i] = 0;
		}
		for (j=1; j<=m; j++) {
			neighbor[index[nodei[j]] + degree[nodei[j]]] = nodej[j];
			degree[nodei[j]]++;
			neighbor[index[nodej[j]] + degree[nodej[j]]] = nodei[j];
			degree[nodej[j]]++;
		}
		for (i=2; i<=n; i++)
			aux1[i] = 1;
		aux1[1] = 0;
		connect = 1;
		aux2[1] = 1;
		k = 1;
		while (true) {
			i = aux2[k];
			k--;
			for (j=index[i]; j<=index[i+1]-1; j++) {
				r = neighbor[j];
				if (aux1[r] != 0) {
					connect++;
					if (connect == n) {
						connect /= n;
						if (connect == 1) return true;
						return false;
					}
					aux1[r] = 0;
					k++;
					aux2[k] = r;
				}
			}
			if (k == 0) {
				connect /= n;
				if (connect == 1) return true;
				return false;
			}
		}
	}
	/**
	 * 
	 * @param n
	 * @param m
	 * @param startnode
	 * @param nodei
	 * @param nodej
	 * @param cost
	 * @param sol
	 * @param trail
	 */
	public static void ChinesePostmanTour(int n, int m, int startnode,
			int nodei[], int nodej[], int cost[],
			int sol[][], int trail[])
	{
		int i,iplus1,j,k,idxa,idxb,idxc,idxd,idxe,wt,high,duparcs,totsolcost;
		int loch,loca,locb,locc,locd,loce,locf,locg,hub,tmpopty,tmpoptx=0;
		int nplus,p,q,cur,curnext,position=0;
		int neighbor[] = new int[m + m + 1];
		int weight[] = new int[m + m + 1];
		int degree[] = new int[n + 1];
		int next[] = new int[n + 2];
		int core[] = new int[n + 1];
		int aux1[] = new int[n + 1];
		int aux2[] = new int[n + 1];
		int aux3[] = new int[n + 1];
		int aux4[] = new int[n + 1];
		int aux5[] = new int[n + 1];
		int aux6[] = new int[n + 1];
		int tmparg[] = new int[1];
		float wk1[] = new float[n + 1];
		float wk2[] = new float[n + 1];
		float wk3[] = new float[n + 1];
		float wk4[] = new float[n + 1];
		float eps,work1,work2,work3,work4;
		boolean skip,complete;

		eps = 0.0001f;
		// check for connectedness
		if (!connected(n,m,nodei,nodej)) {
			sol[0][0] = 1;
			return;
		}
		sol[0][0] = 0;

		// store up the neighbors of each node
		for (i=1; i<=n; i++)
			degree[i] = 0;
		for (j=1; j<=m; j++) {
			degree[nodei[j]]++;
			degree[nodej[j]]++;
		}
		next[1] = 1;
		for (i=1; i<=n; i++) {
			iplus1 = i + 1;
			next[iplus1] = next[i] + degree[i];
			degree[i] = 0;
		}
		totsolcost = 0;
		high = 0;
		for (j=1; j<=m; j++) {
			totsolcost += cost[j];
			k = next[nodei[j]] + degree[nodei[j]];
			neighbor[k] = nodej[j];
			weight[k] = cost[j];
			degree[nodei[j]]++;
			k = next[nodej[j]] + degree[nodej[j]];
			neighbor[k] = nodei[j];
			weight[k] = cost[j];
			degree[nodej[j]]++;
			high += cost[j];
		}
		nplus = n + 1;
		locg = -nplus;
		for (i=1; i<=n; i++)
			wk4[i] = high;
		// initialization
		for (p=1; p<=n; p++) {
			core[p] = p;
			aux1[p] = p;
			aux4[p] = locg;
			aux5[p] = 0;
			aux3[p] = p;
			wk1[p] = 0f;
			wk2[p] = 0f;
			i = next[p];
			loch = next[p+1];
			loca = loch - i;
			locd = loca / 2;
			locd *= 2;
			if (loca != locd) {
				loch--;
				aux4[p] = 0;
				wk3[p] = 0f;
				for (q=i; q<=loch; q++) {
					idxc = neighbor[q];
					work2 = (float) (weight[q]);
					if (wk4[idxc] > work2) {
						aux2[idxc] = p;
						wk4[idxc] = work2;
					}
				}
			}
		}
		// examine the labeling
		iterate:
			while (true) {
				work1 = high;
				for (locd=1; locd<=n; locd++)
					if (core[locd] == locd) {
						work2 = wk4[locd];
						if (aux4[locd] >= 0) {
							work2 = 0.5f * (work2 + wk3[locd]);
							if (work1 >= work2) {
								work1 = work2;
								tmpoptx = locd;
							}
						}
						else {
							if (aux5[locd] > 0) work2 += wk1[locd];
							if (work1 > work2) {
								work1 = work2;
								tmpoptx = locd;
							}
						}
					}
				work4 = ((float)high) / 2f;
				if (work1 >= work4) {
					sol[0][0] = 2;
					return;
				}
				if (aux4[tmpoptx] >= 0) {
					idxb = aux2[tmpoptx];
					idxc = aux3[tmpoptx];
					loca = core[idxb];
					locd = tmpoptx;
					loce = loca;
					while (true) {
						aux5[locd] = loce;
						idxa = aux4[locd];
						if (idxa == 0) break;
						loce = core[idxa];
						idxa = aux5[loce];
						locd = core[idxa];
					}
					hub = locd;
					locd = loca;
					loce = tmpoptx;
					while (true) {
						if (aux5[locd] > 0) break;
						aux5[locd] = loce;
						idxa = aux4[locd];
						if (idxa == 0) {
							// augmentation
							loch = 0;
							for (locb=1; locb<=n; locb++)
								if (core[locb] == locb) {
									idxd = aux4[locb];
									if (idxd >= 0) {
										if (idxd == 0) loch++;
										work2 = work1 - wk3[locb];
										wk3[locb] = 0f;
										wk1[locb] += work2;
										aux4[locb] = -idxd;
									}
									else {
										idxd = aux5[locb];
										if (idxd > 0) {
											work2 = wk4[locb] - work1;
											wk1[locb] += work2;
											aux5[locb] = -idxd;
										}
									}
								}
							while (true) {
								if (locd != loca) {
									loce = aux5[locd];
									aux5[locd] = 0;
									idxd = -aux5[loce];
									idxe = aux6[loce];
									aux4[locd] = -idxe;
									idxa = -aux4[loce];
									aux4[loce] = -idxd;
									locd = core[idxa];
								}
								else {
									if (loca == tmpoptx) break;
									aux5[loca] = 0;
									aux4[loca] = -idxc;
									aux4[tmpoptx] = -idxb;
									loca = tmpoptx;
									locd = hub;
								}
							}
							aux5[tmpoptx] = 0;
							idxa = 1;
							if (loch <= 2) {
								// generate the original graph by expanding all pseudonodes
								wt = 0;
								for (locb=1; locb<=n; locb++)
									if (core[locb] == locb) {
										idxb = -aux4[locb];
										if (idxb != nplus) {
											if (idxb >= 0) {
												loca = core[idxb];
												idxc = -aux4[loca];
												tmparg[0] = position;
												cpt_DuplicateEdges(neighbor,next,idxb,idxc,tmparg);
												position = tmparg[0];
												work1 = -(float) (weight[position]);
												work1 += wk1[locb] + wk1[loca];
												work1 += wk2[idxb] + wk2[idxc];
												if (Math.abs(work1) > eps) {
													sol[0][0] = 3;
													return;
												}
												wt += weight[position];
												aux4[loca] = idxb;
												aux4[locb] = idxc;
											}
										}
									}
								for (locb=1; locb<=n; locb++) {
									while (true) {
										if (aux1[locb] == locb) break;
										hub = core[locb];
										loca = aux1[hub];
										idxb = aux5[loca];
										if (idxb > 0) {
											idxd = aux2[loca];
											locd = loca;
											tmparg[0] = locd;
											cpt_ExpandBlossom(core,aux1,aux3,wk1,wk2,tmparg,idxd);
											locd = tmparg[0];
											aux1[hub] = idxd;
											work3 = wk3[loca];
											wk1[hub] = work3;
											while (true) {
												wk2[idxd] -= work3;
												if (idxd == hub) break;
												idxd = aux1[idxd];
											}
											idxb = aux4[hub];
											locd = core[idxb];
											if (locd != hub) {
												loca = aux5[locd];
												loca = core[loca];
												idxd = aux4[locd];
												aux4[locd] = idxb;
												do {
													loce = core[idxd];
													idxb = aux5[loce];
													idxc = aux6[loce];
													locd = core[idxb];
													tmparg[0] = position;
													cpt_DuplicateEdges(neighbor,next,idxb,idxc,tmparg);
													position = tmparg[0];
													work1 = -(float)(weight[position]);
													wt += weight[position];
													work1 += wk1[locd] + wk1[loce];
													work1 += wk2[idxb] + wk2[idxc];
													if (Math.abs(work1) > eps) {
														sol[0][0] = 3;
														return;
													}
													aux4[loce] = idxc;
													idxd = aux4[locd];
													aux4[locd] = idxb;
												} while (locd != hub);
												if (loca == hub) continue;
											}
											while (true) {
												idxd = aux4[loca];
												locd = core[idxd];
												idxe = aux4[locd];
												tmparg[0] = position;
												cpt_DuplicateEdges(neighbor,next,idxd,idxe,tmparg);
												position = tmparg[0];
												wt += weight[position];
												work1 = -(float)(weight[position]);
												work1 += wk1[loca] + wk1[locd];
												work1 += wk2[idxd] + wk2[idxe];
												if (Math.abs(work1) > eps) {
													sol[0][0] = 3;
													return;
												}
												aux4[loca] = idxe;
												aux4[locd] = idxd;
												idxc = aux5[locd];
												loca = core[idxc];
												if (loca == hub) break;
											}
											break;
										}
										else {
											idxc = aux4[hub];
											aux1[hub] = hub;
											work3 = wk2[hub];
											wk1[hub] = 0f;
											wk2[hub] = 0f;
											do {
												idxe = aux3[loca];
												idxd = aux1[idxe];
												tmparg[0] = loca;
												cpt_ExpandBlossom(core,aux1,aux3,wk1,wk2,tmparg,idxd);
												loca = tmparg[0];
												loce = core[idxc];
												if (loce != loca) {
													idxb = aux4[loca];
													tmparg[0] = position;
													cpt_DuplicateEdges(neighbor,next,hub,idxb,tmparg);
													position = tmparg[0];
													work1 = -(float)(weight[position]);
													wt += weight[position];
													work1 += wk2[idxb] + wk1[loca] + work3;
													if (Math.abs(work1) > eps) {
														sol[0][0] = 3;
														return;
													}
												}
												else
													aux4[loca] = idxc;
												loca = idxd;
											} while (loca != hub);
										}
									}
								}
								// store up the duplicate edges
								duparcs = 0;
								i = next[2];
								for (p=2; p<=n; p++) {
									loch = next[p+1] - 1;
									for (q=i; q<=loch; q++) {
										idxd = neighbor[q];
										if (idxd <= 0) {
											idxd = -idxd;
											if (idxd <= p) {
												duparcs++;
												sol[duparcs][1] = p;
												sol[duparcs][2] = idxd;
											}
										}
									}
									i = loch + 1;
								}
								cpt_Trail(n,neighbor,weight,next,aux3,core,startnode);
								// store up the optimal trail
								trail[1] = startnode;
								cur = startnode;
								curnext = 1;        
								do {
									p = next[cur];
									q = aux3[cur];
									complete = true;
									for (i=q; i>=p; i--) {
										if (weight[i] > 0) {
											curnext++;
											trail[curnext] = weight[i];
											cur = weight[i];
											weight[i] = -1;
											complete = false;
											break;
										}
									}
								} while (!complete);
								trail[0] = curnext;
								sol[3][0] = duparcs;
								sol[1][0] = totsolcost + wt;
								return;
							}
							tmparg[0] = idxa;
							cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
									aux3,aux4,wk1,wk2,wk3,wk4,tmparg,n);
							idxa = tmparg[0];
							continue iterate;
						}
						loce = core[idxa];
						idxa = aux5[loce];
						locd = core[idxa];
					}
					while (true) {
						if (locd == hub) {
							// shrink a blossom
							work3 = wk1[hub] + work1 - wk3[hub];
							wk1[hub] = 0f;
							idxe = hub;
							do {
								wk2[idxe] += work3;
								idxe = aux1[idxe];
							} while (idxe != hub);
							idxd = aux1[hub];
							skip = false;
							if (hub != loca) skip = true;
							do {
								if (!skip) {
									loca = tmpoptx;
									loce = aux5[hub];
								}
								skip = false;
								while (true) {
									aux1[idxe] = loce;
									idxa = -aux4[loce];
									aux4[loce] = idxa;
									wk1[loce] += wk4[loce] - work1;
									idxe = loce;
									tmparg[0] = idxe;
									cpt_ShrinkBlossom(core,aux1,wk1,wk2,hub,tmparg);
									idxe = tmparg[0];
									aux3[loce] = idxe;
									locd = core[idxa];
									aux1[idxe] = locd;
									wk1[locd] += work1 - wk3[locd];
									idxe = locd;
									tmparg[0] = idxe;
									cpt_ShrinkBlossom(core,aux1,wk1,wk2,hub,tmparg);
									idxe = tmparg[0];
									aux3[locd] = idxe;
									if (loca == locd) break;
									loce = aux5[locd];
									aux5[locd] = aux6[loce];
									aux6[locd] = aux5[loce];
								}
								if (loca == tmpoptx) {
									aux5[tmpoptx] = idxb;
									aux6[tmpoptx] = idxc;
									break;
								}
								aux5[loca] = idxc;
								aux6[loca] = idxb;
							} while (hub != tmpoptx);
							aux1[idxe] = idxd;
							loca = aux1[hub];
							aux2[loca] = idxd;
							wk3[loca] = work3;
							aux5[hub] = 0;
							wk4[hub] = high;
							wk3[hub] = work1;
							cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,
									aux3,aux4,wk1,wk2,wk3,wk4,hub);
							continue iterate;
						}
						locf = aux5[hub];
						aux5[hub] = 0;
						idxd = -aux4[locf];
						hub = core[idxd];
					}
				}
				else {
					if (aux5[tmpoptx] > 0) {
						loca = aux1[tmpoptx];
						if (loca != tmpoptx) {
							idxa = aux5[loca];
							if (idxa > 0) {
								// expand a blossom
								idxd = aux2[loca];
								locd = loca;
								tmparg[0] = locd;
								cpt_ExpandBlossom(core,aux1,aux3,wk1,wk2,tmparg,idxd);
								locd = tmparg[0];
								work3 = wk3[loca];
								wk1[tmpoptx] = work3;
								aux1[tmpoptx] = idxd;
								while (true) {
									wk2[idxd] -= work3;
									if (idxd == tmpoptx) break;
									idxd = aux1[idxd];
								}

								idxb = -aux4[tmpoptx];
								locd = core[idxb];
								idxc = aux4[locd];
								hub = core[idxc];
								if (hub != tmpoptx) {
									loce = hub;
									while (true) {
										idxa = aux5[loce];
										locd = core[idxa];
										if (locd == tmpoptx) break;
										idxa = aux4[locd];
										loce = core[idxa];
									}
									aux5[hub] = aux5[tmpoptx];
									aux5[tmpoptx] = aux6[loce];
									aux6[hub] = aux6[tmpoptx];
									aux6[tmpoptx] = idxa;
									idxd = aux4[hub];
									loca = core[idxd];
									idxe = aux4[loca];
									aux4[hub] = -idxb;
									locd = loca;
									while (true) {
										idxb = aux5[locd];
										idxc = aux6[locd];
										aux5[locd] = idxe;
										aux6[locd] = idxd;
										aux4[locd] = idxb;
										loce = core[idxb];
										idxd = aux4[loce];
										aux4[loce] = idxc;
										if (loce == tmpoptx) break;
										locd = core[idxd];
										idxe = aux4[locd];
										aux5[loce] = idxd;
										aux6[loce] = idxe;
									}
								}
								idxc = aux6[hub];
								locd = core[idxc];
								wk4[locd] = work1;
								if (locd != hub) {
									idxb = aux5[locd];
									loca = core[idxb];
									aux5[locd] = aux5[hub];
									aux6[locd] = idxc;
									do {
										idxa = aux4[locd];
										aux4[locd] = -idxa;
										loce = core[idxa];
										idxa = aux5[loce];
										aux5[loce] = -idxa;
										wk4[loce] = high;
										wk3[loce] = work1;
										locd = core[idxa];
										wk4[locd] = work1;
										cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,
												aux3,aux4,wk1,wk2,wk3,wk4,loce);
									} while (locd != hub);
									aux5[hub] = aux6[loce];
									aux6[hub] = idxa;
									if (loca == hub) continue iterate;
								}
								loce = loca;
								do {
									idxa = aux4[loce];
									aux4[loce] = -idxa;
									locd = core[idxa];
									aux5[loce] = -locd;
									idxa = aux5[locd];
									aux4[locd] = -aux4[locd];
									loce = core[idxa];
									aux5[locd] = -loce;
								} while (loce != hub);
								do {
									locd = -aux5[loca];
									tmparg[0] = loca;
									cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
											aux3,aux4,wk1,wk2,wk3,wk4,tmparg,loca);
									loca = tmparg[0];
									loca = -aux5[locd];
									tmparg[0] = locd;
									cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
											aux3,aux4,wk1,wk2,wk3,wk4,tmparg,locd);
									locd = tmparg[0];
								} while (loca != hub);
								continue iterate;
							}
						}
						// modify a blossom
						wk4[tmpoptx] = high;
						wk3[tmpoptx] = work1;
						i = 1;
						wk1[tmpoptx] = 0f;
						idxa = -aux4[tmpoptx];
						loca = core[idxa];
						idxb = aux4[loca];
						if (idxb == tmpoptx) {
							i = 2;
							aux4[loca] = idxa;
							idxd = aux1[tmpoptx];
							aux1[tmpoptx] = loca;
							wk1[loca] += work1 - wk3[loca];
							idxe = loca;
							tmparg[0] = idxe;
							cpt_ShrinkBlossom(core,aux1,wk1,wk2,tmpoptx,tmparg);
							idxe = tmparg[0];
							aux3[loca] = idxe;
							aux1[idxe] = idxd;
							idxb = aux6[tmpoptx];
							if (idxb == tmpoptx) {
								idxa = aux5[tmpoptx];
								loca = core[idxa];
								aux4[tmpoptx] = aux4[loca];
								aux4[loca] = idxa;
								aux5[tmpoptx] = 0;
								idxd = aux1[tmpoptx];
								aux1[tmpoptx] = loca;
								wk1[loca] += work1 - wk3[loca];
								idxe = loca;
								tmparg[0] = idxe;
								cpt_ShrinkBlossom(core,aux1,wk1,wk2,tmpoptx,tmparg);
								idxe = tmparg[0];
								aux3[loca] = idxe;
								aux1[idxe] = idxd;
								cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,
										aux3,aux4,wk1,wk2,wk3,wk4,tmpoptx);
								continue iterate;
							}
						}
						do {
							idxc = tmpoptx;
							locd = aux1[tmpoptx];
							while (true) {
								idxd = locd;
								idxe = aux3[locd];
								skip = false;
								while (true) {
									if (idxd == idxb) {
										skip = true;
										break;
									}
									if (idxd == idxe) break;
									idxd = aux1[idxd];
								}
								if (skip) break;
								locd = aux1[idxe];
								idxc = idxe;
							}
							idxd = aux1[idxe];
							aux1[idxc] = idxd;
							tmparg[0] = locd;
							cpt_ExpandBlossom(core,aux1,aux3,wk1,wk2,tmparg,idxd);
							locd = tmparg[0];
							wk4[locd] = work1;
							if (i == 2) {
								aux5[locd] = aux5[tmpoptx];
								aux6[locd] = idxb;
								aux5[tmpoptx] = 0;
								aux4[tmpoptx] = aux4[locd];
								aux4[locd] = -tmpoptx;
								cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,
										aux3,aux4,wk1,wk2,wk3,wk4,tmpoptx);
								continue iterate;
							}
							i = 2;
							aux5[locd] = tmpoptx;
							aux6[locd] = aux4[locd];
							aux4[locd] = -idxa;
							idxb = aux6[tmpoptx];
							if (idxb == tmpoptx) {
								idxa = aux5[tmpoptx];
								loca = core[idxa];
								aux4[tmpoptx] = aux4[loca];
								aux4[loca] = idxa;
								aux5[tmpoptx] = 0;
								idxd = aux1[tmpoptx];
								aux1[tmpoptx] = loca;
								wk1[loca] += work1 - wk3[loca];
								idxe = loca;
								tmparg[0] = idxe;
								cpt_ShrinkBlossom(core,aux1,wk1,wk2,tmpoptx,tmparg);
								idxe = tmparg[0];
								aux3[loca] = idxe;
								aux1[idxe] = idxd;
								cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,
										aux3,aux4,wk1,wk2,wk3,wk4,tmpoptx);
								continue iterate;
							}
						} while (core[idxb] == tmpoptx);
						aux5[locd] = aux5[tmpoptx];
						aux6[locd] = idxb;
						aux5[tmpoptx] = 0;
						locd = aux1[tmpoptx];
						if (locd == tmpoptx) {
							aux4[tmpoptx] = locg;
							tmpopty = tmpoptx;
							tmparg[0] = tmpopty;
							cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
									aux3,aux4,wk1,wk2,wk3,wk4,tmparg,tmpoptx);
							tmpopty = tmparg[0];
							continue iterate;
						}
						idxe = aux3[locd];
						idxd = aux1[idxe];
						aux1[tmpoptx] = idxd;
						tmparg[0] = locd;
						cpt_ExpandBlossom(core,aux1,aux3,wk1,wk2,tmparg,idxd);
						locd = tmparg[0];
						aux4[tmpoptx] = -aux4[locd];
						aux4[locd] = -tmpoptx;
						locc = locd;
						tmparg[0] = locc;
						cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
								aux3,aux4,wk1,wk2,wk3,wk4,tmparg,locd);
						locc = tmparg[0];
						tmpopty = tmpoptx;
						tmparg[0] = tmpopty;
						cpt_SecondScan(neighbor,weight,next,high,core,aux1,aux2,
								aux3,aux4,wk1,wk2,wk3,wk4,tmparg,tmpoptx);
						tmpopty = tmparg[0];
						continue iterate;
					}
					else {
						// grow an alternating tree
						idxa = -aux4[tmpoptx];
						if (idxa <= n) {
							aux5[tmpoptx] = aux2[tmpoptx];
							aux6[tmpoptx] = aux3[tmpoptx];
							loca = core[idxa];
							aux4[loca] = -aux4[loca];
							wk4[loca] = high;
							wk3[loca] = work1;
							cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,aux3,
									aux4,wk1,wk2,wk3,wk4,loca);
							continue iterate;
						}
						else {
							idxb = aux2[tmpoptx];
							loca = core[idxb];
							aux4[tmpoptx] = aux4[loca];
							wk4[tmpoptx] = high;
							wk3[tmpoptx] = work1;
							aux4[loca] = idxb;
							wk1[loca] += work1 - wk3[loca];
							idxe = loca;
							tmparg[0] = idxe;
							cpt_ShrinkBlossom(core,aux1,wk1,wk2,tmpoptx,tmparg);
							idxe = tmparg[0];
							aux3[loca] = idxe;
							aux1[tmpoptx] = loca;
							aux1[idxe] = tmpoptx;
							cpt_FirstScan(neighbor,weight,next,core,aux1,aux2,aux3,
									aux4,wk1,wk2,wk3,wk4,tmpoptx);
							continue iterate;
						}
					}
				}
			}
	}
	/**
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
	 * Solves a min cost flow problem defined on this graph.  Demands must be set, or else we get an error here.
	 * @param g
	 * @return answer - entry [0][0] holds the final cost, and the edge from node 
	 * [i][0] to [i][1] that has cost [i][2] has flow [i][3] in the optimal solution.
	 */
	public static int[][] minCostNetworkFlow(DirectedGraph g) throws GraphInfeasibleException, SetupException
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
			throw new SetupException("There was a problem setting up the min cost flow problem.  This probably indicates an " +
					"error in the minCostNetworkFlow method.");
		}

		//everything is probably okay
		int answer[][] = new int[edges+1][4];
		for(int j=1;j<edges+1;j++)
		{
			answer[j][0] = nodei[j];
			answer[j][1] = nodej[j];
			answer[j][2] =arccost[j];
			answer[j][3]= flowsol[j];
		}
		//store the final cost in [0][0]
		answer[0][0] = arccost[0];

		return answer;
	}
	/**
	 * Solves a min cost flow problem defined on the network ( directed graph ).  Implementation is taken from Lau:
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
	public static Set<Pair<Vertex>> minCostMatching(Graph<Vertex,Link<Vertex>> graph)
	{
		return null;
	}

}
