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
package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import gurobi.*;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.SingleVehicleSolver;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class DCPPSolver_Gurobi extends SingleVehicleSolver<DirectedVertex, Arc, DirectedGraph> {

    public DCPPSolver_Gurobi(Problem<DirectedVertex, Arc, DirectedGraph> instance) throws IllegalArgumentException {
        super(instance);
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            DirectedGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isStronglyConnected(mGraph))
                return false;
        }
        return true;
    }

    @Override
    protected Problem getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Tour> solve() {

        /*
         * Solves the IP representing the DCPP, this is a glorified min-cost flow solver.
         *
         * copy - a copy of the graph
         *
         * indexedVertices - the vertex map of copy
         *
         * env - Gurobi var
         * model - Gurobi var
         * expr - Gurobi var
         * varArray - Gurobi var
         *
         * Dplus, Dminus - the set of vertex ids that have excess indegree, and outdegree respectively
         */
        try {
            //copy
            DirectedGraph copy = mInstance.getGraph().getDeepCopy();
            TIntObjectHashMap<DirectedVertex> indexedVertices = copy.getInternalVertexMap();

            //Gurobi stuff
            GRBEnv env = new GRBEnv("miplog.log");
            GRBModel model;
            GRBLinExpr expr;
            GRBVar[][] varArray;
            ArrayList<Integer> Dplus;
            ArrayList<Integer> Dminus;
            int l;
            int m;


            int n = copy.getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            int[][] edgePath = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(copy, dist, path, edgePath);

            //calculate Dplus and Dminus
            Dplus = new ArrayList<Integer>();
            Dminus = new ArrayList<Integer>();
            for (DirectedVertex v : copy.getVertices()) {
                if (v.getDelta() < 0)
                    Dminus.add(v.getId());
                else if (v.getDelta() > 0)
                    Dplus.add(v.getId());
            }

            //Now set up the model in Gurobi and solve it, and see if you get the right answer
            model = new GRBModel(env);

            //create variables
            //after this snippet, element[j][k] contains the variable x_jk which represents the
            //number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
            l = Dplus.size();
            m = Dminus.size();
            varArray = new GRBVar[l][m];
            for (int j = 0; j < l; j++) {
                for (int k = 0; k < m; k++) {
                    varArray[j][k] = model.addVar(0.0, Double.MAX_VALUE, dist[Dplus.get(j)][Dminus.get(k)], GRB.INTEGER, "x" + Dplus.get(j) + Dminus.get(k));
                }
            }

            //update the model with changes
            model.update();

            //create constraints
            for (int j = 0; j < l; j++) {
                expr = new GRBLinExpr();
                //for each j, sum up the x_jk and make sure they take care of all the supply
                for (int k = 0; k < m; k++) {
                    expr.addTerm(1, varArray[j][k]);
                }
                model.addConstr(expr, GRB.EQUAL, indexedVertices.get(Dplus.get(j)).getDelta(), "cj" + j);
            }
            for (int k = 0; k < m; k++) {
                expr = new GRBLinExpr();
                //for each k, sum up the x_jk and make sure they take care of all the demand
                for (int j = 0; j < l; j++) {
                    expr.addTerm(1, varArray[j][k]);
                }
                model.addConstr(expr, GRB.EQUAL, -1 * indexedVertices.get(Dminus.get(k)).getDelta(), "ck" + k);
            }
            model.optimize();

            int temp;
            for (int j = 0; j < l; j++) {
                for (int k = 0; k < m; k++) {
                    temp = (int) varArray[j][k].get(GRB.DoubleAttr.X);
                    if (temp > 0) {
                        for (int i = 0; i < temp; i++)
                            CommonAlgorithms.addShortestPath(copy, dist, path, edgePath, new Pair<Integer>(Dplus.get(j), Dminus.get(k)));
                    }
                }
            }

            //return the answer
            TIntObjectHashMap<Arc> indexedEdges = copy.getInternalEdgeMap();
            ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
            Tour<DirectedVertex, Arc> eulerTour = new Tour<DirectedVertex, Arc>();
            for (int i = 0; i < ans.size(); i++) {
                eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
            }

            HashSet<Tour> ret = new HashSet<Tour>();
            ret.add(eulerTour);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.DIRECTED, ProblemAttributes.Type.CHINESE_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        return "This solver does not support printing.";
    }

    @Override
    public String getSolverName() {
        return "Edmonds' Integer Programming Solver for the Directed Chinese Postman Problem";
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        return new HashMap<String, Double>();
    }

    @Override
    public Solver instantiate(Problem p) {
        return new DCPPSolver_Gurobi(p);
    }

}
