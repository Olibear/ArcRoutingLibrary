package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;
import oarlib.problem.impl.MultiVehicleProblem;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Solver abstraction.  Most general contract that Multivehicle solvers must fulfill.
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleSolver<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> {

    private static final Logger LOGGER = Logger.getLogger(MultiVehicleSolver.class);
    MultiVehicleProblem<V,E,G> mInstance;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected MultiVehicleSolver(MultiVehicleProblem instance) throws IllegalArgumentException {
        //make sure I'm a valid problem instance
        if (!(instance.getProblemType() == getProblemType())) {
            LOGGER.error("It appears that this problem does not match the problem type handled by this solver.");
            throw new IllegalArgumentException();
        }
        mInstance = instance;

    }

    /**
     * Attempts to solve the instance assigned to this problem.
     *
     * @return null if problem instance is not assigned, or solver failed.
     */
    public Collection<Route<V,E>> trySolve() throws GraphInfeasibleException {
        if (!checkGraphRequirements()) {
            LOGGER.error("It does not appear as though it is possible to solve the problem on this graph.");
            throw new GraphInfeasibleException();
        }
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
    protected abstract MultiVehicleProblem getInstance();

    /**
     * Actually solves the instance, (first checking for feasibility), returning a Collection of routes.
     *
     * @return The set of routes the solver has concluded is best.
     */
    protected abstract Collection<Route<V,E>> solve();

    /**
     * Specifies what type of problem this is a solver for.
     *
     * @return
     */
    public abstract Problem.Type getProblemType();

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
            String[] args1 = {"/Users/oliverlum/Downloads/metis-5.1.0/build/Darwin-x86_64/programs/gpmetis", filename, "" + numParts, "-contig", "-minconn", "-niter=1000", "-ncuts=1000", "-ufactor=1"};
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

        Collection<Route<V,E>> currSol = mInstance.getSol();

        if (currSol == null)
            LOGGER.error("It does not appear as though this solver has been run yet!",new IllegalStateException());

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
        ans += "CapacitatedDCPPSolver: Printing current solution...";
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
