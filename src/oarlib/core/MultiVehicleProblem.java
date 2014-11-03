package oarlib.core;

/**
 * Problem abstraction for capacitated problems.  This includes support for number of vehicles, or for vehicle capacity.
 * Currently, this does not include support for heterogeneous fleets.
 * <p/>
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleProblem extends Problem {

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
    protected MultiVehicleProblem(int numVehicles) {
        super("");
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
    protected MultiVehicleProblem(int numVehicles, int capacity) {
        super("");
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
    protected MultiVehicleProblem(int numVehicles, int capacity, String name) {
        super(name);
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

    public abstract CapacitatedObjective getObjectiveType();

    public enum CapacitatedObjective {
        Distance,
        MinMax,
        MaxService
    }
}
