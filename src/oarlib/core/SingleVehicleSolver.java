package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;


/**
 * Solver abstraction. Most general contract that Single Vehicle Solvers must fulfill.
 *
 * @author oliverlum
 */
public abstract class SingleVehicleSolver {

    protected Route currSol;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected SingleVehicleSolver(Problem instance) throws IllegalArgumentException {
        //make sure I'm a valid problem instance
        if (!(instance.getType() == getProblemType())) {
            throw new IllegalArgumentException("It appears that this problem does not match the problem type handled by this solver.");
        }
    }

    /**
     * Attempts to solve the instance assigned to this problem.
     *
     * @return null if problem instance is not assigned, or solver failed.
     */
    public Route trySolve() throws GraphInfeasibleException {
        if (!checkGraphRequirements())
            throw new GraphInfeasibleException();
        return solve();
    }

    /**
     * Determines if the graph meets theoretical requirements for this solver to be run on it.
     *
     * @return - true if the graph meets pre-requisites (usually connectivity-related), false oth.
     */
    protected abstract boolean checkGraphRequirements();

    /**
     * @return - the problem instance
     */
    protected abstract Problem getInstance();

    /**
     * Actually solves the instance, (first checking for feasibility), returning a Collection of routes.
     *
     * @return The set of routes the solver has concluded is best.
     */
    protected abstract Route solve();

    /**
     * Specifies what type of problem this is a solver for.
     *
     * @return
     */
    public abstract Problem.Type getProblemType();

    /**
     * Essentially a toString method for the current solution, it can include meta data output, or whatever the solver
     * decides to include.
     *
     * @return - a string representation of the current solution
     * @throws IllegalStateException - if solve hasn't been called yet
     */
    public abstract String printCurrentSol() throws IllegalStateException;

}
