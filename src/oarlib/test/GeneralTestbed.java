package oarlib.test;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import gurobi.*;
import java.util.HashMap;
import java.util.HashSet;
import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.graph.graphgen.DirectedGraphGenerator;
import oarlib.graph.graphgen.UndirectedGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.io.Format;
import oarlib.graph.io.GraphReader;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.MSArbor;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.DirectedCPP;
import oarlib.problem.impl.DirectedRPP;
import oarlib.problem.impl.MixedCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.problem.impl.WindyCPP;
import oarlib.problem.impl.WindyRPP;
import oarlib.solver.impl.DCPPSolver_Edmonds;
import oarlib.solver.impl.DRPPSolver_Christofides;
import oarlib.solver.impl.MCPPSolver_Yaoyuenyong;
import oarlib.solver.impl.WPPSolver_Gurobi;
import oarlib.solver.impl.WPPSolver_Gurobi_CuttingPlane;
import oarlib.solver.impl.WRPPSolver_Benavent_H1;
import oarlib.solver.impl.MCPPSolver_Frederickson;
import oarlib.solver.impl.UCPPSolver_Edmonds;
import oarlib.solver.impl.WRPPSolver_Win;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

public class GeneralTestbed {

	/**
	 * The main method.  This class contains a bunch of test / validation methods, and is meant to give examples of
	 * how to use the architecture.  
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//testSimpleGraphReader("/Users/File/Location/Of/A/Graph.txt");
		//testUndirectedGraphGenerator();
		//validateEulerTour();
		//testUCPPSolver();
		//validateUCPPSolver();
		//validateDCPPSolver();
		//testFredericksons("/Users/Username/FolderName");
		//validateMCPPSolver("/Users/Username/FolderName");
		//validateImprovedMCPPSolver("/Users/Username/FolderName");
		//validateWRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
		//validateImprovedWRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
		//testCamposGraphReader();
		//validateSimplifyGraph();
		//testMSArbor();
		//testDRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
		//POMSexample();
	}
	
	@SuppressWarnings("unused")
	private static void POMSexample()
	{
		try
		{
			WindyGraph neighborhood = new WindyGraph(17);
			neighborhood.addEdge(1, 2, 5, 5, false);
			neighborhood.addEdge(2, 3, 4, 4, false);
			neighborhood.addEdge(4, 17, 7, 7, true);
			neighborhood.addEdge(1, 6, 6, 6, true);
			neighborhood.addEdge(2, 8, 5, 5, true);
			neighborhood.addEdge(3, 10, 3, 3, false);
			neighborhood.addEdge(4, 5, 2, 2, true);
			neighborhood.addEdge(5, 6, 3, 3, true);
			neighborhood.addEdge(6, 7, 1, 1, false);
			neighborhood.addEdge(7, 8, 3, 3, true);
			neighborhood.addEdge(8, 9, 2, 2, true);
			neighborhood.addEdge(9, 10, 2, 2, false);
			neighborhood.addEdge(4, 11, 3, 3, false);
			neighborhood.addEdge(7, 11, 5, 5, true);
			neighborhood.addEdge(11, 12, 5, 5, false);
			neighborhood.addEdge(7, 13, 7, 7, false);
			neighborhood.addEdge(8, 14, 8, 8, false);
			neighborhood.addEdge(9, 15, 7, 7, true);
			neighborhood.addEdge(10, 16, 8, 8, true);
			neighborhood.addEdge(12, 13, 4, 4, true);
			neighborhood.addEdge(13, 14, 3, 3, false);
			neighborhood.addEdge(14, 15, 3, 3, true);
			neighborhood.addEdge(15, 16, 2, 2, false);
			
			WindyRPP testProblem = new WindyRPP(neighborhood);
			WRPPSolver_Win testSolver = new WRPPSolver_Win(testProblem);
			Route ans = testSolver.trySolve();
			System.out.println(ans.toString());
			
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	/**
	 * A method to validate our graph simplification in the DRPP Solver.  We construct the example given in the original
	 * paper by Christofides, and ensure that the output matches with that displayed in the figures of the paper.
	 */
	@SuppressWarnings("unused")
	private static void validateSimplifyGraph()
	{
		try
		{
			// Create the test instance
			DirectedGraph test = new DirectedGraph(13);
			test.addEdge(1, 2, "a1", 2, true);
			test.addEdge(4, 5, "a2", 3, true);

			test.addEdge(3, 1, "a3", 5, false);
			test.addEdge(2, 3, "a4", 4, true);
			test.addEdge(2, 13, "a5", 7, false);
			test.addEdge(13, 4, "a6", 4, false);
			test.addEdge(4, 7, "a7", 6, false);
			test.addEdge(6, 4, "a8", 3, true);
			test.addEdge(5, 6, "a9", 5, true);
			test.addEdge(6, 5, "a10", 3, false);

			test.addEdge(3, 8, "a11", 3, false);
			test.addEdge(12, 3, "a12", 7, false);
			test.addEdge(12, 13, "a13", 9, false);
			test.addEdge(6, 7, "a14", 4, true);

			test.addEdge(9, 12, "a15", 5, false);
			test.addEdge(11, 12, "a16", 2, false);
			test.addEdge(7, 11, "a17", 8, false);
			test.addEdge(11, 7, "a18", 3, false);

			test.addEdge(8, 9, "a19", 4, true);
			test.addEdge(9, 10, "a20", 1, true);
			test.addEdge(10, 11, "a21", 6, true);

			test.addEdge(11, 10, "a22", 3, false);
			test.addEdge(10, 9, "a23", 5, true);
			test.addEdge(9, 8, "a24", 3, true);

			// run the solver on it
			DirectedRPP validInstance = new DirectedRPP(test);
			DRPPSolver_Christofides validSolver = new DRPPSolver_Christofides(validInstance);
			validSolver.trySolve();

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A method to test / validate our implementation of Christofides' heuristic for DRPP Solver.
	 */
	@SuppressWarnings("unused")
	private static void testDRPPSolver(String instanceFolder, String outputFile)
	{
		GraphReader gr = new GraphReader(Format.Name.Campos);
		try
		{
			DirectedRPP validInstance;
			DRPPSolver_Christofides validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
			long start;
			long end;

			//run on all the instances in the folder
			for(final File testInstance: testInstanceFolder.listFiles())
			{
				int bestCost = Integer.MAX_VALUE; // the running best cost over running the solver repeatedly on the same instance
				for(int i = 0; i < 1; i++)
				{
					String temp = testInstance.getName();
					System.out.println(temp); // print the file name

					//ensure that the problem is a valid instance
					if(!temp.endsWith(".0") && !temp.endsWith(".1") && !temp.endsWith(".1_3") && !temp.endsWith(".2_3") && !temp.endsWith(".3_3"))
						continue;

					// read the graph
					Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);

					if(g.getClass() == DirectedGraph.class)
					{
						// run it and time it
						DirectedGraph g2 = (DirectedGraph)g;
						validInstance = new DirectedRPP(g2);
						validSolver = new DRPPSolver_Christofides(validInstance);
						start = System.nanoTime();
						validAns = validSolver.trySolve();
						end = System.nanoTime();
						if(validAns.getCost() < bestCost)
							bestCost = validAns.getCost();
						System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our DRPP implementation on a graph with " + g2.getEdges().size() + " edges.");
						System.out.println(validAns.toString());
					}
				}
				if(bestCost == Integer.MAX_VALUE)
					continue;
				System.out.println("bestCost: " + bestCost);
				//pw.println(bestCost + ";");
			}
			pw.close();
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A method to test our JNI Wrapper for MSArbor's minimum spanning arborescence code.
	 * We test on a toy instance, and make sure it's robust to edge removal.
	 */
	@SuppressWarnings("unused")
	private static void testMSArbor()
	{
		try
		{
			// set up the toy instance
			int n = 3;
			int m = 6;
			int[] weights = new int[n * (n-1)];
			weights[0] = 1000; //0-0
			weights[1] = 6; //0-1
			weights[2] = 5; //1-0
			weights[3] = 1000; //1-1
			weights[4] = 2; //2-0
			weights[5] = 10; //2-1

			// run it directly
			int[] ans = MSArbor.msArbor(n, m, weights);

			// run our wrapper that takes our graph structure
			DirectedGraph test = new DirectedGraph();
			test.addVertex(new DirectedVertex("orig"));
			test.addVertex(new DirectedVertex("orig"));
			test.addVertex(new DirectedVertex("orig"));

			test.addEdge(1,2,"orig",6);
			test.addEdge(2,1,"orig",5);
			test.addEdge(3,1,"orig",2);
			test.addEdge(3,2,"orig",3);
			test.addEdge(1,3,"orig",1);
			test.addEdge(2,3,"orig",4);

			HashMap<Integer, Arc> testArcs = test.getInternalEdgeMap();
			test.removeEdge(testArcs.get(3));
			test.removeEdge(testArcs.get(4));
			test.removeEdge(testArcs.get(1));
			test.removeEdge(testArcs.get(6));

			HashSet<Integer> msa = CommonAlgorithms.minSpanningArborescence(test, 2);

		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * An example method which shows how to use our graph generators.
	 */
	@SuppressWarnings("unused")
	private static void testUndirectedGraphGenerator()
	{
		UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
		/**
		 * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
		 * connected, and density roughly .5.
		 */
		UndirectedGraph g = (UndirectedGraph) ugg.generateGraph(1000, 10, true, .5);
	}

	/**
	 * An example method which shows how to use our graph readers.
	 */
	@SuppressWarnings("unused")
	private static void testSimpleGraphReader(String instancePath)
	{
		GraphReader gr = new GraphReader(Format.Name.Simple);
		try 
		{
			Graph<?,?> g = gr.readGraph(instancePath);
			if(g.getClass() == DirectedGraph.class)
			{
				DirectedGraph g2 = (DirectedGraph)g;
			}
			System.out.println("check things");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A method to test our implementation of Frederickson's algorithm on the instances
	 * provided on Angel Corberan's website
	 */
	@SuppressWarnings("unused")
	private static void testFredericksons(String instanceFolder)
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			MixedCPP validInstance;
			MCPPSolver_Frederickson validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			long start;
			long end;

			// run on all instances in the folder
			for(final File testInstance: testInstanceFolder.listFiles())
			{
				String temp = testInstance.getName();
				System.out.println(temp);
				if(temp.equals(".DS_Store")) // ignore mac stuff
					continue;

				Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);
				if(g.getClass() == MixedGraph.class)
				{
					MixedGraph g2 = (MixedGraph)g;
					validInstance = new MixedCPP(g2);
					validSolver = new MCPPSolver_Frederickson(validInstance);
					start = System.nanoTime();
					validAns = validSolver.trySolve(); //my ans
					end = System.nanoTime();
					System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");

				}
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A method to test / validate our implementation of Yaoyuenyong's algorithm on the instances
	 * graciously provided by Yaoyuenyong; the results are compared against those given in
	 * his paper.
	 */
	@SuppressWarnings("unused")
	private static void validateImprovedMCPPSolver(String instanceFolder)
	{
		GraphReader gr = new GraphReader(Format.Name.Yaoyuenyong);
		try
		{
			MixedCPP validInstance;
			MCPPSolver_Yaoyuenyong validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			long start;
			long end;

			//run on all instances in the folder
			for(final File testInstance: testInstanceFolder.listFiles())
			{
				String temp = testInstance.getName();
				System.out.println(temp);
				if(temp.equals(".DS_Store"))
					continue;
				Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);
				if(g.getClass() == MixedGraph.class)
				{
					MixedGraph g2 = (MixedGraph)g;
					validInstance = new MixedCPP(g2);
					validSolver = new MCPPSolver_Yaoyuenyong(validInstance);
					start = System.nanoTime();
					validAns = validSolver.trySolve();
					end = System.nanoTime();
					System.out.println(validAns.getCost());
					System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Yaoyuenyong's implementation on a graph with " + g2.getEdges().size() + " edges.");
					System.out.println(validAns.toString());
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A method to validate our implementation of Frederickson's algorithm on the instances
	 * graciously provided by Yaoyuenyong; the results are compared against those given in
	 * his paper.
	 */
	@SuppressWarnings("unused")
	private static void validateMCPPSolver(String instanceFolder)
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		//GraphReader gr = new GraphReader(Format.Name.Yaoyuenyong);
		try
		{
			MixedCPP validInstance;
			MCPPSolver_Frederickson validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			long start;
			long end;

			for(final File testInstance: testInstanceFolder.listFiles())
			{
				String temp = testInstance.getName();
				System.out.println(temp);
				if(temp.equals(".DS_Store"))
					continue;
				Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);
				int arcCount = 0;
				int edgeCount = 0;
				int oddDegreeCount = 0;
				if(g.getClass() == MixedGraph.class)
				{
					MixedGraph g2 = (MixedGraph)g;
					validInstance = new MixedCPP(g2);
					validSolver = new MCPPSolver_Frederickson(validInstance);
					start = System.nanoTime();
					validAns = validSolver.trySolve();
					end = System.nanoTime();
					System.out.println(validAns.getCost());
					System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");
					System.out.println(validAns.toString());
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A method to validate the WRPP Solver based on instances provided on 
	 * Angel Corberan's website.
	 */
	@SuppressWarnings("unused")
	private static void validateWRPPSolver(String instanceFolder, String outputFile)
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			WindyRPP validInstance;
			WRPPSolver_Win validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
			long start;
			long end;

			// run the solver on the instances in the provided folder
			for(final File testInstance: testInstanceFolder.listFiles())
			{
				int bestCost = Integer.MAX_VALUE;
				for(int i = 0; i < 1; i++)
				{
					String temp = testInstance.getName();
					System.out.println(temp);
					if(!temp.startsWith("A") && !temp.startsWith("M") && !temp.startsWith("m"))
						continue;
					Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);

					if(g.getClass() == WindyGraph.class)
					{
						WindyGraph g2 = (WindyGraph)g;
						validInstance = new WindyRPP(g2);
						validSolver = new WRPPSolver_Win(validInstance);
						start = System.nanoTime();
						validAns = validSolver.trySolve();
						if(validAns.getCost() < bestCost)
							bestCost = validAns.getCost();
						end = System.nanoTime();
						System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our WRPP1 implementation on a graph with " + g2.getEdges().size() + " edges.");
						System.out.println(validAns.toString());
						pw.println((end-start)/(1e6) + "," + g2.getEdges().size() + ";");
					}
				}
				if(bestCost == Integer.MAX_VALUE)
					continue;
				//pw.println(bestCost + ";");
			}
			pw.close();
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A method to validate the improved WRPP Solver based on instances provided on 
	 * Angel Corberan's website.
	 */
	@SuppressWarnings("unused")
	private static void validateImprovedWRPPSolver(String instanceFolder, String outputFile)
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			WindyRPP validInstance;
			WRPPSolver_Benavent_H1 validSolver;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
			long start;
			long end;

			for(final File testInstance: testInstanceFolder.listFiles())
			{
				int bestCost = Integer.MAX_VALUE;
				for(int i = 0; i < 1; i++)
				{
					String temp = testInstance.getName();
					System.out.println(temp);
					if(!temp.startsWith("A") && !temp.startsWith("M") && !temp.startsWith("m"))
						continue;
					Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);

					if(g.getClass() == WindyGraph.class)
					{
						WindyGraph g2 = (WindyGraph)g;
						validInstance = new WindyRPP(g2);
						validSolver = new WRPPSolver_Benavent_H1(validInstance);
						start = System.nanoTime();
						validAns = validSolver.trySolve();
						end = System.nanoTime();
						if(validAns.getCost() < bestCost)
							bestCost = validAns.getCost();
						System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our WRPP1 implementation on a graph with " + g2.getEdges().size() + " edges.");
						System.out.println(validAns.toString());
						pw.println((end-start)/(1e6) + "," + g2.getEdges().size() + ";");
					}
				}
				if(bestCost == Integer.MAX_VALUE)
					continue;
				//pw.println(bestCost + ";");
			}
			pw.close();
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	@SuppressWarnings("unused")
	private static void validateWPPIPSolvers(String instanceFolder, String outputFile)
	{
		GraphReader gr = new GraphReader(Format.Name.Corberan);
		try
		{
			WindyCPP validInstance;
			WPPSolver_Gurobi validSolver;
			WPPSolver_Gurobi_CuttingPlane validSolver2;
			Route validAns;

			File testInstanceFolder = new File(instanceFolder);
			PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
			long start;
			long end;

			int cost = 0;
			int cost2 = 0;

			// run the solver on the instances in the provided folder
			int j = 0;
			for(final File testInstance: testInstanceFolder.listFiles())
			{
				for(int i = 0; i < 1; i++)
				{
					String temp = testInstance.getName();
					System.out.println(temp);
					if(!temp.startsWith("WA") && !temp.startsWith("WB"))
						continue;
					Graph<?,?> g = gr.readGraph(instanceFolder + "/" + temp);

					if(g.getClass() == WindyGraph.class)
					{
						WindyGraph g2 = (WindyGraph)g;
						validInstance = new WindyCPP(g2);
						//validSolver = new WPPSolver_Gurobi(validInstance);
						validSolver2 = new WPPSolver_Gurobi_CuttingPlane(validInstance);

						//exact
						//start = System.nanoTime();
						//validAns = validSolver.trySolve();
						//end = System.nanoTime();

						//pw.print((end-start)/(1e6) + ",");
						//System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our WPP_Gurobi implementation on a graph with " + g2.getEdges().size() + " edges.");


						//heuristic
						start = System.nanoTime();
						validAns = validSolver2.trySolve();
						end = System.nanoTime();

						pw.print((end-start)/(1e6));
						pw.println();
						System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our WPP_Gurobi_CuttingPlane implementation on a graph with " + g2.getEdges().size() + " edges.");
					}
				}

			}
			pw.close();
		} catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
	}

	/**
	 * A method to ensure that our implementation of Hierholzer's algorithm to find an euler tour on
	 * an Eulerian graph.
	 */
	@SuppressWarnings("unused")
	private static void validateEulerTour()
	{
		try{
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
			UndirectedGraph g;
			long startTime;
			long endTime;
			boolean tourOK;
			for(int i=10;i<150;i+=10)
			{
				tourOK = false;
				g = (UndirectedGraph)ugg.generateEulerianGraph(i, 10, true);
				System.out.println("Undirected graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g));
				startTime = System.nanoTime();
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g);
				endTime = System.nanoTime();
				System.out.println("It took " + (endTime-startTime)/(1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g.getEdges().size() + " edges.");

				if(ans.size() != g.getEdges().size())
				{
					System.out.println("tourOK: " + tourOK);
					continue;
				}
				HashSet<Integer> used = new HashSet<Integer>();
				HashMap<Integer, Edge> indexedEdges = g.getInternalEdgeMap();
				Edge curr = null;
				Edge prev = null;
				//make sure it's a real tour
				for(int j = 0; j < ans.size(); j++)
				{
					// can't walk the same edge
					if(used.contains(ans.get(j)))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
					//make sure endpoints match up
					prev = curr;
					curr = indexedEdges.get(ans.get(j));
					if(prev == null)
						continue;
					if(!(prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getFirst().getId() ||
							prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getFirst().getId() ||
							prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getSecond().getId() ||
							prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getSecond().getId()))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
				}
				tourOK = true;
				System.out.println("tourOK: " + tourOK);
			}

			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedGraph g2;
			for(int i=10; i<150; i+=10)
			{
				tourOK = false;
				g2 = (DirectedGraph)dgg.generateEulerianGraph(i, 10, true);
				System.out.println("Directed graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g2));
				startTime = System.nanoTime();
				ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g2);
				endTime = System.nanoTime();
				System.out.println("It took " + (endTime-startTime)/(1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g2.getEdges().size() + " edges.");

				if(ans.size() != g2.getEdges().size())
				{
					System.out.println("tourOK: " + tourOK);
					continue;
				}
				HashSet<Integer> used = new HashSet<Integer>();
				HashMap<Integer, Arc> indexedEdges = g2.getInternalEdgeMap();
				Arc curr = null;
				Arc prev = null;
				//make sure it's a real tour
				for(int j = 0; j < ans.size(); j++)
				{
					// can't walk the same edge
					if(used.contains(ans.get(j)))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
					//make sure endpoints match up
					prev = curr;
					curr = indexedEdges.get(ans.get(j));
					if(prev == null)
						continue;
					if(!(prev.getHead().getId() == curr.getTail().getId()))
					{
						System.out.println("tourOK: " + tourOK);
						break;
					}
				}
				tourOK = true;
				System.out.println("tourOK: " + tourOK);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * A method to compare solutions from our UCPP solver to a gurobi solver.  Note
	 * that in order to use this, you must have a valid gurobi license.
	 */
	@SuppressWarnings("unused")
	private static void validateUCPPSolver()
	{
		try {
			UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
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
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> oddVertices;

			//the answers
			int l;
			int myCost;
			int trueCost;

			for(int i=2;i<150; i+=10)
			{
				myCost = 0;
				trueCost = 0;
				g = (UndirectedGraph)ugg.generateGraph(i, 10, true);
				System.out.println("Generated undirected graph with n = " + i);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				//copy for Gurobi to work on
				g2 = g.getDeepCopy();
				validInstance = new UndirectedCPP(g);
				validSolver = new UCPPSolver_Edmonds(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our UCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

				myCost += validAns.getCost();

				int n = g2.getVertices().size();
				int[][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

				//set up oddVertices
				oddVertices = new ArrayList<Integer>();
				for(UndirectedVertex v: g2.getVertices())
				{
					if(v.getDegree() %2 == 1)
						oddVertices.add(v.getId());
				}

				//Now set up the model in Gurobi and solve it, and see if you get the right answer
				model = new GRBModel(env);
				//put in the base cost of all the edges that we'll add to the objective
				for(Edge a: g2.getEdges())
					trueCost+=a.getCost();

				//create variables
				//after this snippet, element[j][k] contains the variable x_jk which represents the
				//number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
				l = oddVertices.size();
				varArray = new GRBVar[l][l];
				for(int j=0; j<l;j++)
				{
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						varArray[j][k] = model.addVar(0.0,1.0,dist[oddVertices.get(j)][oddVertices.get(k)], GRB.BINARY, "x" + oddVertices.get(j) + oddVertices.get(k));
					}
				}

				//update the model
				model.update();


				//create constraints
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each j, sum up the x_jk and make sure they equal 1
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
				}
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each k, sum up the x_jk and make sure they equal 1
					for(int k=0; k<l; k++)
					{
						if(j==k)
							continue;
						expr.addTerm(1, varArray[k][j]);
					}
					model.addConstr(expr, GRB.EQUAL, 1, "cj"+j);
				}
				for(int j=0; j<l; j++)
				{
					if(j==0)
						continue;
					expr = new GRBLinExpr();
					//enforce symmetry
					for(int k=0; k<j; k++)
					{
						expr.addTerm(1, varArray[j][k]);
						expr.addTerm(-1, varArray[k][j]);
					}
					model.addConstr(expr, GRB.EQUAL, 0, "cj"+j);
				}
				model.optimize();
				trueCost+=model.get(GRB.DoubleAttr.ObjVal)/2;
				System.out.println("myCost = " + myCost + ", trueCost = " + trueCost);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A method to compare solutions from our DCPP solver to a gurobi solver.  Note
	 * that in order to use this, you must have a valid gurobi license.
	 */
	@SuppressWarnings("unused")
	private static void validateDCPPSolver()
	{
		try{
			DirectedGraph g;
			DirectedGraph g2;
			DirectedGraphGenerator dgg = new DirectedGraphGenerator();
			DirectedCPP validInstance;
			DCPPSolver_Edmonds validSolver;
			Route validAns;

			//timing stuff
			long start;
			long end;

			//Gurobi stuff
			GRBEnv env = new GRBEnv("miplog.log");
			GRBModel  model;
			GRBLinExpr expr;
			GRBVar[][] varArray;
			ArrayList<Integer> Dplus;
			ArrayList<Integer> Dminus;
			int l;
			int m;
			int myCost;
			double trueCost;
			for(int i=2;i<150; i+=10)
			{
				myCost=0;
				trueCost=0;
				g = (DirectedGraph)dgg.generateGraph(i, 10, true);
				if(CommonAlgorithms.isEulerian(g))
					continue;
				//copy for gurobi to run on
				g2 = g.getDeepCopy();
				HashMap<Integer, DirectedVertex> indexedVertices = g2.getInternalVertexMap();
				System.out.println("Generated directed graph with n = " + i);

				validInstance = new DirectedCPP(g);
				validSolver = new DCPPSolver_Edmonds(validInstance);
				start = System.nanoTime();
				validAns = validSolver.trySolve(); //my ans
				end = System.nanoTime();
				System.out.println("It took " + (end-start)/(1e6) + " milliseconds to run our DCPP Solver implementation on a graph with " + g.getEdges().size() + " edges.");

				myCost += validAns.getCost();

				int n = g2.getVertices().size();
				int[][] dist = new int[n+1][n+1];
				int[][] path = new int[n+1][n+1];
				CommonAlgorithms.fwLeastCostPaths(g2, dist, path);

				//calculate Dplus and Dminus
				Dplus = new ArrayList<Integer>();
				Dminus = new ArrayList<Integer>();
				for(DirectedVertex v : g2.getVertices())
				{
					if (v.getDelta() < 0)
						Dminus.add(v.getId());
					else if(v.getDelta() > 0)
						Dplus.add(v.getId());
				}

				//Now set up the model in Gurobi and solve it, and see if you get the right answer
				model = new GRBModel(env);
				//put in the base cost of all the edges that we'll add to the objective
				for(Arc a: g2.getEdges())
					trueCost+=a.getCost();

				//create variables
				//after this snippet, element[j][k] contains the variable x_jk which represents the
				//number of paths from vertex Dplus.get(j) to Dminus.get(k) that we add to the graph to make it Eulerian.
				l = Dplus.size();
				m = Dminus.size();
				varArray = new GRBVar[l][m];
				for(int j=0; j<l;j++)
				{
					for(int k=0; k<m; k++)
					{
						varArray[j][k] = model.addVar(0.0,Double.MAX_VALUE,dist[Dplus.get(j)][Dminus.get(k)], GRB.INTEGER, "x" + Dplus.get(j) + Dminus.get(k));
					}
				}

				//update the model with changes
				model.update();

				//create constraints
				for(int j=0; j<l; j++)
				{
					expr = new GRBLinExpr();
					//for each j, sum up the x_jk and make sure they take care of all the supply
					for(int k=0; k<m; k++)
					{
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, indexedVertices.get(Dplus.get(j)).getDelta(), "cj"+j);
				}
				for(int k=0;k<m;k++)
				{
					expr = new GRBLinExpr();
					//for each k, sum up the x_jk and make sure they take care of all the demand
					for(int j=0;j<l;j++)
					{
						expr.addTerm(1, varArray[j][k]);
					}
					model.addConstr(expr, GRB.EQUAL, -1 * indexedVertices.get(Dminus.get(k)).getDelta(), "ck"+k);
				}
				model.optimize();
				trueCost+=model.get(GRB.DoubleAttr.ObjVal);
				System.out.println("myCost = " + myCost + ", trueCost = " + trueCost);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * An example method of how to setup a graph and use a solver.
	 */
	@SuppressWarnings("unused")
	private static void testUCPPSolver()
	{
		try{

			long start = System.currentTimeMillis(); // for timing
			UndirectedGraph test = new UndirectedGraph(); // initialize the graph

			// vertices
			UndirectedVertex v1 = new UndirectedVertex("dummy");
			UndirectedVertex v2 = new UndirectedVertex("dummy2");
			UndirectedVertex v3 = new UndirectedVertex("dummy3");

			// endpoints for the edges
			Pair<UndirectedVertex> ep = new Pair<UndirectedVertex>(v1, v2);
			Pair<UndirectedVertex> ep2 = new Pair<UndirectedVertex>(v2, v1);
			Pair<UndirectedVertex> ep3 = new Pair<UndirectedVertex>(v2, v3);
			Pair<UndirectedVertex> ep4 = new Pair<UndirectedVertex>(v3,v1);

			// initialize the edges
			Edge e = new Edge("stuff", ep, 10);
			Edge e2 = new Edge("more stuff", ep2, 20);
			Edge e3 = new Edge("third stuff", ep3, 5);
			Edge e4 = new Edge("fourth stuff", ep4, 7);

			// add all the elements to the graph
			test.addVertex(v1);
			test.addVertex(v2);
			test.addVertex(v3);
			test.addEdge(e);
			test.addEdge(e2);
			test.addEdge(e3);
			test.addEdge(e4);

			// set up the instance, and solve it
			UndirectedCPP testInstance = new UndirectedCPP(test);
			UCPPSolver_Edmonds testSolver = new UCPPSolver_Edmonds(testInstance);
			Route testAns = testSolver.trySolve();
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
