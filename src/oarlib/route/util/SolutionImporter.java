package oarlib.route.util;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 5/21/16.
 */
public class SolutionImporter {

    private static Logger LOGGER = Logger.getLogger(RouteExporter.class);

    /**
     * Catch all for importing routes.  To add another format, just add it to the type
     * and then an appropriate clause and method.
     *
     * @param filename - the file containing the route to be imported
     * @param rf       - the format to use
     */
    public static Collection<Tour<DirectedVertex, Arc>> importRoutes(String filename, int depotId, RouteFormat rf) {
        switch (rf) {
            case CORBERAN:
                return importCorberan(filename, depotId);
        }

        LOGGER.error("The format you have passed in appears to be not supported.");
        return null;
    }

    public static <V extends Vertex, E extends Link<V>> HashSet<Tour> mapToGraph(Graph<V, E> g, Collection<Tour<DirectedVertex, Arc>> routes) {
        HashSet<Tour> ans = new HashSet<Tour>();

        for (Route<DirectedVertex, Arc> r : routes) {
            Tour toAdd = new Tour();
            ArrayList<Arc> path = r.getPath();

            for (int index = 0; index < path.size(); index++) {

                Arc a = path.get(index);
                int i = a.getFirstEndpointId();
                int j = a.getSecondEndpointId();

                toAdd.appendEdge(g.findEdges(i, j).get(0), r.getServicingList().get(index));
            }

            ans.add(toAdd);
        }

        return ans;

    }

    private static Collection<Tour<DirectedVertex, Arc>> importCorberan(String filename, int depotId) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            ArrayList<Tour<DirectedVertex, Arc>> ans = new ArrayList<Tour<DirectedVertex, Arc>>();

            //header
            br.readLine();

            //find out how many vertices
            int n = 0;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",|\\(|\\)");
                if (line.charAt(0) == 'y')
                    break;
                int index = Integer.parseInt(split[1]);
                if (index > n)
                    n = index;
                index = Integer.parseInt(split[2].replaceAll("\\s+", ""));
                if (index > n)
                    n = index;
            }

            br.close();
            br = new BufferedReader(new FileReader(filename));
            br.readLine();

            //y's
            HashMap<Pair<Integer>, Integer> service = new HashMap<Pair<Integer>, Integer>();
            while ((line = br.readLine()) != null) {
                if (!(line.charAt(0) == 'y'))
                    continue;
                String[] split = line.split(",|\\(|\\)");

                int i = Integer.parseInt(split[1]);
                int j = Integer.parseInt(split[2].replaceAll("\\s+", ""));
                service.put(new Pair<Integer>(i, j), Character.getNumericValue(line.charAt(1)));
            }


            br.close();
            br = new BufferedReader(new FileReader(filename));
            br.readLine();

            //x's
            int routeNumber = 1;
            DirectedGraph ansGraphs = new DirectedGraph(n);
            while ((line = br.readLine()) != null) {
                if (Character.getNumericValue(line.charAt(1)) != routeNumber) {
                    //get the Euler tour, and add it to the answer
                    ansGraphs.setDepotId(depotId);
                    ArrayList<Integer> route = CommonAlgorithms.tryHierholzer(ansGraphs);
                    //create the route
                    Tour toAdd = new Tour();
                    Pair<Integer> toCheck;
                    Arc a;
                    boolean serve;
                    for (Integer i : route) {
                        a = ansGraphs.getEdge(i);
                        toCheck = new Pair<Integer>(a.getFirstEndpointId(), a.getSecondEndpointId());

                        if (service.containsKey(toCheck) && service.get(toCheck) == routeNumber)
                            serve = true;
                        else
                            serve = false;

                        toAdd.appendEdge(ansGraphs.getEdge(i), serve);
                    }
                    //add it
                    ans.add(toAdd);

                    routeNumber++;
                    ansGraphs = new DirectedGraph(n);
                }
                if (line.startsWith("y"))
                    break;

                String[] split = line.split(",|\\(|\\)");
                int i = Integer.parseInt(split[1]);
                int j = Integer.parseInt(split[2].replaceAll("\\s+", ""));

                int copies = Integer.parseInt(split[3].replaceAll("\\s+|=", ""));

                for (int k = 1; k <= copies; k++) {
                    ansGraphs.addEdge(i, j, 1);
                }
            }

            return ans;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public enum RouteFormat {
        CORBERAN, //Rui's requested output format
    }

}
