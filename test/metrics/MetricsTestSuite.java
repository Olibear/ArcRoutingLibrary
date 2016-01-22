package metrics;

import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.metrics.*;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


/**
 * Created by oliverlum on 11/29/15.
 */
public class MetricsTestSuite {

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
    public void testATD(){
        AverageTraversalMetric atm = new AverageTraversalMetric(genTestNetwork());
        double test = atm.evaluate(genTestRoutes());
        assertEquals(atm.evaluate(genTestRoutes()),2.13,.3);
    }

    @Test
    public void testAvg(){
        AvgMetric am = new AvgMetric();
        double test = am.evaluate(genTestRoutes());
        assertEquals(am.evaluate(genTestRoutes()),6.0,.1);
    }

    @Test
    public void testConvexHull(){
        ConvexHullMetric chm = new ConvexHullMetric();
        double test = chm.evaluate(genTestRoutes());
        assertEquals(chm.evaluate(genTestRoutes()), 0.5, .01);
    }

    @Test
    public void testDepotDistance(){
        DepotDistanceToCenter ddtc = new DepotDistanceToCenter(genTestNetwork());
        double test = ddtc.evaluate(genTestRoutes());
        assertEquals(ddtc.evaluate(genTestRoutes()),Math.sqrt(.5)/Math.sqrt(2.5), .1);
    }

    @Test
    public void testDev(){
        DevMetric dm = new DevMetric();
        double test = dm.evaluate(genTestRoutes());
        assertEquals(dm.evaluate(genTestRoutes()), 0.0,.1);
    }

    @Test
    public void testEdgeCost(){
        EdgeCostMetric ecm = new EdgeCostMetric(genTestNetwork());
        double test = ecm.evaluate(genTestRoutes());
        assertEquals(ecm.evaluate(genTestRoutes()), 10.0,.1);
    }

    @Test
    public void testMax(){
        MaxMetric mm = new MaxMetric();
        double test = mm.evaluate(genTestRoutes());
        assertEquals(mm.evaluate(genTestRoutes()), 6.0,.1);
    }

    @Test
    public void testMin(){
        MinMetric mm = new MinMetric();
        double test = mm.evaluate(genTestRoutes());
        assertEquals(mm.evaluate(genTestRoutes()), 6.0,.1);
    }

    @Test
    public void testNumLinks(){
        NumLinksMetric nlm = new NumLinksMetric(genTestNetwork());
        double test = nlm.evaluate(genTestRoutes());
        assertEquals(nlm.evaluate(genTestRoutes()), 10.0,.1);
    }

    @Test
    public void testNumNodes(){
        NumNodesMetric nnm = new NumNodesMetric(genTestNetwork());
        double test = nnm.evaluate(genTestRoutes());
        assertEquals(nnm.evaluate(genTestRoutes()), 8.0,.1);
    }

    @Test
    public void testRouteOverlap(){
        RouteOverlapMetric rom = new RouteOverlapMetric(genTestNetwork());
        double test = rom.evaluate(genTestRoutes());
        assertEquals(rom.evaluate(genTestRoutes()), 4 / (Math.pow(Math.sqrt(2) + Math.sqrt(8) - 1, 2) - 8),.1);
    }

    @Test
    public void testSum(){
        SumMetric sm = new SumMetric();
        double test = sm.evaluate(genTestRoutes());
        assertEquals(sm.evaluate(genTestRoutes()), 12.0,.1);
    }

    @Test
    public void testVar(){
        VarMetric vm = new VarMetric();
        double test = vm.evaluate(genTestRoutes());
        assertEquals(vm.evaluate(genTestRoutes()), 0.0,.1);

    }
}
