package oarlib.test;

import java.util.Collection;
import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.Format;
import oarlib.graph.io.GraphReader;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.solver.impl.DCPPSolver;
import oarlib.solver.impl.UCPPSolver;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class GeneralTestbed {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
	}
	private static void check (Link<?> a)
	{
		if (a.getClass() == Arc.class)
			System.out.println("WEEEE");
	}
	private static void testGraphReader()
	{
		GraphReader gr = new GraphReader(Format.Name.DIMACS_Modified);
		try 
		{
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testUCPPSolver()
	{
		try{
			long start = System.currentTimeMillis();
			UndirectedGraph test = new UndirectedGraph();
			
			UndirectedVertex v1 = new UndirectedVertex("dummy");
			UndirectedVertex v2 = new UndirectedVertex("dummy2");
			UndirectedVertex v3 = new UndirectedVertex("dummy3");
			
			Pair<UndirectedVertex> ep = new Pair<UndirectedVertex>(v1, v2);
			Pair<UndirectedVertex> ep2 = new Pair<UndirectedVertex>(v2, v1);
			Pair<UndirectedVertex> ep3 = new Pair<UndirectedVertex>(v2, v3);
			Pair<UndirectedVertex> ep4 = new Pair<UndirectedVertex>(v3,v1);
			
			Edge e = new Edge("stuff", ep, 10);
			Edge e2 = new Edge("more stuff", ep2, 20);
			Edge e3 = new Edge("third stuff", ep3, 5);
			Edge e4 = new Edge("fourth stuff", ep4, 7);
			
			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(e);
			test.addEdge(e2);
			test.addEdge(e3);
			test.addEdge(e4);
			
			UndirectedCPP testInstance = new UndirectedCPP(test);
			UCPPSolver testSolver = new UCPPSolver(testInstance);
			Collection<Route> testAns = testSolver.trySolve();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static void testDCPPSolver()
	{
		try {
			long start = System.currentTimeMillis();
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
			Arc a4 = new Arc("fourth stuff", ep4, 7);
			Arc a5 = new Arc("fifth stuff", ep3,  8);


			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(a);
			test.addEdge(a2);
			test.addEdge(a3);
			test.addEdge(a4);
			test.addEdge(a5);

			DirectedCPP testInstance = new DirectedCPP(test);
			DCPPSolver testSolver = new DCPPSolver(testInstance);
			Collection<Route> testAns = testSolver.trySolve();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
