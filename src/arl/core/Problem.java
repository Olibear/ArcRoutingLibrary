package arl.core;

import java.util.Collection;

import arl.problem.impl.ObjectiveFunction;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * @author oliverlum
 *
 */
public abstract class Problem {
	
	//local fields
	protected Graph<Vertex, Edge> mGraph;
	protected ObjectiveFunction mObj;
	
	/**
	 * Basic constructor
	 * @param g
	 */
	public Problem(Graph<Vertex, Edge> g, ObjectiveFunction o){ 
		mGraph = g;
		mObj = o;
	};
	/**
	 * Evaluates the objective function associated with this problem.
	 * @return the objective function's value given a candidate set of routes.
	 */
	public abstract double evaluateObjective(Collection<Route> routes);
	/**
	 * Says whether the provided set of routes is a feasible solution
	 * @return true if problem instance is solvable; false oth.
	 */
	public abstract boolean isFeasible(Collection<Route> routes);

}
