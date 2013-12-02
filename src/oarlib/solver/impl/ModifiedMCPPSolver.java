package oarlib.solver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import oarlib.core.MixedEdge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Solver;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.MixedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.MixedVertex;

/**
 * 
 * @author oliverlum
 *
 */
public class ModifiedMCPPSolver extends Solver{

	MixedCPP mInstance;

	public ModifiedMCPPSolver(MixedCPP instance) throws IllegalArgumentException {
		super(instance);
		mInstance = instance;
	}

	/**
	 * Implements Frederickson's heuristic for the mixed CPP, and then eliminates added directed cycles, as 
	 * detailed in Pearn and Chou.
	 */
	@Override
	protected Collection<Route> solve() {
		try {

			MixedGraph ans1 = mInstance.getGraph(); //starting point for Mixed1
			MixedGraph ans2 = ans1.getDeepCopy(); //starting point for Mixed2

			//Vars for bookkeeping
			ArrayList<MixedEdge> U = new ArrayList<MixedEdge>();
			ArrayList<MixedEdge> M = new ArrayList<MixedEdge>();
			ArrayList<Boolean> inMdubPrime =  new ArrayList<Boolean>();

			//Start Mixed 1
			//Even
			CommonAlgorithms.evenDegree(ans1);

			//Symmetric
			CommonAlgorithms.inOutDegree(ans1, U, M, inMdubPrime);

			//Even
			CommonAlgorithms.evenParity(ans1, U, M, inMdubPrime);
			//End Mixed 1
			
			//Start Improvement Phase
			
			//End Improvement Phase

			//Start Mixed 2
			U = new ArrayList<MixedEdge>();
			M = new ArrayList<MixedEdge>();
			inMdubPrime =  new ArrayList<Boolean>();
			CommonAlgorithms.inOutDegree(ans2, U, M, inMdubPrime);
			CommonAlgorithms.largeCycles(ans2, U);
			ans2.clearEdges();
			for(int i = 0;i < M.size(); i++)
			{
				ans2.addEdge(M.get(i));
			}
			for(int i = 0; i < U.size(); i++)
			{
				ans2.addEdge(U.get(i));
			}
			//End Mixed 2
			
			//Start Improvement Phase
			
			//End Improvement Phase

			//select the lower cost of the two
			int cost1 = 0;
			int cost2 = 0;
			for(MixedEdge temp: ans1.getEdges())
			{
				cost1+=temp.getCost();
			}
			for(MixedEdge temp: ans2.getEdges())
			{
				cost2+=temp.getCost();
			}
			ArrayList<Route> ret = new ArrayList<Route>();
			ArrayList<Integer> tour;
			if(cost1 <= cost2)
			{
				tour = CommonAlgorithms.tryHierholzer(ans1);
				Tour eulerTour = new Tour();
				HashMap<Integer, MixedEdge> indexedEdges = ans1.getInternalEdgeMap();
				for (int i=0;i<tour.size();i++)
				{
					eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
				}
				ret.add(eulerTour);
			}
			else
			{
				tour = CommonAlgorithms.tryHierholzer(ans2);
				Tour eulerTour = new Tour();
				HashMap<Integer, MixedEdge> indexedEdges = ans2.getInternalEdgeMap();
				for (int i=0;i<tour.size();i++)
				{
					eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
				}
				ret.add(eulerTour);
			}
			return ret;

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
