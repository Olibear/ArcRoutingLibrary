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

import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Solver abstraction.  Most general contract that Multivehicle solvers must fulfill.
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleSolver<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> extends Solver<V, E, G> {

    private static final Logger LOGGER = Logger.getLogger(MultiVehicleSolver.class);

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected MultiVehicleSolver(Problem<V, E, G> instance) throws IllegalArgumentException {
        super(instance);
    }

    /**
     * Runs the vertex-weighted partitioning code from the METIS library, (the gpmetis program) on the graph file provided.
     *
     * @param numParts - the number of parts to partition the graph into, (e.g. 4 means the vertices will be partitioned
     *                 4 parts.
     * @param filename - the file path to the location of the graph file.  In order to work properly, this file must be in the
     *                 format expected by the METIS library; see the manual for details.
     */
    protected void runMetis(int numParts, String filename) {
        try {
            //run gpmetis
            String[] args1 = {"/usr/local/bin/gpmetis", filename, "" + numParts, "-contig", "-minconn", "-niter=1000", "-ncuts=1000", "-ufactor=1"};
            Runtime r = Runtime.getRuntime();
            LOGGER.debug("Start");
            Process p = r.exec(args1);
            p.waitFor();
            int exitVal = p.exitValue();
            LOGGER.debug("Stop " + exitVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Essentially a toString method for the current solution, it can include meta data output, or whatever the solver
     * decides to include.
     *
     * @return - a string representation of the current solution
     * @throws IllegalStateException - if solve hasn't been called yet
     */
    public String printCurrentSol() throws IllegalStateException {

        Collection<Route<V, E>> currSol = mInstance.getSol();

        if (currSol == null)
            LOGGER.error("It does not appear as though this solver has been run yet!", new IllegalStateException());

        int tempCost;
        int numZeroRoutes = 0;
        int totalCost = 0;
        int minLength = Integer.MAX_VALUE;
        int maxLength = Integer.MIN_VALUE;
        double percentVariance, averageCost, averageCostNoEmpty;
        double deviationFromAverage, deviationFromAverageNoEmpty;
        int addedCost = 0;

        for (Link l : mInstance.getGraph().getEdges())
            addedCost -= l.getCost();


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += this.getSolverName() + ": Printing current solution...";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";
        for (Route<V, E> r : currSol) {
            //gather metrics
            tempCost = r.getCost();

            if (tempCost == 0)
                numZeroRoutes++;

            if (tempCost < minLength)
                minLength = tempCost;

            if (tempCost > maxLength)
                maxLength = tempCost;

            totalCost += tempCost;

            ans += "\n";
            ans += "Route: " + r.toString() + "\n";
            ans += "Route Cost: " + tempCost + "\n";
            ans += "\n";
        }

        percentVariance = ((double) maxLength - minLength) / maxLength;
        averageCost = (double) totalCost / currSol.size();
        averageCostNoEmpty = (double) totalCost / (currSol.size() - numZeroRoutes);
        deviationFromAverage = ((double) maxLength - averageCost) / maxLength;
        deviationFromAverageNoEmpty = ((double) maxLength - averageCostNoEmpty) / maxLength;
        addedCost += totalCost;


        ans += "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
        ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
        ans += "Max Route Length: " + maxLength + "\n";
        ans += "Min Route Length: " + minLength + "\n";
        ans += "Average Route Length: " + averageCost + "\n";
        ans += "Average RouteLength (excluding empty): " + averageCostNoEmpty + "\n";
        ans += "% variance: " + 100.0 * percentVariance + "\n";
        ans += "% deviation from average length: " + 100.0 * deviationFromAverage + "\n";
        ans += "% deviation from average length (excluding empty): " + 100.0 * deviationFromAverageNoEmpty + "\n";
        ans += "Added cost: " + addedCost + "\n";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";

        return ans;
    }
}
