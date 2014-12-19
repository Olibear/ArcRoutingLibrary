package core;

import oarlib.graph.graphgen.erdosrenyi.DirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.MixedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.UndirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.WindyErdosRenyiGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for the graph generator classes.
 * <p/>
 * Created by oliverlum on 11/11/14.
 */
public class GraphGeneratorTestSuite {

    @Test
    public void testUndirectedGraphGenerator() {
        UndirectedErdosRenyiGraphGenerator ugg = new UndirectedErdosRenyiGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        UndirectedGraph g = ugg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:", true, CommonAlgorithms.isConnected(g));
        assertEquals("Check n:", 1000, g.getVertices().size());
    }

    @Test
    public void testDirectedGraphGenerator() {
        DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        DirectedGraph g = dgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:", true, CommonAlgorithms.isStronglyConnected(g));
        assertEquals("Check n:", 1000, g.getVertices().size());
    }

    @Test
    public void testMixedGraphGenerator() {
        MixedErdosRenyiGraphGenerator mgg = new MixedErdosRenyiGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        MixedGraph g = mgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:", true, CommonAlgorithms.isStronglyConnected(g));
        assertEquals("Check n:", 1000, g.getVertices().size());
    }

    @Test
    public void testWindyGraphGenerator() {
        WindyErdosRenyiGraphGenerator wgg = new WindyErdosRenyiGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .001.
         */
        WindyGraph g = wgg.generateGraph(1000, 10, true, .001);
        assertEquals("Check connectivity:", true, CommonAlgorithms.isConnected(g));
        assertEquals("Check n:", 1000, g.getVertices().size());
    }
}
