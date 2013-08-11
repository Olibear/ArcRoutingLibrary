package arl.core;

import java.util.Collection;

/**
 * Problem interface.  Most general contract that Problem objects must fulfill.
 * @author oliverlum
 *
 */
public interface Problem {
	/**
	 * Evaluates the objective function associated with this problem.
	 * @return the objective function's value given a candidate set of routes.
	 */
	public double evaluateObjective(Collection<Route> routes);

}
