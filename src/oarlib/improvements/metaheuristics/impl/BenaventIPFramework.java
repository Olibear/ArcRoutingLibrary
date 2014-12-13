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
public class BenaventIPFramework extends ImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> mProblem;
    private static final Logger LOGGER = Logger.getLogger(BenaventIPFramework.class);

    public BenaventIPFramework(MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
        mProblem = problem;
    }

    public BenaventIPFramework(MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
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
        Collection<Route<WindyVertex, WindyEdge>> currSol, globalBest, postSimplify;
        Route<WindyVertex, WindyEdge> perturbed;

        int nIter = 2; //num perturbations
        globalBest = initialSol;
        LOGGER.info("Starting obj value: " + oarlib.graph.util.Utils.getObjectiveValue(initialSol, ObjectiveFunction.MAX));

        //apply the intraroute IPs on each of the routes
        LOGGER.debug("IntraRoute IPs");
        Benavent_VND1 vnd1 = new Benavent_VND1(getProblem());
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
        LOGGER.debug("Compare1");
        globalBest = Utils.compareSolutions(currSol, globalBest);
        LOGGER.info("Best obj value set to : " + oarlib.graph.util.Utils.getObjectiveValue(globalBest, ObjectiveFunction.MAX));

        for (int ILS = 1; ILS < nIter; ILS++) {

            //collapse
            LOGGER.debug("Collapse");
            Route<WindyVertex, WindyEdge> collapsed = Utils.aggregateIntoGlobalTour(currSol, getGraph());

            //perturb
            LOGGER.debug("Perturb");
            TwoSwapPerturb perturbation = new TwoSwapPerturb(getProblem());
            perturbed = perturbation.improveRoute(collapsed);

            //resplit
            LOGGER.debug("Resplit");
            MultiVehicleWRPP tempInstance = new MultiVehicleWRPP(getGraph(), mProblem.getmNumVehicles());
            MultiWRPPSolver_Benavent splitter = new MultiWRPPSolver_Benavent(tempInstance);

            ArrayList<Route<WindyVertex, WindyEdge>> container = new ArrayList<Route<WindyVertex, WindyEdge>>();
            Collection<Route<DirectedVertex, Arc>> dirTours = splitter.splitRoute(Utils.convertWindyTourToDirectedTour(perturbed), mProblem.getGraph(), mProblem.getmNumVehicles());
            for (Route<DirectedVertex, Arc> r : dirTours) {
                container.add(oarlib.graph.util.Utils.reclaimTour(r, mProblem.getGraph()));
            }
            LOGGER.info("Perturb obj value: " + oarlib.graph.util.Utils.getObjectiveValue(container, ObjectiveFunction.MAX));

            //apply the intraroute IPs on each of the routes
            LOGGER.debug("IntraRoute IPs 2");
            vnd1 = new Benavent_VND1(getProblem(), container);
            postVND1 = vnd1.improveSolution();
            LOGGER.info("VND1 obj value: " + oarlib.graph.util.Utils.getObjectiveValue(postVND1, ObjectiveFunction.MAX));

            //simplify
            LOGGER.debug("Simplify");
            Simplification simplification = new Simplification(getProblem(), postVND1);
            postSimplify = simplification.improveSolution();
            LOGGER.info("Simplify obj value: " + oarlib.graph.util.Utils.getObjectiveValue(postSimplify, ObjectiveFunction.MAX));

            //run interroute
            LOGGER.debug("InterRoute IPs 2");
            vnd2 = new Benavent_VND2(getProblem(), postSimplify);
            postVND2 = vnd2.improveSolution();
            LOGGER.info("VND2 obj value: " + oarlib.graph.util.Utils.getObjectiveValue(postVND2, ObjectiveFunction.MAX));

            //update global sol
            LOGGER.debug("Compare 2");
            currSol = Utils.compareSolutions(currSol, postVND2);
            LOGGER.info("Curr obj value set to : " + oarlib.graph.util.Utils.getObjectiveValue(currSol, ObjectiveFunction.MAX));

        }

        //update global sol
        LOGGER.debug("Compare 3");
        globalBest = Utils.compareSolutions(currSol, globalBest);
        LOGGER.info("Best obj value set to : " + oarlib.graph.util.Utils.getObjectiveValue(globalBest, ObjectiveFunction.MAX));

        //return best sol
        return globalBest;
    }
}
