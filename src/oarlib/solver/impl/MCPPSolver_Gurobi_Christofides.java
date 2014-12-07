package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import gurobi.*;
import oarlib.core.Problem;
import oarlib.core.Problem.Type;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.MixedEdge;
import oarlib.problem.impl.cpp.MixedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.MixedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author oliverlum
 *         <p/>
 *         An IP formulation of the MCPP given by Christofides in 1984.  The variables are as follows:
 *         <p/>
 *         c_ij: the cost of going from vertex i to vertex j (only defiend if there is a link allowing travel from i to j)
 *         x_ij: the number of additional times an arc from vertex i to vertex j is included in the augmentation
 *         y_ij: the number of times an edge from vertex i to vertex j is traversed in the direction from i to j in the augmentation
 *         z_k: an integer variable to ensure that the degree of vertex k is even at the end of the augmentation process
 */
public class MCPPSolver_Gurobi_Christofides extends SingleVehicleSolver {

    MixedCPP mInstance;

    public MCPPSolver_Gurobi_Christofides(MixedCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            MixedGraph mGraph = mInstance.getGraph();
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
    protected Route solve() {
        try {
            //copy
            MixedGraph copy = mInstance.getGraph().getDeepCopy();
            TIntObjectHashMap<MixedVertex> indexedVertices = copy.getInternalVertexMap();
            int n = copy.getVertices().size();
            int arcCost = 0;

            //Gurobi stuff
            GRBEnv env = new GRBEnv("miplog.log");
            env.set(GRB.DoubleParam.MIPGap, 0);
            GRBModel model;
            GRBLinExpr[] symmetryExpr = new GRBLinExpr[n + 1];
            GRBLinExpr[] evenExpr = new GRBLinExpr[n + 1];

            //key = link id, value = var(s)  (for an edge, the first represent endpoints.getFirst() to endpoints.getSecond() )
            HashMap<Integer, GRBVar> xVars = new HashMap<Integer, GRBVar>();
            HashMap<Integer, Pair<GRBVar>> yVars = new HashMap<Integer, Pair<GRBVar>>();
            GRBVar[] zVarArray = new GRBVar[n + 1];

            //Now set up the model in Gurobi and solve it, and see if you get the right answer
            model = new GRBModel(env);

            //create variables and constraints
            int tailId, headId;
            GRBVar temp, tempReverse;
            GRBLinExpr edgeTemp;
            ArrayList<GRBLinExpr> edgeConstraints = new ArrayList<GRBLinExpr>();

            for (int i = 1; i <= n; i++) {
                zVarArray[i] = model.addVar(0.0, Double.MAX_VALUE, 0, GRB.INTEGER, "z_" + i);
                symmetryExpr[i] = new GRBLinExpr();
                evenExpr[i] = new GRBLinExpr();
            }

            for (MixedEdge me : copy.getEdges()) {
                if (me.isDirected()) {
                    tailId = me.getTail().getId();
                    headId = me.getHead().getId();

                    //we're dealing with an x_ij
                    temp = model.addVar(0.0, Double.MAX_VALUE, me.getCost(), GRB.INTEGER, "x_" + tailId + headId);
                    xVars.put(me.getId(), temp);

                    //this appears in 4 constraints, the symmetry and evenness ones for the tail and head
                    symmetryExpr[tailId].addConstant(1);
                    symmetryExpr[tailId].addTerm(1, temp);
                    symmetryExpr[headId].addConstant(-1);
                    symmetryExpr[headId].addTerm(-1, temp);
                    evenExpr[tailId].addTerm(1, temp);
                    evenExpr[headId].addTerm(1, temp);

                    arcCost += me.getCost();
                } else {

                    tailId = me.getEndpoints().getFirst().getId();
                    headId = me.getEndpoints().getSecond().getId();

                    //we're dealing with a y_ij and y_ji
                    temp = model.addVar(0.0, Double.MAX_VALUE, me.getCost(), GRB.INTEGER, "y_" + tailId + headId);
                    tempReverse = model.addVar(0.0, Double.MAX_VALUE, me.getCost(), GRB.INTEGER, "y_" + headId + tailId);
                    yVars.put(me.getId(), new Pair<GRBVar>(temp, tempReverse));

                    //each of these variables appears in 4 constraints, the symmetry and evenness ones for the tail and head
                    symmetryExpr[tailId].addTerm(1, temp);
                    symmetryExpr[tailId].addTerm(-1, tempReverse);
                    symmetryExpr[headId].addTerm(1, tempReverse);
                    symmetryExpr[headId].addTerm(-1, temp);

                    evenExpr[tailId].addTerm(1, temp);
                    evenExpr[tailId].addTerm(1, tempReverse);
                    evenExpr[tailId].addConstant(-1);
                    evenExpr[headId].addTerm(1, temp);
                    evenExpr[headId].addTerm(1, tempReverse);
                    evenExpr[headId].addConstant(-1);

                    //also add the constraint that we need to traverse this edge at least 1 time
                    edgeTemp = new GRBLinExpr();
                    edgeTemp.addTerm(1, temp);
                    edgeTemp.addTerm(1, tempReverse);
                    edgeConstraints.add(edgeTemp);
                }
            }

            //update
            model.update();

            //finalize the constraints
            for (int i = 1; i <= n; i++) {
                model.addConstr(symmetryExpr[i], GRB.EQUAL, 0, "symmetry_" + i);

                evenExpr[i].addTerm(-2, zVarArray[i]);
                if (indexedVertices.get(i).getDegree() % 2 == 1)
                    evenExpr[i].addConstant(-1);
                model.addConstr(evenExpr[i], GRB.EQUAL, 0, "even_" + i);
            }

            for (GRBLinExpr ec : edgeConstraints)
                model.addConstr(ec, GRB.GREATER_EQUAL, 1, "edgeConstraint");


            //optimize
            model.optimize();

            //now create the route

            //return the answer
            ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
            TIntObjectHashMap<MixedEdge> indexedEdges = copy.getInternalEdgeMap();
            Tour eulerTour = new Tour();
            for (int i=0;i<ans.size();i++) {
                eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
            }

            //print the obj value.
            System.out.println(arcCost + model.get(GRB.DoubleAttr.ObjVal));

            return eulerTour;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Type getProblemType() {
        return Problem.Type.MIXED_CHINESE_POSTMAN;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        return "This solver does not support printing.";
    }

    @Override
    public String getSolverName() {
        return "Christofides' Integer Programming Solver for the Mixed Chinese Postman";
    }

}
