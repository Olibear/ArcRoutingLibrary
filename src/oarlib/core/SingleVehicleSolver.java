/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;
import org.apache.log4j.Logger;

import java.util.Collection;


/**
 * Solver abstraction. Most general contract that Single Vehicle Solvers must fulfill.
 *
 * @author oliverlum
 */
public abstract class SingleVehicleSolver<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> {

    private static final Logger LOGGER = Logger.getLogger(SingleVehicleSolver.class);
    protected Problem<V, E, G> mInstance;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected SingleVehicleSolver(Problem<V, E, G> instance) throws IllegalArgumentException {
        //make sure I'm a valid problem instance
        if (!(instance.getProblemType() == getProblemType())) {
            throw new IllegalArgumentException("It appears that this problem does not match the problem type handled by this solver.");
        }
        mInstance = instance;
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
     * @return - the problem type
     */
    public abstract Problem.Type getProblemType();

    /**
     * Essentially a toString method for the current solution, it can include meta data output, or whatever the solver
     * decides to include.
     *
     * @return - a string representation of the current solution
     * @throws IllegalStateException - if solve hasn't been called yet
     */
    public String printCurrentSol() throws IllegalStateException {
        Collection<Route<V, E>> currSol = mInstance.getSol();
        if (currSol.size() > 1)
            LOGGER.error("It appears as though this solution contains multiple routes to a single vehicle problem.", new IllegalStateException());
        if (currSol == null)
            LOGGER.error("It does not appear as though this solver has been run yet!", new IllegalStateException());

        for (Route<V, E> r : currSol) {
            String ans = this.getSolverName() + ": Printing current solution...";
            ans += "\n";
            ans += "=======================================================";
            ans += "\n";
            ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
            ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
            ans += "Route Cost: " + r.getCost() + "\n";
            ans += "\n";
            ans += "=======================================================";
            ans += "\n";
            ans += "\n";
            ans += currSol.toString();
            ans += "\n";
            ans += "\n";
            ans += "=======================================================";

            return ans;
        }
        return null;
    }

    public abstract String getSolverName();

}
