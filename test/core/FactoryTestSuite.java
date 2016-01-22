package core;

import oarlib.core.Graph;
import oarlib.graph.factory.impl.*;
import oarlib.graph.impl.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 11/29/15.
 */
public class FactoryTestSuite {

    @Test
    public void testDirectedGraphFactory() {

        DirectedGraphFactory dgf = new DirectedGraphFactory();
        DirectedGraph dg = dgf.instantiate();

        assert(dg instanceof DirectedGraph);
        assertEquals(dg.getVertices().size(),0);
        assertEquals(dg.getEdges().size(),0);

    }

    @Test
    public void testUndirectedGraphFactory() {

        UndirectedGraphFactory ugf = new UndirectedGraphFactory();
        UndirectedGraph ug = ugf.instantiate();

        assert(ug instanceof UndirectedGraph);
        assertEquals(ug.getVertices().size(),0);
        assertEquals(ug.getEdges().size(),0);

    }

    @Test
    public void testMixedGraphFactory() {

        MixedGraphFactory mgf = new MixedGraphFactory();
        MixedGraph mg = mgf.instantiate();

        assert(mg instanceof MixedGraph);
        assertEquals(mg.getVertices().size(),0);
        assertEquals(mg.getEdges().size(),0);

    }

    @Test
    public void testWindyGraphFactory() {

        WindyGraphFactory wgf = new WindyGraphFactory();
        WindyGraph wg = wgf.instantiate();

        assert(wg instanceof WindyGraph);
        assertEquals(wg.getVertices().size(),0);
        assertEquals(wg.getEdges().size(),0);

    }

    @Test
    public void testZigZagGraphFactory() {

        ZigZagGraphFactory zzgf = new ZigZagGraphFactory();
        ZigZagGraph zzg = zzgf.instantiate();

        assert(zzg instanceof ZigZagGraph);
        assertEquals(zzg.getVertices().size(),0);
        assertEquals(zzg.getEdges().size(),0);

    }

}
