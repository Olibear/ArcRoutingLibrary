package route.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.Utils;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.util.RouteExpander;
import oarlib.solver.impl.WRPPSolver_Benavent_H1;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test suite to test operations over alternate representations of routes, (e.g. the compact
 * representation suggested by Benavent et al. (2005) New Heuristic Algorithms For the Windy
 * Rural Postman Problem.
 * <p/>
 * Created by oliverlum on 11/22/14.
 */
public class AltRepresentationTests {

    private static final Logger LOGGER = Logger.getLogger(AltRepresentationTests.class);

    @Test
    public void testRouteFlattenerExpander() {

        BasicConfigurator.configure();

        try {

            WindyGraph testGraph = new WindyGraph(5);
            testGraph.addEdge(1, 2, 3, 4, true);
            testGraph.addEdge(2, 3, 5, 2, false);
            testGraph.addEdge(3, 4, 1, 7, false);
            testGraph.addEdge(4, 5, 1, 1, true);
            testGraph.addEdge(1, 3, 5, 2, true);

            WindyRPP testProb = new WindyRPP(testGraph, "test instance");
            WRPPSolver_Benavent_H1 testSolver = new WRPPSolver_Benavent_H1(testProb, false);
            RouteExpander wre = new RouteExpander(testGraph);

            Route testAns = Utils.reclaimTour(testSolver.trySolve().iterator().next(), testGraph);
            LOGGER.debug("Test ans:" + testAns.toString());
            LOGGER.debug("Test cost: " + testAns.getCost());
            TIntArrayList flattenedRoute = testAns.getCompactRepresentation();
            LOGGER.debug("Flattened Rep: " + flattenedRoute.toString());
            Route unflattened = wre.unflattenRoute(flattenedRoute, testAns.getCompactTraversalDirection());
            LOGGER.debug("Unflattened Route: " + unflattened.toString());
            LOGGER.debug("Unflattened cost: " + unflattened.getCost());

            assertEquals("Costs equal.", testAns.getCost(), unflattened.getCost());


        } catch (Exception e) {
            e.printStackTrace();
            assertEquals("Test failed.", false, true);
        }

    }
}
