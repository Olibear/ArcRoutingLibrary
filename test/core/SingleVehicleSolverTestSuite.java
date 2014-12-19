package core;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import gurobi.*;
import oarlib.core.Route;
import oarlib.graph.graphgen.erdosrenyi.DirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.UndirectedErdosRenyiGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.problem.impl.cpp.DirectedCPP;
import oarlib.problem.impl.cpp.UndirectedCPP;
import oarlib.solver.impl.DCPPSolver_Edmonds;
import oarlib.solver.impl.UCPPSolver_Edmonds;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;


/**
 * Suite of unit tests to verify the functionality of the single vehicle solvers.
 * <p/>
 * Created by oliverlum on 11/11/14.
 */
public class SingleVehicleSolverTestSuite {

    private static final Logger LOGGER = Logger.getLogger(SingleVehicleSolverTestSuite.class);

    @Test
    public void testUCPPSolver() {
        try {
            UndirectedErdosRenyiGraphGenerator ugg = new UndirectedErdosRenyiGraphGenerator();
            UndirectedGraph g;
            UndirectedGraph g2;
            UndirectedCPP validInstance;
            UCPPSolver_Edmonds validSolver;
            Route validAns;

            //timing stuff
            long start;
            long end;

            //Gurobi stuff
            GRBEnv env = new GRBEnv("miplog.log");
            GRBModel model;
            GRBLinExpr expr;
            GRBVar[][] varArray;
            ArrayList<Integer> oddVertices;

            //the answers
            int l;
            int myCost;
            int trueCost;

            for (int i = 2; i < 150; i += 10) {
                myCost = 0;
                trueCost = 0;
                g = ugg.generateGraph(i, 10, true);
                LOGGER.debug("Generated undirected graph with n = " + i);
                if (CommonAlgorithms.isEulerian(g))
                    continue;
                //copy for Gurobi to work on
                g2 = g.getDeepCopy();
                validInstance = new UndirectedCPP(g);
                validSolver = new UCPPSolver_Edmonds(validInstance);
                start = System.nanoTime();
                validAns = validSolver.trySolve(); //my ans
                end = System.nanoTime();
                LOGGER.debug("It took " + (end - start) / (1e6) + " milliseconds to run our UCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

                myCost += validAns.getCost();

                int n = g2.getVertices().size();
                int[][] dist = new int[n + 1][n + 1];
                int[][] path = new int[n + 1][n + 1];
                CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

                //set up oddVertices
                oddVertices = new ArrayList<Integer>();
                for (UndirectedVertex v : g2.getVertices()) {
                    if (v.getDegree() % 2 == 1)
                        oddVertices.add(v.getId());
                }

                //Now set up the model in Gurobi and solve it, and see if you get the right answer
                model = new GRBModel(env);
                //put in the base cost of all the edges that we'll add to the objective
                for (Edge a : g2.getEdges())
                    trueCost += a.getCost();

                //create variables
                //after this snippet, element[j][k] contains the variable x_jk which represents the
                //number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
                l = oddVertices.size();
                varArray = new GRBVar[l][l];
                for (int j = 0; j < l; j++) {
                    for (int k = 0; k < l; k++) {
                        if (j == k)
                            continue;
                        varArray[j][k] = model.addVar(0.0, 1.0, dist[oddVertices.get(j)][oddVertices.get(k)], GRB.BINARY, "x" + oddVertices.get(j) + oddVertices.get(k));
                    }
                }

                //update the model
                model.update();


                //create constraints
                for (int j = 0; j < l; j++) {
                    expr = new GRBLinExpr();
                    //for each j, sum up the x_jk and make sure they equal 1
                    for (int k = 0; k < l; k++) {
                        if (j == k)
                            continue;
                        expr.addTerm(1, varArray[j][k]);
                    }
                    model.addConstr(expr, GRB.EQUAL, 1, "cj" + j);
                }
                for (int j = 0; j < l; j++) {
                    expr = new GRBLinExpr();
                    //for each k, sum up the x_jk and make sure they equal 1
                    for (int k = 0; k < l; k++) {
                        if (j == k)
                            continue;
                        expr.addTerm(1, varArray[k][j]);
                    }
                    model.addConstr(expr, GRB.EQUAL, 1, "cj" + j);
                }
                for (int j = 0; j < l; j++) {
                    if (j == 0)
                        continue;
                    expr = new GRBLinExpr();
                    //enforce symmetry
                    for (int k = 0; k < j; k++) {
                        expr.addTerm(1, varArray[j][k]);
                        expr.addTerm(-1, varArray[k][j]);
                    }
                    model.addConstr(expr, GRB.EQUAL, 0, "cj" + j);
                }
                model.optimize();
                trueCost += model.get(GRB.DoubleAttr.ObjVal) / 2;
                LOGGER.debug("myCost = " + myCost + ", trueCost = " + trueCost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDCPPSolver() {
        try {
            DirectedGraph g;
            DirectedGraph g2;
            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedCPP validInstance;
            DCPPSolver_Edmonds validSolver;
            Route validAns;

            //timing stuff
            long start;
            long end;

            //Gurobi stuff
            GRBEnv env = new GRBEnv("miplog.log");
            GRBModel model;
            GRBLinExpr expr;
            GRBVar[][] varArray;
            TIntArrayList Dplus;
            TIntArrayList Dminus;
            int l;
            int m;
            int myCost;
            double trueCost;
            for (int i = 2; i < 150; i += 10) {
                myCost = 0;
                trueCost = 0;
                g = dgg.generateGraph(i, 10, true);
                if (CommonAlgorithms.isEulerian(g))
                    continue;
                //copy for gurobi to run on
                g2 = g.getDeepCopy();
                TIntObjectHashMap<DirectedVertex> indexedVertices = g2.getInternalVertexMap();
                LOGGER.debug("Generated directed graph with n = " + i);

                validInstance = new DirectedCPP(g);
                validSolver = new DCPPSolver_Edmonds(validInstance);
                start = System.nanoTime();
                validAns = validSolver.trySolve(); //my ans
                end = System.nanoTime();
                LOGGER.debug("It took " + (end - start) / (1e6) + " milliseconds to run our DCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

                myCost += validAns.getCost();

                int n = g2.getVertices().size();
                int[][] dist = new int[n + 1][n + 1];
                int[][] path = new int[n + 1][n + 1];
                CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

                //calculate Dplus and Dminus
                Dplus = new TIntArrayList();
                Dminus = new TIntArrayList();
                for (DirectedVertex v : g2.getVertices()) {
                    if (v.getDelta() < 0)
                        Dminus.add(v.getId());
                    else if (v.getDelta() > 0)
                        Dplus.add(v.getId());
                }

                //Now set up the model in Gurobi and solve it, and see if you get the right answer
                model = new GRBModel(env);
                //put in the base cost of all the edges that we'll add to the objective
                for (Arc a : g2.getEdges())
                    trueCost += a.getCost();

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
                trueCost += model.get(GRB.DoubleAttr.ObjVal);
                LOGGER.debug("myCost = " + myCost + ", trueCost = " + trueCost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFredericksonMCPPSolver() {

    }

    @Test
    public void testYaoyuenyongMCPPSolver() {

    }

    @Test
    public void testWinWRPPSolver() {

    }

    @Test
    public void testBenaventWRPPSolver() {

    }
}
