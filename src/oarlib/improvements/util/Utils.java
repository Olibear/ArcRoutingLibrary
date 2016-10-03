package oarlib.improvements.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.link.impl.AsymmetricLink;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by oliverlum on 11/29/14.
 */
public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);
    /**
     * fetches the id of the longest route in the initial solution passed in.
     *
     * @return the id of the longest route in the initial solution provided by this object.
     */
    public static <V extends Vertex, E extends Link<V>> Route<V,E> findLongestRoute(Collection<Route<V,E>> routes){
        int max = Integer.MIN_VALUE;
        Route<V,E> ret = null;
        for(Route<V,E> r: routes) {
            if(r.getCost() > max) {
                max = r.getCost();
                ret = r;
            }
        }
        if(ret == null) {
            LOGGER.debug("We were unable to find a longest route.  This is most likely because the collection apssed in was empty. ");
        }
        return ret;
    }

    /**
     * Takes each of the required (or reverse required) edges in the graph, and assigns a coordinate to it based on the endpoints
     * of the street segment.
     * @param g - street network, (with vertex coordinates assigned)
     * @return - a hashmap with key = link id, value = customer coordinates
     */
    public static <V extends Vertex, E extends Link<V>> HashMap<Integer, Pair<Double>> assignCustomersToCoordinates(Graph<V,E> g) {

        HashMap<Integer, Pair<Double>> ans = new HashMap<Integer, Pair<Double>>();
        for(E e : g.getEdges()) {
            if(e.isRequired() || ((AsymmetricLink)e).isReverseRequired()) {
                ans.put(e.getId(), new Pair<Double>((e.getEndpoints().getFirst().getX() + e.getEndpoints().getSecond().getX())*.5, (e.getEndpoints().getFirst().getY() + e.getEndpoints().getSecond().getY())*.5));
            }
        }
        return ans;
    }

    public static double geoDistance(Pair<Double> p1, Pair<Double> p2){
        return Math.sqrt(Math.pow(p2.getFirst() - p1.getFirst(),2) + Math.pow(p2.getSecond() - p1.getSecond(),2));
    }

    /**
     * Centered at p1.
     * @param p1 - first point
     * @param p2 - second point
     * @return - the angle in radians (using Math.atan) that p2 is if p1 is the reference origin.
     */
    public static double findAngle(Pair<Double> p1, Pair<Double> p2) {
        return Math.atan((p2.getSecond() - p1.getSecond()) / (p2.getFirst() - p1.getFirst()));
    }

    public static Pair<Double> findCenter(ArrayList<Pair<Double>> points) {

        double x,y;
        x = 0.0;
        y = 0.0;
        for(Pair<Double> p : points) {
            x += p.getFirst();
            y += p.getSecond();
        }

        return new Pair<Double>(x/points.size(), y / points.size());
    }

    /**
     * Takes each of the required (or reverse required) edges in the graph, and assigns them to the closest partition's center, for
     * use in the aesthetic solvers.
     * @param partitionCenters - a list of the geographic centers of the partitions
     * @param customers - the list of coordinates of the customers
     * @return - a HashMap, with key = customer id (as given by the key in customers), value = partition id (as index in partitionCenters)
     */
    public static HashMap<Integer, Integer> assignCustomersToPartitions(ArrayList<Pair<Double>> partitionCenters, HashMap<Integer, Pair<Double>> customers){

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();

        int minIndex;
        double minDist;
        double dist;
        double custX, custY, partX, partY;
        for(Integer i : customers.keySet()){
            minDist = Double.MAX_VALUE;
            minIndex = -1;
            custX = customers.get(i).getFirst();
            custY = customers.get(i).getSecond();
            for(int j = 0 ; j < partitionCenters.size() ; j++) {
                partX = partitionCenters.get(j).getFirst();
                partY = partitionCenters.get(j).getSecond();

                dist = Math.sqrt(Math.pow(custX - partX,2) + Math.pow(custY - partY,2));
                if(dist < minDist) {
                    minDist = dist;
                    minIndex = j;
                }
            }

            ans.put(i, minIndex);

        }

        return ans;
    }

    /**
     * Determines which of the two candidate solutions is better, (currently only min-max objective, but will support more in the future)
     * @param sol1 - first candidate solution
     * @param sol2 - second candidate solution
     * @return - the superior candidate solution
     */
    public static <V extends Vertex, E extends Link<V>> Collection<Route<V, E>> compareSolutions(Collection<Route<V,E>> sol1, Collection<Route<V,E>> sol2) {

        int cost1 = findLongestRoute(sol1).getCost();
        int cost2 = findLongestRoute(sol2).getCost();

        if(cost1 < cost2)
            return sol1;
        else
            return sol2;
    }

    /**
     * Collapses a collection of routes into a single global tour by appending and deduping all the compact representations
     * of the routes.  The ordering of the visitation is given by the ordering of the iterator over the collection.
     *
     * @param routes - the routes to be collapsed
     * @return - the global tour
     */
    public static <V extends Vertex, E extends Link<V>, G extends Graph<V,E>> Route<V,E> aggregateIntoGlobalTour(Collection<Route<V,E>> routes, G graph) {
        RouteExpander<G> re = new RouteExpander<G>(graph);
        TIntArrayList flatGlobal = new TIntArrayList();
        ArrayList<Boolean> globalDir = new ArrayList<Boolean>();

        HashSet<Integer> alreadyAdded = new HashSet<Integer>();

        TIntArrayList tempFlat;
        int tempId;
        for(Route<V,E> r: routes) {
            tempFlat = r.getCompactRepresentation();
            int limi = tempFlat.size();
            for(int i = 0; i < limi; i ++) {
                tempId = tempFlat.get(i);
                if(!alreadyAdded.contains(tempId)) {
                    alreadyAdded.add(tempId);
                    flatGlobal.add(tempId);
                    globalDir.add(r.getCompactTraversalDirection().get(i));
                }
            }
        }

        return re.unflattenRoute(flatGlobal, globalDir);
    }

    public static Tour<DirectedVertex, Arc> convertWindyTourToDirectedTour(Route<WindyVertex, WindyEdge> origRoute) {
        int maxId = Integer.MIN_VALUE;
        List<WindyEdge> path = origRoute.getPath();

        int firstId, secondId;
        for(WindyEdge we: path) {
            firstId = we.getEndpoints().getFirst().getId();
            secondId = we.getEndpoints().getSecond().getId();
            if(firstId > maxId){
                maxId = firstId;
            }
            if(secondId > maxId){
                maxId = secondId;
            }
        }

        DirectedGraph ansGraph = new DirectedGraph(maxId);
        Tour<DirectedVertex, Arc> ans = new Tour<DirectedVertex, Arc>();
        ArrayList<Boolean> td = origRoute.getTraversalDirection();
        ArrayList<Boolean> service = origRoute.getServicingList();

        try {
            int limi = path.size();
            WindyEdge temp;
            for (int i = 0; i < limi; i++) {
                temp = path.get(i);
                if (td.get(i)) {
                    ansGraph.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), temp.getCost(), temp.isRequired());
                    ans.appendEdge(ansGraph.getEdge(i + 1), service.get(i));
                } else {
                    ansGraph.addEdge(temp.getEndpoints().getSecond().getId(), temp.getEndpoints().getFirst().getId(), temp.getReverseCost(), temp.isRequired());
                    ans.appendEdge(ansGraph.getEdge(i + 1), service.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ans;
    }

}
