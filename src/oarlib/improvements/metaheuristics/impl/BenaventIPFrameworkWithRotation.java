/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
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
 *
 */
package oarlib.improvements.metaheuristics.impl;

import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementProcedure;
import oarlib.improvements.impl.Benavent_VND1;
import oarlib.improvements.impl.Benavent_VND2_Aesthetic;
import oarlib.improvements.perturbation.RouteRotator;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.solver.impl.WRPPSolver_Benavent_H1;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 8/31/16.
 */
public class BenaventIPFrameworkWithRotation extends ImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(BenaventIPFramework.class);

    public BenaventIPFrameworkWithRotation(MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public BenaventIPFrameworkWithRotation(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.MULTI_VEHICLE, null, null);
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        Collection<Route<WindyVertex, WindyEdge>> currSol, globalBest;
        int numIters = 5;

        globalBest = initialSol;
        LOGGER.info("Starting obj value: " + mProblem.getObjectiveFunction().evaluate(initialSol));


        for(int i = 0; i < numIters; i++) {
            //apply the intraroute IPs on each of the routes
            LOGGER.debug("IntraRoute IPs");
            Benavent_VND1 vnd1 = new Benavent_VND1(getProblem(), initialSol);
            Collection<Route<WindyVertex, WindyEdge>> postVND1 = vnd1.improveSolution();
            LOGGER.info("VND1 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND1));

            //apply the interroute IPs
            LOGGER.debug("InterRoute IPs");
            Benavent_VND2_Aesthetic vnd2 = new Benavent_VND2_Aesthetic(getProblem(), postVND1);
            Collection<Route<WindyVertex, WindyEdge>> postVND2 = vnd2.improveSolution();
            LOGGER.info("VND2 obj value: " + mProblem.getObjectiveFunction().evaluate(postVND2));

            //set curr to bestSol
            currSol = postVND2;

            //update global best
            LOGGER.debug("Compare");
            globalBest = Utils.compareSolutions(currSol, globalBest);
            LOGGER.info("Best obj value set to : " + mProblem.getObjectiveFunction().evaluate(globalBest));

            //perturb
            initialSol = RouteRotator.rotateRoutes(getProblem().getGraph(),initialSol,Math.PI/(numIters * 2));
        }

        //return best sol
        return globalBest;
    }

}
