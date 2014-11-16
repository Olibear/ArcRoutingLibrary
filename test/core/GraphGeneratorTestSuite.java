package core;

import oarlib.graph.graphgen.DirectedGraphGenerator;
import oarlib.graph.graphgen.MixedGraphGenerator;
import oarlib.graph.graphgen.UndirectedGraphGenerator;
import oarlib.graph.graphgen.WindyGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 11/11/14.
 */
public class GraphGeneratorTestSuite {

    @Test
    public void testUndirectedGraphGenerator() {
        UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        UndirectedGraph g = (UndirectedGraph) ugg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:",true, CommonAlgorithms.isConnected(g));
        assertEquals("Check n:",1000,g.getVertices().size());
    }

    @Test
    public void testDirectedGraphGenerator() {
        DirectedGraphGenerator dgg = new DirectedGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        DirectedGraph g = (DirectedGraph) dgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:",true, CommonAlgorithms.isStronglyConnected(g));
        assertEquals("Check n:",1000,g.getVertices().size());
    }

    @Test
    public void testMixedGraphGenerator() {
        MixedGraphGenerator mgg = new MixedGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        MixedGraph g = (MixedGraph) mgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:",true, CommonAlgorithms.isStronglyConnected(g));
        assertEquals("Check n:",1000,g.getVertices().size());
    }

    @Test
    public void testWindyGraphGenerator() {
        WindyGraphGenerator wgg = new WindyGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        WindyGraph g = (WindyGraph) wgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:",true, CommonAlgorithms.isConnected(g));
        assertEquals("Check n:",1000,g.getVertices().size());
    }
}
