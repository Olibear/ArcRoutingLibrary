package oarlib.solver.impl;

import java.util.Collection;
import java.util.LinkedHashSet;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.problem.impl.DirectedCPP;
import oarlib.vertex.impl.DirectedVertex;

public class DCPPSolver extends Solver{
	
	DirectedGraph mGraph;

	public DCPPSolver(DirectedCPP instance) throws IllegalArgumentException {
		super(instance);
	}

	@Override
	protected Collection<Route> solve() {
		
		LinkedHashSet<DirectedVertex> Dplus;
		LinkedHashSet<DirectedVertex> Dminus;
		LinkedHashSet<DirectedVertex> Dall;
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

}
