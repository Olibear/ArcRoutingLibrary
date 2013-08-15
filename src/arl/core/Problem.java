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
	protected Graph<Vertex, Link> mGraph;
	protected ObjectiveFunction mObj;
	
	/**
	 * Basic constructor
	 * @param g
	 */
	public Problem(Graph<Vertex, Link> g, ObjectiveFunction o){ 
		mGraph = g;
		mObj = o;
	};
	/**
	 * Says whether the provided set of routes is a feasible solution
	 * @return true if problem instance is solvable; false oth.
	 */
	public abstract boolean isFeasible(Collection<Route> routes);

}
