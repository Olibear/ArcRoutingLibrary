package arl.problem.impl;

import java.util.Collection;

import arl.core.Route;

/**
 * General form of an objective function.
 * @author oliverlum
 *
 */
public abstract class ObjectiveFunction {

	/**
	 * Evaluates the objective function associated with this problem.
	 * @return the objective function's value at its current state.
	 */
	public abstract double evaluateObjective();

}
