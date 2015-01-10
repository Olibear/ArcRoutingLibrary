package oarlib.problem.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import oarlib.objfunc.ObjectiveFunction;

/**
 * Problem abstraction for capacitated problems.  This includes support for number of vehicles, or for vehicle capacity.
 * Currently, this does not include support for heterogeneous fleets.
 * <p/>
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleProblem<V extends Vertex, E extends Link<V>, G extends Graph<V,E>> extends Problem<V,E,G> {

    int mNumVehicles;
    int mCapacity;
    boolean capSet;
    boolean numVehiclesSet;

    /**
     * Default constructor for a Capacitated Problem, this assumes there is no max capacity, but rather a constraint
     * on the number of vehicles used.
     *
     * @param numVehicles - the number of routes allowed to exist in the final solution.
     */
    protected MultiVehicleProblem(G graph, int numVehicles, ObjectiveFunction objFunc) {
        super(graph, "", objFunc);
        mNumVehicles = numVehicles;
        numVehiclesSet = true;
        capSet = false;
    }

    /**
     * Constructor for a Capacitated Problem, this enforces both the max number of vehicles used in the solution
     * as well as the maximum capacity of a route.  It is possible that this not both can be respected, in which
     * case the solution returned will be infeasible in an unpredictable way.
     * If you only wish to enforce capacity, pass in a value <= 0 for numVehicles.
     *
     * @param numVehicles - the number of routes allowed to exist in the final solution.
     * @param capacity    - the max capacity that a route in the solution is allowed to have
     */
    protected MultiVehicleProblem(G graph, int numVehicles, int capacity, ObjectiveFunction objFunc) {
        super(graph, "", objFunc);
        mCapacity = capacity;
        capSet = true;
        if (numVehicles > 0) {
            mNumVehicles = numVehicles;
            numVehiclesSet = true;
        } else
            numVehiclesSet = false;
    }


    /**
     * Constructor for a Capacitated Problem, this enforces both the max number of vehicles used in the solution
     * as well as the maximum capacity of a route.  It is possible that this not both can be respected, in which
     * case the solution returned will be infeasible in an unpredictable way.
     * If you only wish to enforce capacity, pass in a value <= 0 for numVehicles.
     *
     * @param numVehicles - the number of routes allowed to exist in the final solution.
     * @param capacity    - the max capacity that a route in the solution is allowed to have
     * @param name        - the instance name
     */
    protected MultiVehicleProblem(G graph, int numVehicles, int capacity, String name, ObjectiveFunction objFunc) {
        super(graph, name, objFunc);
        mCapacity = capacity;
        capSet = true;
        if (numVehicles > 0) {
            mNumVehicles = numVehicles;
            numVehiclesSet = true;
        } else
            numVehiclesSet = false;
    }

    /**
     * Get the max capacity that a route in a feasible solution to this problem is allowed to have.
     * If capacity is not being enforced, -1 is returned.
     *
     * @return - the capacity bound on a route, or -1 if this constraint is no limit set.
     */
    public int getCapacity() {
        if (!capSet)
            return -1;
        return mCapacity;
    }

    /**
     * Get the number of vehicles that a feasible solution to this problem is allowed to have.
     * If number of vehicles is not being enforced, -1 is returned.
     *
     * @return - the max number of routes allowed to exist in a feasible solution, or -1 if there is
     * no limit set.
     */
    public int getmNumVehicles() {
        if (!numVehiclesSet)
            return -1;
        return mNumVehicles;
    }
}
