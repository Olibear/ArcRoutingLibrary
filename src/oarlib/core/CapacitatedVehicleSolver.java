package oarlib.core;

import oarlib.exceptions.GraphInfeasibleException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Solver abstraction.  Most general contract that Capacitated Solvers must fulfill.
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class CapacitatedVehicleSolver {
    /**
     * Default constructor; must set problem instance.
     * @param instance - instance for which this is a solver
     */
    protected CapacitatedVehicleSolver(CapacitatedProblem instance) throws IllegalArgumentException{
        //make sure I'm a valid problem instance
        if(!(instance.getType() == getProblemType() ))
        {
            throw new IllegalArgumentException("It appears that this problem does not match the problem type handled by this solver.");
        }

    }
    /**
     * Attempts to solve the instance assigned to this problem.
     * @return null if problem instance is not assigned, or solver failed.
     */
    public Collection<Route> trySolve() throws GraphInfeasibleException{
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
    protected  abstract CapacitatedProblem getInstance();    /**
     * Actually solves the instance, (first checking for feasibility), returning a Collection of routes.
     * @return The set of routes the solver has concluded is best.
     */
    protected abstract Collection<Route> solve();
    /**
     * Specifies what type of problem this is a solver for.
     * @return
     */
    public abstract Problem.Type getProblemType();

    /**
     * Runs the vertex-weighted partitioning code from the METIS library, (the gpmetis program) on the graph file provided.
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
            System.out.println("Start");
            Process p = r.exec(args1);
            p.waitFor();
            int exitVal = p.exitValue();
            System.out.println("Stop " + exitVal);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    /**
     * Partitions the graph into the specified number of parts.  This is essentially the setup / wrapper for a
     * runMetis call, and for the naive solvers will precede a call to route().
     */
    protected abstract HashMap<Integer, Integer> partition();

    /**
     * Solves the routing problem over a subgraph, or group of subgraphs.
     */
    protected abstract Route route(HashSet<Integer> ids);
}
