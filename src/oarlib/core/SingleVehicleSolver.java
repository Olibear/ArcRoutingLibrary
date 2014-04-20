package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;


/**
 * Solver abstraction. Most general contract that Solvers must fulfill.
 * @author oliverlum
 *
 */
public abstract class SingleVehicleSolver {
	/**
	 * Default constructor; must set problem instance.
	 * @param instance - instance for which this is a solver
	 */
	public SingleVehicleSolver(Problem instance) throws IllegalArgumentException{
		//make sure I'm a valid problem instance
		if(!(instance.getType() == getProblemType()))
		{
			throw new IllegalArgumentException();
		}
	}
	/**
	 * Attempts to solve the instance assigned to this problem.  
	 * @return null if problem instance is not assigned.
	 */
	public Route trySolve() throws GraphInfeasibleException{
		if(!checkGraphRequirements())
			throw new GraphInfeasibleException();
		return solve();
	}
	/**
	 * Determines if the graph meets theoretical requirements for this solver to be run on it.
	 * @return - true if the graph meets pre-requisites (usually connectivity-related), false oth.
	 */
	protected abstract boolean checkGraphRequirements();
	/**
	 * @return - the problem instance
	 */
	protected abstract Problem getInstance();
	/**
	 * Actually solves the instance, (first checking for feasibility), returning a Collection of routes.
	 * @return The set of routes the solver has concluded is best.
	 */
	protected abstract Route solve();
	/**
	 * Specifies what type of problem this is a solver for.
	 * @return
	 */
	public abstract Problem.Type getProblemType();

}
