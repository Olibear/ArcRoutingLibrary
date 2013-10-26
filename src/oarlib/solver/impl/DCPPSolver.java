package oarlib.solver.impl;

import java.util.Collection;
import java.util.LinkedHashSet;

import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.problem.impl.DirectedCPP;
import oarlib.vertex.impl.DirectedVertex;

public class DCPPSolver extends Solver{
	
	DirectedGraph<Arc> mGraph;

	public DCPPSolver(DirectedCPP instance) throws IllegalArgumentException {
		super(instance);
	}

	@Override
	protected Collection<Route> solve() {
		
		LinkedHashSet<DirectedVertex> Dplus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dminus = new LinkedHashSet<DirectedVertex>();
		LinkedHashSet<DirectedVertex> Dall = new LinkedHashSet<DirectedVertex>();
		
		//prepare our unbalanced vertex sets
		for(DirectedVertex v: mGraph.getVertices())
		{
			if(v.getDelta() > 0)
			{
				Dplus.add(v);
				Dall.add(v);
			}
			if(v.getDelta() < 0)
			{
				Dminus.add(v);
				Dall.add(v);
			}
		}
		
		int n = mGraph.getVertices().size();
		int[][] dist = new int[n][n];
		//construct the distance matrix
		for(Arc a: mGraph.getEdges())
		{
			dist[a.getTail().getId()][a.getHead().getId()] = a.getCost();
		}
		for(int i = 0;i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				//set the zero guys to big unless it's on the diagonal.
			}
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

}
