package arl.core;

import java.util.Collection;

import arl.core.problem.impl.ObjectiveFunction;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * @author oliverlum
 *
 */
public abstract class Problem {
	
	//local fields
	protected Graph mGraph;
	protected ObjectiveFunction mObj;
	
	/**
	 * Basic constructor
	 * @param g
	 */
	public Problem(Graph g, ObjectiveFunction o){ 
		mGraph = g;
		mObj = o;
	};
	/**
	 * Evaluates the objective function associated with this problem.
	 * @return the objective function's value given a candidate set of routes.
	 */
	public abstract double evaluateObjective(Collection<Route> routes);
	/**
	 * Says whether the current state of the Problem is solvable
	 * @return true if problem instance is solvable; false oth.
	 */
	public abstract boolean isFeasible();

}
