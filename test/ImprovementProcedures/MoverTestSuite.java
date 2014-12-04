package ImprovementProcedures;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.util.CompactMove;
import oarlib.improvements.util.Mover;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 12/2/14.
 */
public class MoverTestSuite {

    @Test
    public void testMoverEvalCost() {

        BasicConfigurator.configure();

        try {
            WindyGraph testGraph = new WindyGraph(5);//v graph
            testGraph.addEdge(1, 2, 1, 2, true);
            testGraph.addEdge(2, 3, 2, 3, true);

            testGraph.addEdge(1, 4, 1, 4, false);
            testGraph.addEdge(4, 5, 4, 5, true);

            Tour one = new Tour();
            one.appendEdge(testGraph.getEdge(1));
            one.appendEdge(testGraph.getEdge(2));
            one.appendEdge(testGraph.getEdge(2));
            one.appendEdge(testGraph.getEdge(1));

            Tour two = new Tour();
            two.appendEdge(testGraph.getEdge(3));
            two.appendEdge(testGraph.getEdge(4));
            two.appendEdge(testGraph.getEdge(4));
            two.appendEdge(testGraph.getEdge(3));

            ArrayList<Route<WindyVertex, WindyEdge>> testAns = new ArrayList<Route<WindyVertex, WindyEdge>>();
            testAns.add(one);
            testAns.add(two);

            Mover<WindyVertex, WindyEdge, WindyGraph> testMover = new Mover<WindyVertex, WindyEdge, WindyGraph>(testGraph);
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moves = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
            CompactMove<WindyVertex, WindyEdge> testMove = new CompactMove<WindyVertex, WindyEdge>(one, two, 1, 0);
            moves.add(testMove);

            int savings = testMover.evalComplexMove(moves, testAns);

            System.out.println(savings);

            assertEquals("Expected savings:", 8, savings);

        } catch (Exception e) {
            e.printStackTrace();
            assertEquals("Error.", true, false);
        }

    }

    @Test
    public void testMakeMove() {


        BasicConfigurator.configure();

        try {
            WindyGraph testGraph = new WindyGraph(5);//v graph
            testGraph.addEdge(1, 2, 1, 2, true);
            testGraph.addEdge(2, 3, 2, 3, true);

            testGraph.addEdge(1, 4, 1, 4, true);
            testGraph.addEdge(4, 5, 4, 5, true);

            Tour one = new Tour();
            one.appendEdge(testGraph.getEdge(1));
            one.appendEdge(testGraph.getEdge(2));
            one.appendEdge(testGraph.getEdge(2));
            one.appendEdge(testGraph.getEdge(1));

            Tour two = new Tour();
            two.appendEdge(testGraph.getEdge(3));
            two.appendEdge(testGraph.getEdge(4));
            two.appendEdge(testGraph.getEdge(4));
            two.appendEdge(testGraph.getEdge(3));

            ArrayList<Route<WindyVertex, WindyEdge>> testAns = new ArrayList<Route<WindyVertex, WindyEdge>>();
            testAns.add(one);
            testAns.add(two);

            Mover<WindyVertex, WindyEdge, WindyGraph> testMover = new Mover<WindyVertex, WindyEdge, WindyGraph>(testGraph);
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moves = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
            CompactMove<WindyVertex, WindyEdge> testMove = new CompactMove<WindyVertex, WindyEdge>(one, two, 1, 1);
            moves.add(testMove);

            TIntObjectHashMap<Route<WindyVertex, WindyEdge>> changedRoutes = testMover.makeComplexMove(moves);

            System.out.println("One: " + one.toString());
            System.out.println("Two: " + two.toString());

            int maxCost = 0;
            for (int i : changedRoutes.keys()) {
                System.out.println(changedRoutes.get(i));
                if (changedRoutes.get(i).getCost() > maxCost)
                    maxCost = changedRoutes.get(i).getCost();
            }

            assertEquals("Expected max cost:", 27, maxCost);
        } catch (Exception e) {
            e.printStackTrace();
            assertEquals("Error.", true, false);
        }

    }

    @Test
    public void testCostParity() {

    }
}
