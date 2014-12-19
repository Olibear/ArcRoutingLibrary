package oarlib.improvements.metaheuristics.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementProcedure;
import oarlib.improvements.impl.Benavent_VND1;
import oarlib.improvements.impl.Benavent_VND2;
import oarlib.improvements.impl.Simplification;
import oarlib.improvements.perturbation.TwoSwapPerturb;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.objfunc.ObjectiveFunction;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.problem.impl.multivehicle.MultiVehicleWRPP;
import oarlib.solver.impl.MultiWRPPSolver_Benavent;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 12/3/14.
 */
public class OnePassBenaventIPFramework extends ImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> mProblem;
    private static final Logger LOGGER = Logger.getLogger(BenaventIPFramework.class);

    public OnePassBenaventIPFramework(MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
        mProblem = problem;
    }

    public OnePassBenaventIPFramework(MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, initialSol);
        mProblem = problem;
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        Collection<Route<WindyVertex, WindyEdge>> currSol, globalBest;

        int nIter = 2; //num perturbations
        globalBest = initialSol;
        LOGGER.info("Starting obj value: " + oarlib.graph.util.Utils.getObjectiveValue(initialSol, ObjectiveFunction.MAX));

        //apply the intraroute IPs on each of the routes
        LOGGER.debug("IntraRoute IPs");
        Benavent_VND1 vnd1 = new Benavent_VND1(getProblem(), initialSol);
        Collection<Route<WindyVertex, WindyEdge>> postVND1 = vnd1.improveSolution();
        LOGGER.info("VND1 obj value: " + oarlib.graph.util.Utils.getObjectiveValue(postVND1, ObjectiveFunction.MAX));

        //apply the interroute IPs
        LOGGER.debug("InterRoute IPs");
        Benavent_VND2 vnd2 = new Benavent_VND2(getProblem(), postVND1);
        Collection<Route<WindyVertex, WindyEdge>> postVND2 = vnd2.improveSolution();
        LOGGER.info("VND2 obj value: " + oarlib.graph.util.Utils.getObjectiveValue(postVND2, ObjectiveFunction.MAX));

        //set curr to bestSol
        currSol = postVND2;

        //update global best
        LOGGER.debug("Compare");
        globalBest = Utils.compareSolutions(currSol, globalBest);
        LOGGER.info("Best obj value set to : " + oarlib.graph.util.Utils.getObjectiveValue(globalBest, ObjectiveFunction.MAX));

        //return best sol
        return globalBest;
    }
}
