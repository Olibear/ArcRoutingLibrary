package core;

import gnu.trove.TIntIntHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.route.impl.Tour;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 11/29/15.
 */
public class RouteTestSuite {

    private Collection<Route> genTestRoutes(){

        ArrayList<Route> ans = new ArrayList<Route>();
        Graph g = genTestNetwork();
        Tour r1 = new Tour();
        r1.appendEdge(g.getEdge(9));
        r1.appendEdge(g.getEdge(3));
        r1.appendEdge(g.getEdge(6));
        r1.appendEdge(g.getEdge(5));
        r1.appendEdge(g.getEdge(1));
        r1.appendEdge(g.getEdge(8));

        Tour r2 = new Tour();
        r2.appendEdge(g.getEdge(9));
        r2.appendEdge(g.getEdge(10));
        r2.appendEdge(g.getEdge(4));
        r2.appendEdge(g.getEdge(7));
        r2.appendEdge(g.getEdge(6));
        r2.appendEdge(g.getEdge(2));

        ans.add(r1);
        ans.add(r2);

        return ans;
    }

    private Graph genTestNetwork(){
        WindyGraph testNetwork = new WindyGraph(8);

        //set vertex coords
        testNetwork.getVertex(1).setCoordinates(0,1);
        testNetwork.getVertex(2).setCoordinates(1,1);
        testNetwork.getVertex(3).setCoordinates(2,1);
        testNetwork.getVertex(4).setCoordinates(3,1);
        testNetwork.getVertex(5).setCoordinates(0,0);
        testNetwork.getVertex(6).setCoordinates(1,0);
        testNetwork.getVertex(7).setCoordinates(2,0);
        testNetwork.getVertex(8).setCoordinates(3,0);

        //add the edges
        try {
            testNetwork.addEdge(1, 5, 1); //1
            testNetwork.addEdge(2, 6, 1); //2
            testNetwork.addEdge(3, 7, 1); //3
            testNetwork.addEdge(4, 8, 1); //4

            testNetwork.addEdge(1, 2, 1); //5
            testNetwork.addEdge(2, 3, 1); //6
            testNetwork.addEdge(3, 4, 1); //7

            testNetwork.addEdge(5, 6, 1); //8
            testNetwork.addEdge(6, 7, 1); //9
            testNetwork.addEdge(7, 8, 1); //10

        } catch (Exception e) {
            e.printStackTrace();
        }

        testNetwork.setDepotId(6);

        return testNetwork;
    }

    @Test
    public void testTourInit(){

        Tour init = new Tour();

        //check parameters
        assertEquals(init.getCost(), 0);
        assertEquals(init.getReqCost(), 0);
        assertEquals(init.getPath().isEmpty(), true);
        assertEquals(init.getTraversalDirection().isEmpty(), true);
        assertEquals(init.getCompactRepresentation().isEmpty(), true);
        assertEquals(init.getCompactTraversalDirection().isEmpty(), true);

    }

    @Test
    public void testToString(){

        Route route = genTestRoutes().iterator().next();
        assertEquals(route.toString(), "6-7-3-2-1-5-6");
    }

    @Test
    public void testCustomIDMap(){

        Route route = genTestRoutes().iterator().next();
        TIntIntHashMap customIDMap = new TIntIntHashMap();
        customIDMap.put(1,2);
        customIDMap.put(2,3);
        customIDMap.put(3,4);
        customIDMap.put(4,5);
        customIDMap.put(5,6);
        customIDMap.put(6,7);
        customIDMap.put(7,8);

        route.setMapping(customIDMap);
        assertEquals(route.toString(), "7-8-4-3-2-6-7");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testServiceNonReq(){

        Graph g = genTestNetwork();
        g.getEdge(5).setRequired(false);
        Tour t = new Tour();
        Link temp = g.getEdge(5);
        t.appendEdge(g.getEdge(5),true);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSharedEndpoint(){

        Graph g = genTestNetwork();
        Tour t = new Tour();
        t.appendEdge(g.getEdge(1));
        t.appendEdge(g.getEdge(2));

    }

    @Test
    public void testNormalTour(){

        Collection<Route> testRoutes = genTestRoutes();

        //r1
        Route r1 = testRoutes.iterator().next();
        assertEquals(r1.getCompactRepresentation().size(), 6);
        r1.changeService(0);
        assertEquals(r1.getCompactRepresentation().size(), 5);
        assertEquals(r1.getServicingList().get(0), false);
        assertEquals(r1.getReqCost(), 5);
        assertEquals(r1.getCost(), 6);
        r1.changeService(0);
        assertEquals(r1.getCompactRepresentation().size(), 6);
        assertEquals(r1.getReqCost(), 6);
        assertEquals(r1.getCost(), 6);
        assertEquals(r1.getServicingList().get(0), true);
        assertEquals(r1.getServicingList().get(1), true);


    }

    @Test
    public void testDirectedTour(){

    }


}
