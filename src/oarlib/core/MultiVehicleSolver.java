package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;
import oarlib.problem.impl.MultiVehicleProblem;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Solver abstraction.  Most general contract that Multivehicle solvers must fulfill.
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleSolver {

    private static final Logger LOGGER = Logger.getLogger(MultiVehicleSolver.class);

    protected Collection<Route> currSol;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected MultiVehicleSolver(MultiVehicleProblem instance) throws IllegalArgumentException {
        //make sure I'm a valid problem instance
        if (!(instance.getProblemType() == getProblemType())) {
            throw new IllegalArgumentException("It appears that this problem does not match the problem type handled by this solver.");
        }

    }

    /**
     * Attempts to solve the instance assigned to this problem.
     *
     * @return null if problem instance is not assigned, or solver failed.
     */
    public Collection<Route> trySolve() throws GraphInfeasibleException {
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
    protected abstract MultiVehicleProblem getInstance();

    /**
     * Actually solves the instance, (first checking for feasibility), returning a Collection of routes.
     *
     * @return The set of routes the solver has concluded is best.
     */
    protected abstract Collection<Route> solve();

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
    public abstract String printCurrentSol() throws IllegalStateException;
}
