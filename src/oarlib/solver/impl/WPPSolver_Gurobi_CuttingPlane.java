package oarlib.solver.impl;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.HashMap;
import java.util.HashSet;
import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.core.WindyEdge;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.WindyCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;

/**
 * 
 * @author oliverlum
 * 
 * An IP formulation of the WPP given by Win.  The variables are as follows:
 * 
 * c_ij: the cost of going from vertex i to vertex j
 * x_ij: the number of times an edge is traversed from vertex i to vertex j is included in the augmentation
 *
 */
public class WPPSolver_Gurobi_CuttingPlane extends SingleVehicleSolver{

	WindyCPP mInstance;

	public WPPSolver_Gurobi_CuttingPlane(WindyCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	@Override
	protected boolean checkGraphRequirements() {
		// make sure the graph is connected
		if(mInstance.getGraph() == null)
			return false;
		else
		{
			WindyGraph mGraph = mInstance.getGraph();
			if(!CommonAlgorithms.isConnected(mGraph))
				return false;
		}
		return true;
	}

	@Override
	protected Problem getInstance() {
		return mInstance;
	}

	@Override
	protected Route solve() {

		double time = 0;
		try
		{
			//copy
			WindyGraph copy = mInstance.getGraph().getDeepCopy();
			HashMap<Integer, WindyEdge> indexedEdges = copy.getInternalEdgeMap();
			int n = copy.getVertices().size();
			int m = copy.getEdges().size();

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			env.set(GRB.IntParam.OutputFlag, 0);
			env.set(GRB.DoubleParam.MIPGap, 0); //insist on optimality
			GRBModel  model;

			//Now set up the model in Gurobi and solve it, and see if you get the right answer
			model = new GRBModel(env);

			//create and add vars
			int i,j;
			GRBVar[][] vars = new GRBVar[n+1][n+1];
			WindyEdge tempEdge;
			GRBLinExpr[] edgeConstr = new GRBLinExpr[m];
			GRBLinExpr[] vertexConstr = new GRBLinExpr[n];
			for(int k = 1; k <= m; k++)
			{
				tempEdge = indexedEdges.get(k);
				i = tempEdge.getEndpoints().getFirst().getId();
				j = tempEdge.getEndpoints().getSecond().getId();
				vars[i][j] = model.addVar(0.0, Double.MAX_VALUE, tempEdge.getCost(), GRB.CONTINUOUS, "x_" + i + j);
				vars[j][i] = model.addVar(0.0,Double.MAX_VALUE, tempEdge.getReverseCost(), GRB.CONTINUOUS, "x_" + j + i);

				//add the edge constraint here
				edgeConstr[k-1] = new GRBLinExpr();
				edgeConstr[k-1].addTerm(1, vars[i][j]);
				edgeConstr[k-1].addTerm(1, vars[j][i]);
			}

			for(int k = 1; k <= n; k++)
			{
				vertexConstr[k-1] = new GRBLinExpr();
				for(int l = 1; l <= n; l++)
				{
					if(vars[k][l] != null)
					{
						vertexConstr[k-1].addTerm(1, vars[k][l]);
						vertexConstr[k-1].addTerm(-1, vars[l][k]);
					}

				}
			}

			//update
			model.update();

			//finalize the constraints
			for(GRBLinExpr ec : edgeConstr)
				model.addConstr(ec, GRB.GREATER_EQUAL, 1, "edge_constraint");
			for(GRBLinExpr vc : vertexConstr)
				model.addConstr(vc,  GRB.EQUAL, 0, "vertex_constraint");


			//Step 2: optimize
			long start = System.nanoTime();
			model.optimize();
			long end = System.nanoTime();
			time += (end-start)/(1e6);


			boolean foundViolatedConstraint = true;
			boolean optimal;
			double epsilon = .1;
			double epsilon2 = .2;
			UndirectedGraph Gprime = new UndirectedGraph(n);
			HashSet<HashSet<Integer>> oddComponents;
			HashSet<GRBConstr> oddCutConstr = new HashSet<GRBConstr>();
			while(foundViolatedConstraint)
			{
				//Step 3: check for optimality
				optimal = true;
				foundViolatedConstraint = false;
				for(GRBVar var : model.getVars())
				{
					if(var.get(GRB.DoubleAttr.X) % 1 != 0)
					{
						optimal = false;
						break;
					}
				}

				if(optimal)
					break; //done, solution is complete integral

				//Step 4: delete non-binding constraints
				for(GRBConstr constr: oddCutConstr)
					if(constr.get(GRB.DoubleAttr.Slack) > epsilon)
						model.remove(constr);

				//Step 5: find violated odd cut constraints
				Gprime.clearEdges();
				for(int k = 1; k <= n; k++)
				{
					for(int l = 1; l < k; l++)
					{
						if(vars[k][l] != null)
						{
							if(vars[k][l].get(GRB.DoubleAttr.X) + vars[l][k].get(GRB.DoubleAttr.X) > 1)
								Gprime.addEdge(l, k, 1);
						}

					}
				}

				oddComponents = new HashSet<HashSet<Integer>>();
				oddComponents.addAll(heuristic1(Gprime, copy));

				Gprime.clearEdges();
				for(int k = 1; k <= n; k++)
				{
					for(int l = 1; l < k; l++)
					{
						if(vars[k][l] != null)
						{
							if(vars[k][l].get(GRB.DoubleAttr.X) + vars[l][k].get(GRB.DoubleAttr.X) > 1 + epsilon2)
								Gprime.addEdge(l, k, 1);
						}

					}
				}

				oddComponents.addAll(heuristic2(Gprime, copy, vars));

				//step 6: add them
				for(HashSet<Integer> comp : oddComponents)
				{
					foundViolatedConstraint = true;
					GRBLinExpr occ = new GRBLinExpr();
					for(Integer in : comp)
					{
						for(int l = 1; l <= n; l++)
						{
							if(!comp.contains(l) && vars[in][l] != null)
							{
								occ.addTerm(1, vars[in][l]);
								occ.addTerm(1, vars[l][in]);
								occ.addConstant(-1);
							}
						}
					}
					oddCutConstr.add(model.addConstr(occ, GRB.GREATER_EQUAL, 1, "oddCut_Constr"));
				}

				model.update();
				start = System.nanoTime();
				model.optimize();
				end = System.nanoTime();
				time += (end-start)/(1e6);
			}

			//Step 7: construct a tour from the current solution
			int numFractional = 0;
			for(GRBVar var : model.getVars())
			{
				if(var.get(GRB.DoubleAttr.X) % 1 != 0)
				{
					numFractional++;
				}
			}
			System.out.println(numFractional);
			System.out.println("REALTALK TIME: " + time);
			//return the answer
			//ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
			Tour eulerTour = new Tour();
			//for (int i=0;i<ans.size();i++)
			//{
			//eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
			//}
			
			

			//print the obj value.
			System.out.println(model.get(GRB.DoubleAttr.ObjVal));

			return eulerTour;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private HashSet<HashSet<Integer>> heuristic1(UndirectedGraph Gprime, WindyGraph G)
	{
		//find connected components
		int n = Gprime.getVertices().size();
		int m = Gprime.getEdges().size();

		int[] components = new int[n+1];
		int[] nodei = new int[m+1];
		int[] nodej = new int[m+1];

		int i = 1;
		for(Edge e: Gprime.getEdges())
		{
			nodei[i] = e.getEndpoints().getFirst().getId();
			nodej[i] = e.getEndpoints().getSecond().getId();
			i++;
		}

		CommonAlgorithms.connectedComponents(n, m, nodei, nodej, components);

		HashMap<Integer, WindyVertex> gVertices = G.getInternalVertexMap();
		HashSet<HashSet<Integer>> ans = new HashSet<HashSet<Integer>>();
		WindyVertex temp;
		HashMap<Integer, HashSet<Integer>> candidateCuts = new HashMap<Integer, HashSet<Integer>>();
		for(int j = 1; j <= components[0]; j++)
		{
			candidateCuts.put(j, new HashSet<Integer>());
		}
		int[] componentParity = new int[components[0]+1];
		for(int j = 1; j <= n; j++)
		{
			temp = gVertices.get(j);
			if(temp.getDegree() % 2 == 1)
				componentParity[components[j]] = componentParity[components[j]] + 1;
		}

		for(int j = 1; j <= n; j++)
		{
			if(componentParity[components[j]] % 2 == 1) //this component has an odd number of odd vertices
				candidateCuts.get(components[j]).add(j);		
		}

		for(Integer cutComp : candidateCuts.keySet())
		{
			if(candidateCuts.get(cutComp).isEmpty())
				continue;
			ans.add(candidateCuts.get(cutComp));
		}

		return ans;
	}

	private HashSet<HashSet<Integer>> heuristic2(UndirectedGraph Gprime, WindyGraph G, GRBVar[][] currentSol)
	{
		try
		{
			//find connected components
			int n = Gprime.getVertices().size();
			int m = Gprime.getEdges().size();

			int[] components = new int[n+1];
			int[] nodei = new int[m+1];
			int[] nodej = new int[m+1];

			int i = 1;
			for(Edge e: Gprime.getEdges())
			{
				nodei[i] = e.getEndpoints().getFirst().getId();
				nodej[i] = e.getEndpoints().getSecond().getId();
				i++;
			}

			CommonAlgorithms.connectedComponents(n, m, nodei, nodej, components);

			HashMap<Integer, WindyVertex> gVertices = G.getInternalVertexMap();
			HashSet<HashSet<Integer>> ans = new HashSet<HashSet<Integer>>();
			WindyVertex temp;
			HashMap<Integer, HashSet<Integer>> candidateCuts = new HashMap<Integer, HashSet<Integer>>();
			for(int j = 1; j <= components[0]; j++)
			{
				candidateCuts.put(j, new HashSet<Integer>());
			}

			for(int j = 1; j <= n; j++)
			{
				candidateCuts.get(components[j]).add(j);		
			}

			//determine if the cutset actually represents a violated constraint
			HashSet<Integer> tempSet;
			int cutCardinality;
			double tempSum;
			for(int j = 1; j < components[0]; j ++)
			{
				tempSet = candidateCuts.get(j);
				cutCardinality = 0;
				tempSum = 0;
				for(Integer vid: tempSet)
				{
					temp = gVertices.get(vid);
					for(WindyVertex v2: temp.getNeighbors().keySet())
					{
						if(tempSet.contains(v2.getId()))
							continue;
						//this edge is in the cut
						cutCardinality++;
						tempSum += currentSol[vid][v2.getId()].get(GRB.DoubleAttr.X) + currentSol[v2.getId()][vid].get(GRB.DoubleAttr.X);
					}
				}
				if(cutCardinality % 2 == 1 && tempSum < cutCardinality + 1)
					ans.add(tempSet);
			}

			return ans;
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Type getProblemType() {
		return Problem.Type.WINDY_CHINESE_POSTMAN;
	}

}
