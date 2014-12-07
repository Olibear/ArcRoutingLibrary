package oarlib.improvements.metaheuristics.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementProcedure;
import oarlib.improvements.impl.Benavent_VND1;
import oarlib.improvements.impl.Benavent_VND2;
import oarlib.improvements.impl.Simplification;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 12/3/14.
 */
public class BenaventIPFramework extends ImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    int numVehicles;

    public BenaventIPFramework(WindyGraph windyGraph, Collection<Route<WindyVertex, WindyEdge>> candidateSol) {
        this(windyGraph, candidateSol, 1);
    }

    public BenaventIPFramework(WindyGraph windyGraph, Collection<Route<WindyVertex, WindyEdge>> candidateSol, int numVehicles) {
        super(windyGraph, candidateSol);
        this.numVehicles = numVehicles;
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

        int nsol = 10; //how many solutions to gen
        int nIter = 10; //num perturbations
        globalBest = initialSol;

        for (int MS = 1; MS <= nsol; MS++) {

            //apply the intraroute IPs on each of the routes
            Benavent_VND1 vnd1 = new Benavent_VND1(getGraph(), initialSol);
            Collection<Route<WindyVertex, WindyEdge>> postVND1 = vnd1.improveSolution();

            //apply the interroute IPs
            Benavent_VND2 vnd2 = new Benavent_VND2(getGraph(), postVND1);
            Collection<Route<WindyVertex, WindyEdge>> postVND2 = vnd2.improveSolution();

            //set curr to bestSol
            currSol = postVND2;

            //update global best
            globalBest = Utils.compareSolutions(currSol, globalBest);

            for (int ILS = 1; ILS < nIter; ILS++) {

                //collapse
                Route<WindyVertex, WindyEdge> collapsed = Utils.aggregateIntoGlobalTour(currSol, getGraph());

                //perturb
                /*TwoSwapPerturb perturbation = new TwoSwapPerturb(getGraph(), null);
                perturbed = perturbation.improveRoute(collapsed);

                //resplit

                MultiVehicleWRPP tempInstance = new MultiVehicleWRPP(getGraph(), numVehicles);
                MultiWRPPSolver_Benavent splitter = new MultiWRPPSolver_Benavent(tempInstance);

                ArrayList<Route<WindyVertex, WindyEdge>> container = new ArrayList<Route<WindyVertex, WindyEdge>>();
                splitter.splitRoute();*/


                //apply the intraroute IPs on each of the routes
                vnd1 = new Benavent_VND1(getGraph(), currSol);
                postVND1 = vnd1.improveSolution();

                //simplify
                Simplification simplification = new Simplification(getGraph(), postVND1);
                postSimplify = simplification.improveSolution();

                //run interroute
                vnd2 = new Benavent_VND2(getGraph(), postSimplify);
                postVND2 = vnd2.improveSolution();

                //update global sol
                currSol = Utils.compareSolutions(currSol, postVND2);

            }

            //update global sol
            globalBest = Utils.compareSolutions(currSol, globalBest);

        }

        //return best sol
        return globalBest;
    }
}
