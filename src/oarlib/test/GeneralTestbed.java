package arl.test;

import arl.core.Arc;
import arl.core.Link;
import arl.graph.util.Pair;
import arl.vertex.impl.DirectedVertex;

public class GeneralTestbed {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DirectedVertex v1 = new DirectedVertex("dummy");
		DirectedVertex v2 = new DirectedVertex("dummy2");
		
		Pair<DirectedVertex> ep = new Pair<DirectedVertex>(v1, v2);
		
		Arc a = new Arc("stuff", ep, 10);
		check(a);
	}
	private static void check (Link<?> a)
	{
		if (a.getClass() == Arc.class)
			System.out.println("WEEEE");
	}
}
