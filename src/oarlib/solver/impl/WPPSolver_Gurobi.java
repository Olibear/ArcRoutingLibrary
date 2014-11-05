package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import gurobi.*;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.link.impl.WindyEdge;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.WindyCPP;
import oarlib.route.impl.Tour;

/**
 * @author oliverlum
 *         <p/>
 *         An IP formulation of the WPP given by Win.  The variables are as follows:
 *         <p/>
 *         c_ij: the cost of going from vertex i to vertex j
 *         x_ij: the number of times an edge is traversed from vertex i to vertex j is included in the augmentation
 */
public class WPPSolver_Gurobi extends SingleVehicleSolver {

    WindyCPP mInstance;

    public WPPSolver_Gurobi(WindyCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            WindyGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }

    @Override
    protected Problem getInstance() {
        return mInstance;
    }

    @Override
    protected Route solve() {
        try {
            //copy
            WindyGraph copy = mInstance.getGraph().getDeepCopy();
            TIntObjectHashMap<WindyEdge> indexedEdges = copy.getInternalEdgeMap();
            int n = copy.getVertices().size();
            int m = copy.getEdges().size();

            //Gurobi stuff
            GRBEnv env = new GRBEnv("miplog.log");
            env.set(GRB.DoubleParam.MIPGap, 0); //insist on optimality
            env.set(GRB.IntParam.OutputFlag, 0);
            GRBModel model;

            //Now set up the model in Gurobi and solve it, and see if you get the right answer
            model = new GRBModel(env);

            //create and add vars
            int i, j;
            GRBVar[][] vars = new GRBVar[n + 1][n + 1];
            WindyEdge tempEdge;
            GRBLinExpr[] edgeConstr = new GRBLinExpr[m];
            GRBLinExpr[] vertexConstr = new GRBLinExpr[n];
            for (int k = 1; k <= m; k++) {
                tempEdge = indexedEdges.get(k);
                i = tempEdge.getEndpoints().getFirst().getId();
                j = tempEdge.getEndpoints().getSecond().getId();
                vars[i][j] = model.addVar(0.0, Double.MAX_VALUE, tempEdge.getCost(), GRB.INTEGER, "x_" + i + j);
                vars[j][i] = model.addVar(0.0, Double.MAX_VALUE, tempEdge.getReverseCost(), GRB.INTEGER, "x_" + j + i);

                //add the edge constraint here
                edgeConstr[k - 1] = new GRBLinExpr();
                edgeConstr[k - 1].addTerm(1, vars[i][j]);
                edgeConstr[k - 1].addTerm(1, vars[j][i]);
            }

            for (int k = 1; k <= n; k++) {
                vertexConstr[k - 1] = new GRBLinExpr();
                for (int l = 1; l <= n; l++) {
                    if (vars[k][l] != null) {
                        vertexConstr[k - 1].addTerm(1, vars[k][l]);
                        vertexConstr[k - 1].addTerm(-1, vars[l][k]);
                    }

                }
            }

            //update
            model.update();

            //finalize the constraints
            for (GRBLinExpr ec : edgeConstr)
                model.addConstr(ec, GRB.GREATER_EQUAL, 1, "edge_constraint");
            for (GRBLinExpr vc : vertexConstr)
                model.addConstr(vc, GRB.EQUAL, 0, "vertex_constraint");


            //optimize
            model.optimize();

            //now create the route


            //return the answer
            //ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
            Tour eulerTour = new Tour();
            //for (int i=0;i<ans.size();i++)
            //{
            //eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
            //}

            //print the obj value.
            System.out.println(model.get(GRB.DoubleAttr.ObjVal));

            return eulerTour;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Type getProblemType() {
        return Problem.Type.WINDY_CHINESE_POSTMAN;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        return "This solver does not support printing.";
    }

}
