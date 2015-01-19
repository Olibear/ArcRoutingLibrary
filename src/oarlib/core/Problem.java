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

import oarlib.metrics.Metric;
import oarlib.problem.impl.ProblemAttributes;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Problem abstraction.  Most general contract that Problem objects must fulfill.
 * If you wish to solve a capacitated problem, please see CapacitatedProblem.
 *
 * @author oliverlum
 */
public abstract class Problem<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> {

    private static final Logger LOGGER = Logger.getLogger(Problem.class);

    protected String mName = "";
    protected G mGraph;
    protected Collection<Route<V, E>> mSol;
    protected boolean solved;
    protected Metric mObjFunc;
    protected int mNumVehicles;

    protected Problem(G graph, String name, Metric objFunc) {
        mName = name;
        mGraph = graph;
        solved = false;
        mObjFunc = objFunc;
        mNumVehicles = 1;
    }

    public String getName() {
        return mName;
    }

    /**
     * Retrieve the graph that has been associated with this Problem
     *
     * @return the graph
     */
    public G getGraph() {
        return mGraph;
    }

    public Collection<Route<V, E>> getSol() {
        if (!solved) {
            LOGGER.error("No solution has been set for this problem yet.");
        }
        return mSol;
    }

    public void setSol(Collection<Route<V, E>> newSol) {
        mSol = newSol;
        solved = true;
    }

    public void setSol(Route<V, E> newSol) {
        ArrayList<Route<V, E>> container = new ArrayList<Route<V, E>>();
        container.add(newSol);
        mSol = container;
        solved = true;
    }

    /**
     * Says whether the provided set of routes is a feasible solution
     *
     * @return true if problem instance is solvable; false oth.
     */
    public abstract boolean isFeasible(Collection<Route<V, E>> routes);

    protected void setNumVehicles(int newNum) {
        mNumVehicles = newNum;
    }

    /**
     * Get the number of vehicles that a feasible solution to this problem is allowed to have.
     * If number of vehicles is not being enforced, -1 is returned.
     *
     * @return - the max number of routes allowed to exist in a feasible solution, or -1 if there is
     * no limit set.
     */
    public int getmNumVehicles() {
        return mNumVehicles;
    }

    public abstract ProblemAttributes getProblemAttributes();

    /**
     * @return - The objective function that this problem instance will use to evaluate its quality
     */
    public Metric getObjectiveFunction() {
        return mObjFunc;
    }
}
