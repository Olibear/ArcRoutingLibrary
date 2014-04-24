package oarlib.solver.impl;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.HashMap;

import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Edge;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.UndirectedVertex;

public class UCPPSolver_Gurobi extends SingleVehicleSolver{

	UndirectedCPP mInstance;

	public UCPPSolver_Gurobi(UndirectedCPP instance) throws IllegalArgumentException {
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
			UndirectedGraph mGraph = mInstance.getGraph();
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
		try
		{
			//copy to operate on
			UndirectedGraph copy = mInstance.getGraph().getDeepCopy();
			
			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> oddVertices;
			
			//the answers
			int l;
			int myCost = 0;
			int trueCost = 0;
			
			int n = copy.getVertices().size();
			int[][] dist = new int[n+1][n+1];
			int[][] path = new int[n+1][n+1];
			int[][] edgePath = new int[n+1][n+1];
			CommonAlgorithms.fwLeastCostPaths(copy, dist, path, edgePath);

			//set up oddVertices
			oddVertices = new ArrayList<Integer>();
			for(UndirectedVertex v: copy.getVertices())
			{
				if(v.getDegree() %2 == 1)
					oddVertices.add(v.getId());
			}

			//Now set up the model in Gurobi and solve it, and see if you get the right answer
			model = new GRBModel(env);
			//put in the base cost of all the edges that we'll add to the objective
			for(Edge a: copy.getEdges())
				trueCost+=a.getCost();

			//create variables
			//after this snippet, element[j][k] contains the variable x_jk which represents the
			//number of paths from vertex oddVertices.get(j) to oddVertices.get(k) that we add to the graph to make it Eulerian.
			l = oddVertices.size();
			varArray = new GRBVar[l][l];
			for(int j=0; j<l;j++)
			{
				for(int k=0; k<l; k++)
				{
					if(j==k)
						continue;
					varArray[j][k] = model.addVar(0.0,1.0,dist[oddVertices.get(j)][oddVertices.get(k)], GRB.BINARY, "x" + oddVertices.get(j) + oddVertices.get(k));
				}
			}

			//update the model
			model.update();


			//create constraints
			for(int j=0; j<l; j++)
			{
				expr = new GRBLinExpr();
				//for each j, sum up the x_jk and make sure they equal 1
				for(int k=0; k<l; k++)
				{
					if(j==k)
						continue;
					expr.addTerm(1, varArray[j][k]);
				}
				model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
			}
			for(int j=0; j<l; j++)
			{
				expr = new GRBLinExpr();
				//for each k, sum up the x_jk and make sure they equal 1
				for(int k=0; k<l; k++)
				{
					if(j==k)
						continue;
					expr.addTerm(1, varArray[k][j]);
				}
				model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
			}
			for(int j=0; j<l; j++)
			{
				if(j==0)
					continue;
				expr = new GRBLinExpr();
				//enforce symmetry
				for(int k=0; k<j; k++)
				{
					expr.addTerm(1, varArray[j][k]);
					expr.addTerm(-1, varArray[k][j]);
				}
				model.addConstr(expr, GRB.EQUAL, 0, "cj"+j);
			}
			model.optimize();
			trueCost+=model.get(GRB.DoubleAttr.ObjVal)/2;
			
			for(int j = 0; j < l; j++)
			{
				for(int k = 0; k < l; k++)
				{
					if(varArray[j][k].get(GRB.DoubleAttr.X) > 0)
						CommonAlgorithms.addShortestPath(copy, dist, path, edgePath, new Pair<Integer>(oddVertices.get(j), oddVertices.get(k)));
				}
			}
			
			System.out.println("myCost = " + myCost + ", trueCost = " + trueCost);
			
			//return the answer
			HashMap<Integer, Edge> indexedEdges = copy.getInternalEdgeMap();
			ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
			Tour eulerTour = new Tour();
			for (int i=0;i<ans.size();i++)
			{
				eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
			}
			return eulerTour;
			
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Type getProblemType() {
		return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
	}

}
