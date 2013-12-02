package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.MixedEdge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.MixedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class MCPPSolver extends Solver{

	MixedCPP mInstance;

	public MCPPSolver(MixedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	/**
	 * Implements Frederickson's heuristic for the mixed CPP.
	 */
	@Override
	protected Collection<Route> solve() {
		try {

			MixedGraph copy = mInstance.getGraph(); //starting point for Mixed1
			MixedGraph copy2 = copy.getDeepCopy(); //starting point for Mixed2

			//Vars for bookkeeping
			ArrayList<MixedEdge> U = new ArrayList<MixedEdge>();
			ArrayList<MixedEdge> M = new ArrayList<MixedEdge>();
			ArrayList<Boolean> inMdubPrime =  new ArrayList<Boolean>();

			//Start Mixed 1
			//Even
			CommonAlgorithms.evenDegree(copy);

			//Symmetric
			CommonAlgorithms.inOutDegree(copy, U, M, inMdubPrime);

			//Even
			MixedGraph ans1 = CommonAlgorithms.evenParity(copy, U, M, inMdubPrime);
			//End Mixed 1

			//Start Mixed 2
			U = new ArrayList<MixedEdge>();
			M = new ArrayList<MixedEdge>();
			inMdubPrime =  new ArrayList<Boolean>();
			CommonAlgorithms.inOutDegree(copy2, U, M, inMdubPrime);
			CommonAlgorithms.largeCycles(copy2, U);
			MixedGraph ans2 = new MixedGraph();
			for(int i = 0; i < copy2.getVertices().size(); i++)
			{
				ans2.addVertex(new MixedVertex(""),i);
			}
			HashMap<Integer, MixedVertex> ans2Vertices = ans2.getInternalVertexMap();
			MixedEdge e;
			for(int i = 0;i < M.size(); i++)
			{
				e = M.get(i);
				ans2.addEdge(new MixedEdge("", new Pair<MixedVertex>(ans2Vertices.get(e.getTail().getId()), ans2Vertices.get(e.getHead().getId())),e.getCost(),true ));
			}
			for(int i = 0; i < U.size(); i++)
			{
				e = U.get(i);
				ans2.addEdge(new MixedEdge("", new Pair<MixedVertex>(ans2Vertices.get(e.getEndpoints().getFirst().getId()), ans2Vertices.get(e.getEndpoints().getSecond().getId())), e.getCost(), false));
			}
			//End Mixed 2

			//select the lower cost of the two
			return null;

		} catch(Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public Problem.Type getProblemType() {
		return Problem.Type.MIXED_CHINESE_POSTMAN;
	}

	@Override
	protected MixedCPP getInstance() {
		return mInstance;
	}

}
