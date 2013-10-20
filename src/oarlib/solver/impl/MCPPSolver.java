package oarlib.solver.impl;

import java.util.Collection;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;

public class MCPPSolver extends Solver{

	public MCPPSolver(Problem instance) throws IllegalArgumentException {
		super(instance);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Collection<Route> solve() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.MIXED_CHINESE_POSTMAN;
	}

}
