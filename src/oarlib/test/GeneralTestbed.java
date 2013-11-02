package oarlib.test;

import java.util.Collection;

import oarlib.core.Arc;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedCPP;
import oarlib.solver.impl.DCPPSolver;
import oarlib.vertex.impl.DirectedVertex;

public class GeneralTestbed {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try 
		{
			DirectedGraph test = new DirectedGraph();

			DirectedVertex v1 = new DirectedVertex("dummy");
			DirectedVertex v2 = new DirectedVertex("dummy2");
			DirectedVertex v3 = new DirectedVertex("dummy3");

			Pair<DirectedVertex> ep = new Pair<DirectedVertex>(v1, v2);
			Pair<DirectedVertex> ep2 = new Pair<DirectedVertex>(v2, v1);
			Pair<DirectedVertex> ep3 = new Pair<DirectedVertex>(v2, v3);
			Pair<DirectedVertex> ep4 = new Pair<DirectedVertex>(v3, v1);

			Arc a = new Arc("stuff", ep, 10);
			Arc a2 = new Arc("more stuff", ep2, 20);
			Arc a3 = new Arc("third stuff", ep3, 5);
			Arc a4 = new Arc("third stuff", ep4, 7);


			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(a);
			test.addEdge(a2);
			test.addEdge(a3);
			test.addEdge(a4);

			check(a);
			DirectedCPP testInstance = new DirectedCPP(test);
			DCPPSolver testSolver = new DCPPSolver(testInstance);
			Collection<Route> testAns = testSolver.trySolve();
			System.out.println("At least got to the end");
		} catch (InvalidEndpointsException e)
		{
			e.printStackTrace();
			System.out.println("We tried to add an edge with endpoints that weren't in the graph yet.");
		}
	}
	private static void check (Link<?> a)
	{
		if (a.getClass() == Arc.class)
			System.out.println("WEEEE");
	}
}
