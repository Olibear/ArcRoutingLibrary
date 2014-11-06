package oarlib.test.test.core;

import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
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
import static org.junit.Assert.*;

/**
 * Created by oliverlum on 11/3/14.
 */
public class VertexTestSuite {

    @Test
    public void createDirectedVertex(){

        try {
            DirectedVertex v1 = new DirectedVertex("v1");
            DirectedVertex v2 = new DirectedVertex("v2");

            DirectedGraph test = new DirectedGraph();
            test.addVertex(v1);
            test.addVertex(v2);

            Arc a1 = new Arc("a1", new Pair<DirectedVertex>(v1, v2), 5);
            Arc a2 = new Arc("a2", new Pair<DirectedVertex>(v1, v2), 5);
            Arc a3 = new Arc("a3", new Pair<DirectedVertex>(v1, v2), 5);

            test.addEdge(a1);
            test.addEdge(a2);
            test.addEdge(a3);

            assertEquals("Check in-degree:", 3, v2.getInDegree());
            assertEquals("Check out-degree:", 3, v1.getInDegree());
            assertEquals("Check delta:", 3, v2.getDelta());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Test
    public void createUndirectedVertex(){

        try {
            UndirectedVertex v1 = new UndirectedVertex("v1");
            UndirectedVertex v2 = new UndirectedVertex("v2");

            UndirectedGraph test = new UndirectedGraph();
            test.addVertex(v1);
            test.addVertex(v2);

            Edge a1 = new Edge("a1", new Pair<UndirectedVertex>(v1, v2), 5);
            Edge a2 = new Edge("a2", new Pair<UndirectedVertex>(v1, v2), 5);
            Edge a3 = new Edge("a3", new Pair<UndirectedVertex>(v1, v2), 5);

            test.addEdge(a1);
            test.addEdge(a2);
            test.addEdge(a3);

            assertEquals("Check degree (first endpoint):", 3, v1.getDegree());
            assertEquals("Check degree (second endpoint):", 3, v2.getDegree());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void createMixedVertex(){

        try {
            MixedVertex v1 = new MixedVertex("v1");
            MixedVertex v2 = new MixedVertex("v2");

            MixedGraph test = new MixedGraph();
            test.addVertex(v1);
            test.addVertex(v2);

            MixedEdge a1 = new MixedEdge("a1", new Pair<MixedVertex>(v1, v2), 5, false, false);
            MixedEdge a2 = new MixedEdge("a2", new Pair<MixedVertex>(v1, v2), 5, false, true);
            MixedEdge a3 = new MixedEdge("a3", new Pair<MixedVertex>(v1, v2), 5, true, false);

            test.addEdge(a1);
            test.addEdge(a2);
            test.addEdge(a3);

            assertEquals("Check in-degree:", 1, v2.getInDegree());
            assertEquals("Check out-degree:", 1, v1.getOutDegree());
            assertEquals("Check degree:", 3, v1.getDegree());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void createWindyVertex(){

        try {
            WindyVertex v1 = new WindyVertex("v1");
            WindyVertex v2 = new WindyVertex("v2");

            WindyGraph test = new WindyGraph();
            test.addVertex(v1);
            test.addVertex(v2);

            WindyEdge a1 = new WindyEdge("a1", new Pair<WindyVertex>(v1, v2), 5, 10, false);
            WindyEdge a2 = new WindyEdge("a2", new Pair<WindyVertex>(v1, v2), 5, 10,  true);

            test.addEdge(a1);
            test.addEdge(a2);

            assertEquals("Check degree:", 2, v1.getDegree());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
