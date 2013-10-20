package oarlib.solver.impl;

import java.util.Collection;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;

public class DCPPSolver extends Solver{

	public DCPPSolver(Problem instance) throws IllegalArgumentException {
		super(instance);
	}

	@Override
	protected Collection<Route> solve() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

}
