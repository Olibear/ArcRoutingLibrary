package core;

import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.link.impl.MixedEdge;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for methods internal to our link objects.
 * <p/>
 * Created by oliverlum on 11/3/14.
 */
public class LinkTestSuite {

    @Test
    public void createArc() {

        DirectedVertex v1 = new DirectedVertex("V1");
        DirectedVertex v2 = new DirectedVertex("V2");

        Arc test = new Arc("test", new Pair<DirectedVertex>(v1, v2), 5, false);

        assertEquals("Check label:", "test", test.getLabel());
        assertEquals("Check endpoints:", v1, test.getEndpoints().getFirst());
        assertEquals("Check endpoints:", v2, test.getEndpoints().getSecond());
        assertEquals("Check cost:", 5, test.getCost());
        assertEquals("Check required:", false, test.isRequired());
        assertEquals("Check directed:", true, test.isDirected());
    }

    @Test
    public void createEdge() {

        UndirectedVertex v1 = new UndirectedVertex("V1");
        UndirectedVertex v2 = new UndirectedVertex("V2");

        Edge test = new Edge("test", new Pair<UndirectedVertex>(v1, v2), 5, false);

        assertEquals("Check label:", "test", test.getLabel());
        assertEquals("Check endpoints:", v1, test.getEndpoints().getFirst());
        assertEquals("Check endpoints:", v2, test.getEndpoints().getSecond());
        assertEquals("Check cost:", 5, test.getCost());
        assertEquals("Check required:", false, test.isRequired());
        assertEquals("Check directed:", false, test.isDirected());

    }

    @Test
    public void createMixedEdge() {

        MixedVertex v1 = new MixedVertex("V1");
        MixedVertex v2 = new MixedVertex("V2");

        MixedEdge test = new MixedEdge("test", new Pair<MixedVertex>(v1, v2), 5, true, false);

        assertEquals("Check label:", "test", test.getLabel());
        assertEquals("Check endpoints:", v1, test.getEndpoints().getFirst());
        assertEquals("Check endpoints:", v2, test.getEndpoints().getSecond());
        assertEquals("Check cost:", 5, test.getCost());
        assertEquals("Check required:", false, test.isRequired());
        assertEquals("Check directed:", true, test.isDirected());

    }

    @Test
    public void createWindyEdge() {

        WindyVertex v1 = new WindyVertex("V1");
        WindyVertex v2 = new WindyVertex("V2");

        WindyEdge test = new WindyEdge("test", new Pair<WindyVertex>(v1, v2), 5, 10, false);

        assertEquals("Check label:", "test", test.getLabel());
        assertEquals("Check endpoints:", v1, test.getEndpoints().getFirst());
        assertEquals("Check endpoints:", v2, test.getEndpoints().getSecond());
        assertEquals("Check cost:", 5, test.getCost());
        assertEquals("Check reverse cost:", 10, test.getReverseCost());
        assertEquals("Check required:", false, test.isRequired());
        assertEquals("Check directed:", false, test.isDirected());

    }

}
