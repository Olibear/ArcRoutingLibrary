package oarlib.graph.graphgen;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.graphgen.Util.BoundingBox;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


/**
 * Class for querying the osm database to create a real street network based on a specified geographical bounding box.
 * <p/>
 * <p/>
 * Created by oliverlum on 10/5/14.
 */
public class OSM_Fetcher {

    private static final String[] baseURLS = new String[]{"http://overpass.osm.rambler.ru/cgi/xapi_meta?way", "http://www.overpass-api.de/api/xapi_meta?way", "http://api.openstreetmap.fr/xapi?way"};
    private BoundingBox mBox;
    private boolean needToGen;
    private WindyGraph mGraph;

    public OSM_Fetcher(BoundingBox box) {
        mBox = box;
        mGraph = null;
        needToGen = true;
    }

    public WindyGraph queryForGraph() {

        if (!needToGen)
            return mGraph;

        WindyGraph ans = new WindyGraph();

        for (String base : baseURLS) {
            String requestStr = base;
            requestStr += "[bbox=" + mBox.getMinLon() + "," + mBox.getMinLat() + "," + mBox.getMaxLon() + "," + mBox.getMaxLat() + "][highway=*]";
            URL request;
            try {
                request = new URL(requestStr);
                HttpURLConnection conn = (HttpURLConnection) request.openConnection();

                //parse the DOM
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document doc = builder.parse(conn.getInputStream());

                NodeList nodeList = doc.getDocumentElement().getChildNodes();

                int counter = 1;
                HashMap<String, Integer> refIds = new HashMap<String, Integer>(); //key = nodeId, value = internalId
                TIntObjectHashMap<WindyVertex> ansVertices = ans.getInternalVertexMap();
                NodeList subNodeList;
                Node curr, prev;
                int to, from, cost, reverseCost;
                double perturb;
                Random rng = new Random(1000);

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node instanceof Element) {

                        //if it's a node, add a vertex with matchId = refId
                        if (((Element) node).getTagName() == "node") {
                            WindyVertex toAdd = new WindyVertex("from OSM");
                            toAdd.setCoordinates(Double.parseDouble(((Element) node).getAttribute("lon")), Double.parseDouble(((Element) node).getAttribute("lat")));
                            ans.addVertex(toAdd);
                            refIds.put(((Element) node).getAttribute("id"), counter);
                            counter++;
                        }

                        //if it's a way, then add the appropriate connections
                        else if (((Element) node).getTagName() == "way") {
                            subNodeList = ((Element) node).getElementsByTagName("nd");
                            for (int j = 1; j < subNodeList.getLength(); j++) {
                                curr = subNodeList.item(j);
                                prev = subNodeList.item(j - 1);

                                if (curr instanceof Element && prev instanceof Element) {
                                    from = refIds.get(((Element) prev).getAttribute("ref"));
                                    to = refIds.get(((Element) curr).getAttribute("ref"));
                                    cost = latLonToMeters(ansVertices.get(from).getY(), ansVertices.get(from).getX(), ansVertices.get(to).getY(), ansVertices.get(to).getX()) + 1; //don't want 0 cost edges

                                    perturb = (rng.nextInt(10) - 5) / 100.0;
                                    reverseCost = (int) (cost * (1 + perturb));

                                    ans.addEdge(from, to, cost, reverseCost);
                                }
                            }
                        }
                    }
                }


                /*
                 * repair the graph in the following way:
                 * if it's not connected, calculate the connected components, and then just take the largest one.
                 */
                int n = ans.getVertices().size();
                int m = ans.getEdges().size();
                int[] nodei = new int[m + 1];
                int[] nodej = new int[m + 1];
                int[] component = new int[n + 1];

                //set up the graph
                int i = 1;
                for (WindyEdge we : ans.getEdges()) {
                    nodei[i] = we.getEndpoints().getFirst().getId();
                    nodej[i] = we.getEndpoints().getSecond().getId();
                    i++;
                }


                CommonAlgorithms.connectedComponents(n, m, nodei, nodej, component);

                ArrayList<HashSet<Integer>> orgComponents = new ArrayList<HashSet<Integer>>();
                for (int j = 0; j <= component[0]; j++) {
                    orgComponents.add(new HashSet<Integer>());
                }
                for (int j = 1; j < component.length; j++) {
                    orgComponents.get(component[j]).add(j);
                }

                int maxPartitionSize = 0;
                int maxPartition = 0;
                for (int j = 1; j < orgComponents.size(); j++) {
                    if (orgComponents.get(j).size() > maxPartitionSize) {
                        maxPartitionSize = orgComponents.get(j).size();
                        maxPartition = j;
                    }
                }

                HashSet<Integer> maxPart = orgComponents.get(maxPartition);
                if (component[0] > 1) {

                    //new, reduced ans graph
                    WindyGraph trueAns = new WindyGraph();

                    i = 1;
                    WindyVertex tempVertex, toAdd;
                    TIntObjectHashMap<WindyEdge> ansEdges = ans.getInternalEdgeMap();
                    for (int j = 1; j <= n; j++) {
                        if (maxPart.contains(j)) {
                            ansVertices.get(j).setMatchId(i++);

                            //set x and y
                            tempVertex = ansVertices.get(j);
                            toAdd = new WindyVertex("from OSM");
                            toAdd.setCoordinates(tempVertex.getX(), tempVertex.getY());
                            trueAns.addVertex(toAdd);
                        }
                    }

                    WindyEdge temp;
                    boolean isReq;
                    for (int j = 1; j <= m; j++) {
                        temp = ansEdges.get(j);
                        isReq = rng.nextDouble() > .5;
                        if (maxPart.contains(temp.getEndpoints().getFirst().getId()) && maxPart.contains(temp.getEndpoints().getSecond().getId())) {
                            trueAns.addEdge(temp.getEndpoints().getFirst().getMatchId(), temp.getEndpoints().getSecond().getMatchId(), temp.getCost(), temp.getReverseCost(), isReq);
                        }

                    }

                    if (!CommonAlgorithms.isConnected(trueAns))
                        System.out.println("Something's broken.");

                    needToGen = false;
                    mGraph = trueAns;
                    return trueAns;
                }

                break;


            } catch (Exception e) {
                System.out.println("Error querying the URL.  If there is another stable server, we shall attempt to query it.");
                e.printStackTrace();
            }
        }

        System.out.println("All URLs could not be queried.");

        needToGen = false;
        mGraph = ans;
        return ans;
    }

    private int latLonToMeters(double y1, double x1, double y2, double x2) {
        double R = 6378.137; //radius of the earth in KM
        double dLat = (y2 - y1) * Math.PI / 180.0;
        double dLon = (x2 - x1) * Math.PI / 180.0;
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                Math.cos(y1 * Math.PI / 180.0) * Math.cos(y2 * Math.PI / 180.0) *
                        Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return (int) (d * 1000);
    }
}
