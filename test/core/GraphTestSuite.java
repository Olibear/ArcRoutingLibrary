package core;

import oarlib.core.Graph;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.link.impl.MixedEdge;
import oarlib.link.impl.WindyEdge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 11/3/14.
 */
public class GraphTestSuite {

    @Test
    public void createDirectedGraph(){
        try {
            DirectedGraph test = new DirectedGraph(3);
            test.addEdge(1, 2, 3);
            test.addEdge(2, 3, 3);
            test.addEdge(1, 3, 3);

            //check graph properties
            assertEquals("Check n:",3,test.getVertices().size());
            assertEquals("Check m:",3,test.getEdges().size());
            assertEquals("Check type:", Graph.Type.DIRECTED,test.getType());

            //check neighbors
            assertEquals("Check 1's neighbors:",2,test.getInternalVertexMap().get(1).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",1,test.getInternalVertexMap().get(2).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",0,test.getInternalVertexMap().get(3).getNeighbors().keySet().size());

            //clear edges
            test.clearEdges();
            assertEquals("Check m post clear:", 0, test.getEdges().size());

            Arc factoryArc = test.constructEdge(1,2,"label",1);
            test.addEdge(factoryArc);

            assertEquals("Check post arc add m:",1,test.getEdges().size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void createUndirectedGraph(){
        try {
            UndirectedGraph test = new UndirectedGraph(3);
            test.addEdge(1, 2, 3);
            test.addEdge(2, 3, 3);
            test.addEdge(1, 3, 3);

            //check graph properties
            assertEquals("Check n:",3,test.getVertices().size());
            assertEquals("Check m:",3,test.getEdges().size());
            assertEquals("Check type:", Graph.Type.UNDIRECTED,test.getType());

            //check neighbors
            assertEquals("Check 1's neighbors:",2,test.getInternalVertexMap().get(1).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",2,test.getInternalVertexMap().get(2).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",2,test.getInternalVertexMap().get(3).getNeighbors().keySet().size());

            //clear edges
            test.clearEdges();
            assertEquals("Check m post clear:", 0, test.getEdges().size());

            Edge factoryEdge = test.constructEdge(1,2,"label",1);
            test.addEdge(factoryEdge);

            assertEquals("Check post arc add m:",1,test.getEdges().size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void createMixedGraph(){
        try {
            MixedGraph test = new MixedGraph(3);
            test.addEdge(1, 2, "", 3,false);
            test.addEdge(2, 3, "", 3,true);
            test.addEdge(1, 3, "",3,false);

            //check graph properties
            assertEquals("Check n:", 3, test.getVertices().size());
            assertEquals("Check m:",3,test.getEdges().size());
            assertEquals("Check type:", Graph.Type.MIXED,test.getType());

            //check neighbors
            assertEquals("Check 1's neighbors:",2,test.getInternalVertexMap().get(1).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",2,test.getInternalVertexMap().get(2).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",1,test.getInternalVertexMap().get(3).getNeighbors().keySet().size());

            //clear edges
            test.clearEdges();
            assertEquals("Check m post clear:", 0, test.getEdges().size());

            MixedEdge factoryEdge = test.constructEdge(1,2,"label",1, true);
            test.addEdge(factoryEdge);

            assertEquals("Check post arc add m:",1,test.getEdges().size());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void createWindyGraph(){
        try {
            WindyGraph test = new WindyGraph(3);
            test.addEdge(1, 2, "", 3);
            test.addEdge(2, 3, "", 3);
            test.addEdge(1, 3, "",3);

            //check graph properties
            assertEquals("Check n:", 3, test.getVertices().size());
            assertEquals("Check m:",3,test.getEdges().size());
            assertEquals("Check type:", Graph.Type.WINDY,test.getType());

            //check neighbors
            assertEquals("Check 1's neighbors:",2,test.getInternalVertexMap().get(1).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",2,test.getInternalVertexMap().get(2).getNeighbors().keySet().size());
            assertEquals("Check 2's neighbors:",2,test.getInternalVertexMap().get(3).getNeighbors().keySet().size());

            //clear edges
            test.clearEdges();
            assertEquals("Check m post clear:", 0, test.getEdges().size());

            WindyEdge factoryEdge = test.constructEdge(1,2,"label",1);
            test.addEdge(factoryEdge);

            assertEquals("Check post arc add m:",1,test.getEdges().size());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
