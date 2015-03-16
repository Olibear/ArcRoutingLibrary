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
package oarlib.improvements.metaheuristics.impl;

import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementProcedure;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.impl.Benavent_VND1;
import oarlib.improvements.impl.Benavent_VND2;
import oarlib.improvements.impl.Simplification;
import oarlib.improvements.perturbation.TwoSwapPerturb;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.multivehicle.MinMaxKWRPP;
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

    private static final Logger LOGGER = Logger.getLogger(BenaventIPFramework.class);

    public BenaventIPFramework(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public BenaventIPFramework(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.MULTI_VEHICLE, null, null);
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        Collection<Route<WindyVertex, WindyEdge>> currSol, globalBest, postSimplify;
        Route<WindyVertex, WindyEdge> perturbed;

        int nIter = 2; //num perturbations
        globalBest = initialSol;
        LOGGER.info("Starting obj value: " + mProblem.getObjectiveFunction().evaluate(initialSol));

        //apply the intraroute IPs on each of the routes
        LOGGER.debug("IntraRoute IPs");
        Benavent_VND1 vnd1 = new Benavent_VND1(getProblem());
        Collection<Route<WindyVertex, WindyEdge>> postVND1 = vnd1.improveSolution();
        LOGGER.info("VND1 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND1));

        //apply the interroute IPs
        LOGGER.debug("InterRoute IPs");
        Benavent_VND2 vnd2 = new Benavent_VND2(getProblem(), postVND1);
        Collection<Route<WindyVertex, WindyEdge>> postVND2 = vnd2.improveSolution();
        LOGGER.info("VND2 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND2));

        //set curr to bestSol
        currSol = postVND2;

        //update global best
        LOGGER.debug("Compare1");
        globalBest = Utils.compareSolutions(currSol, globalBest);
        LOGGER.info("Best obj value set to : " + mProblem.getObjectiveFunction().evaluate(globalBest));

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
            MinMaxKWRPP tempInstance = new MinMaxKWRPP(getGraph(), mProblem.getmNumVehicles());
            MultiWRPPSolver_Benavent splitter = new MultiWRPPSolver_Benavent(tempInstance);

            ArrayList<Route<WindyVertex, WindyEdge>> container = new ArrayList<Route<WindyVertex, WindyEdge>>();
            Collection<Route<DirectedVertex, Arc>> dirTours = splitter.splitRoute(Utils.convertWindyTourToDirectedTour(perturbed), mProblem.getGraph(), mProblem.getmNumVehicles());
            for (Route<DirectedVertex, Arc> r : dirTours) {
                container.add(oarlib.graph.util.Utils.reclaimTour(r, mProblem.getGraph()));
            }
            LOGGER.info("Perturb obj value: " + mProblem.getObjectiveFunction().evaluate(container));

            //apply the intraroute IPs on each of the routes
            LOGGER.debug("IntraRoute IPs 2");
            vnd1 = new Benavent_VND1(getProblem(), container);
            postVND1 = vnd1.improveSolution();
            LOGGER.info("VND1 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND1));

            //simplify
            LOGGER.debug("Simplify");
            Simplification simplification = new Simplification(getProblem(), postVND1);
            postSimplify = simplification.improveSolution();
            LOGGER.info("Simplify obj value: " + mProblem.getObjectiveFunction().evaluate(postSimplify));

            //run interroute
            LOGGER.debug("InterRoute IPs 2");
            vnd2 = new Benavent_VND2(getProblem(), postSimplify);
            postVND2 = vnd2.improveSolution();
            LOGGER.info("VND2 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND2));

            //update global sol
            LOGGER.debug("Compare 2");
            currSol = Utils.compareSolutions(currSol, postVND2);
            LOGGER.info("Curr obj value set to : " + mProblem.getObjectiveFunction().evaluate(currSol));

        }

        //update global sol
        LOGGER.debug("Compare 3");
        globalBest = Utils.compareSolutions(currSol, globalBest);
        LOGGER.info("Best obj value set to : " + mProblem.getObjectiveFunction().evaluate(globalBest));

        //return best sol
        return globalBest;
    }
}
