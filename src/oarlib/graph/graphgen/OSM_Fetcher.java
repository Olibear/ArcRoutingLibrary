/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.graph.graphgen;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Link;
import oarlib.graph.graphgen.Util.BoundingBox;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
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
        return queryForGraph(1000);
    }

    public WindyGraph queryForGraph(long seed) {

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
                Random rng = new Random(seed);
                HashMap<String, Link.Zone> zoneRecords = new HashMap<String, Link.Zone>();
                HashMap<String, HashSet<WindyEdge>> zoneStreets = new HashMap<String, HashSet<WindyEdge>>();
                String streetName, typeString, zoneString;
                String[] speedSplit;
                Link.HighwayType type;
                Link.Zone zone;
                boolean isAHighway, required;
                int maxSpeed;

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

                            isAHighway = false;
                            type = Link.HighwayType.NOT_SET;
                            zone = Link.Zone.NOT_SET;
                            streetName = "(No street name provided)";
                            maxSpeed = 0;

                            //first, gather metadata
                            subNodeList = ((Element) node).getElementsByTagName("tag");
                            for(int j = 0; j < subNodeList.getLength(); j++) {

                                curr = subNodeList.item(j);
                                //if it's actually a street, figure out the name and type
                                if( ((Element)curr).getAttribute("k").equals("highway")) {
                                    isAHighway = true; //only want to add / look for more metadata if the way is a highway
                                    typeString = ((Element)curr).getAttribute("v");
                                    if(typeString.equalsIgnoreCase("motorway") || typeString.equalsIgnoreCase("trunk")) {
                                        type = Link.HighwayType.TRUNK;
                                    }
                                    else if(typeString.equalsIgnoreCase("primary")) {
                                        type = Link.HighwayType.PRIMARY;
                                    }
                                    else if(typeString.equalsIgnoreCase("secondary")) {
                                        type = Link.HighwayType.SECONDARY;
                                    }
                                    else if(typeString.equalsIgnoreCase("tertiary")) {
                                        type = Link.HighwayType.TERTIARY;
                                    }
                                    else if(typeString.equalsIgnoreCase("residential")) {
                                        type = Link.HighwayType.RESIDENTIAL_ACCESS;
                                    }
                                    else {
                                        type = Link.HighwayType.OTHER;
                                    }
                                }
                                else if( ((Element)curr).getAttribute("k").equals("name") || ((Element)curr).getAttribute("k").equals("addr:street"))
                                    streetName = ((Element)curr).getAttribute("v");
                                else if( ((Element)curr).getAttribute("k").equals("maxspeed")) {
                                    speedSplit = ((Element) curr).getAttribute("v").split("\\s+");
                                    maxSpeed = Integer.parseInt(speedSplit[0]);
                                }
                                else if( ((Element)curr).getAttribute("k").equals("building")) {
                                    zoneString = ((Element)curr).getAttribute("v");
                                    if(zoneString.equalsIgnoreCase("apartments") || zoneString.equalsIgnoreCase("farm") ||
                                            zoneString.equalsIgnoreCase("hotel") || zoneString.equalsIgnoreCase("house") ||
                                            zoneString.equalsIgnoreCase("detached") || zoneString.equalsIgnoreCase("residential") ||
                                            zoneString.equalsIgnoreCase("dormitory") || zoneString.equalsIgnoreCase("terrace") ||
                                            zoneString.equalsIgnoreCase("houseboat") || zoneString.equalsIgnoreCase("bungalow") ||
                                            zoneString.equalsIgnoreCase("static_caravan")) {
                                        zone = Link.Zone.RESIDENTIAL;
                                    }
                                    else if(zoneString.equalsIgnoreCase("commercial") || zoneString.equalsIgnoreCase("office") ||
                                            zoneString.equalsIgnoreCase("industrial") || zoneString.equalsIgnoreCase("retail") ||
                                            zoneString.equalsIgnoreCase("warehouse")) {
                                        zone = Link.Zone.COMMERCIAL;
                                    }
                                    else if(zoneString.equalsIgnoreCase("bakehouse") || zoneString.equalsIgnoreCase("cathedral") ||
                                            zoneString.equalsIgnoreCase("chapel") || zoneString.equalsIgnoreCase("church") ||
                                            zoneString.equalsIgnoreCase("mosque") || zoneString.equalsIgnoreCase("temple") ||
                                            zoneString.equalsIgnoreCase("synagogue") || zoneString.equalsIgnoreCase("shrine") ||
                                            zoneString.equalsIgnoreCase("civic") || zoneString.equalsIgnoreCase("hospital") ||
                                            zoneString.equalsIgnoreCase("school") || zoneString.equalsIgnoreCase("stadium") ||
                                            zoneString.equalsIgnoreCase("train_station") || zoneString.equalsIgnoreCase("transportation") ||
                                            zoneString.equalsIgnoreCase("university") || zoneString.equalsIgnoreCase("public")) {
                                        zone = Link.Zone.CIVIC;
                                    }
                                    else {
                                        zone = Link.Zone.OTHER;
                                    }
                                }
                            }

                            if(!isAHighway) { //log some info, then continue; it's not a street

                                if(!(zone == Link.Zone.NOT_SET)) {
                                    if(zoneRecords.containsKey(streetName) && zoneRecords.get(streetName) != zone)
                                        zoneRecords.put(streetName, Link.Zone.MIXED);
                                    else
                                        zoneRecords.put(streetName, zone);
                                }
                                continue;
                            }

                            //then, add 'em
                            subNodeList = ((Element) node).getElementsByTagName("nd");
                            for (int j = 1; j < subNodeList.getLength(); j++) {
                                curr = subNodeList.item(j);
                                prev = subNodeList.item(j - 1);

                                if (curr instanceof Element && prev instanceof Element) {
                                    from = refIds.get(((Element) prev).getAttribute("ref"));
                                    to = refIds.get(((Element) curr).getAttribute("ref"));
                                    cost = latLonToMeters(ansVertices.get(from).getY(), ansVertices.get(from).getX(), ansVertices.get(to).getY(), ansVertices.get(to).getX()) + 1; //don't want 0 cost edges

                                    perturb = (rng.nextInt(30) - 15) / 100.0;
                                    reverseCost = (int) (cost * (1 + perturb));
                                    required = (rng.nextInt(30) < 15);

                                    WindyEdge toAdd = new WindyEdge(streetName, new Pair<WindyVertex>(ans.getVertex(from), ans.getVertex(to)), cost, reverseCost);
                                    toAdd.setRequired(required);
                                    toAdd.setMaxSpeed(maxSpeed);
                                    toAdd.setType(type);

                                    if(!zoneStreets.containsKey(streetName))
                                        zoneStreets.put(streetName, new HashSet<WindyEdge>());

                                    zoneStreets.get(streetName).add(toAdd);

                                    ans.addEdge(toAdd);
                                }
                            }
                        }
                    }
                }

                /*
                 * now assign zones
                 */
                for(String street : zoneRecords.keySet()) {
                    if(zoneStreets.containsKey(street)) {
                        Link.Zone z = zoneRecords.get(street);
                        for(WindyEdge we : zoneStreets.get(street)) {
                            we.setZone(z);
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

                    WindyEdge temp, edgeToAdd;
                    WindyVertex tempFirst, tempSecond;
                    boolean isReq;
                    for (int j = 1; j <= m; j++) {
                        temp = ansEdges.get(j);
                        isReq = rng.nextDouble() > .5;
                        if (maxPart.contains(temp.getEndpoints().getFirst().getId()) && maxPart.contains(temp.getEndpoints().getSecond().getId())) {
                            tempFirst = trueAns.getVertex(temp.getEndpoints().getFirst().getMatchId());
                            tempSecond = trueAns.getVertex(temp.getEndpoints().getSecond().getMatchId());
                            if (tempFirst.getNeighbors().containsKey(tempSecond))//don't create a multigraph
                                continue;
                            edgeToAdd = new WindyEdge(temp.getLabel(),new Pair<WindyVertex>(trueAns.getVertex(temp.getEndpoints().getFirst().getMatchId()), trueAns.getVertex(temp.getEndpoints().getSecond().getMatchId())), temp.getCost(), temp.getReverseCost());
                            edgeToAdd.setRequired(isReq);
                            edgeToAdd.setMaxSpeed(temp.getMaxSpeed());
                            edgeToAdd.setType(temp.getType());
                            edgeToAdd.setZone(temp.getZone());
                            trueAns.addEdge(edgeToAdd);
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

    public static WindyGraph removeDegreeOneNodes(WindyGraph input) {

        ArrayList<WindyEdge> edgesToRemove = new ArrayList<WindyEdge>();
        ArrayList<WindyVertex> verticesToRemove = new ArrayList<WindyVertex>();
        boolean removedVertex = true;

        while(removedVertex) {
            //now remove vertices of degree 1
            for (WindyVertex wv : input.getVertices()) {
                if (wv.getDegree() == 1 && wv.getNeighbors().keySet().size() == 1) {
                    for (WindyVertex neighbor : wv.getNeighbors().keySet()) {

                        WindyEdge connector = wv.getNeighbors().get(neighbor).get(0);
                        edgesToRemove.add(connector);

                    }

                    verticesToRemove.add(wv);
                }
                for (WindyEdge we : edgesToRemove) {
                    input.removeEdge(we);
                }
                edgesToRemove.clear();

            }

            for (WindyVertex wv : verticesToRemove) {
                input.removeVertex(wv);
            }

            //go until we don't have anymore
            if(verticesToRemove.size() == 0)
                removedVertex = false;

            verticesToRemove.clear();
        }

        //perhaps now re-index
        return CommonAlgorithms.collapseIndices(input);

    }

    public static WindyGraph removeDegreeTwoNodes(WindyGraph input) {

        int neighbor1To2 = 0;
        int neighbor2To1 = 0;
        ArrayList<Integer> neighborIds = new ArrayList<Integer>();
        ArrayList<WindyEdge> edgesToRemove = new ArrayList<WindyEdge>();
        ArrayList<WindyVertex> verticesToRemove = new ArrayList<WindyVertex>();

        //now remove vertices of degree 2, and destroy the two links to create just 1
        for(WindyVertex wv : input.getVertices()) {
            if(wv.getDegree() == 2 && wv.getNeighbors().keySet().size() == 2) {
                for(WindyVertex neighbor: wv.getNeighbors().keySet()) {
                    WindyEdge connector = wv.getNeighbors().get(neighbor).get(0);

                    //grab costs for new edge
                    if(connector.getEndpoints().getFirst().getId() == wv.getId()) {
                        neighbor1To2 += connector.getReverseCost();
                        neighbor2To1 += connector.getCost();
                        neighborIds.add(connector.getEndpoints().getSecond().getId());
                    }
                    else {
                        neighbor1To2 += connector.getCost();
                        neighbor2To1 += connector.getReverseCost();
                        neighborIds.add(connector.getEndpoints().getFirst().getId());
                    }
                    edgesToRemove.add(connector);

                }
                try {
                    input.addEdge(neighborIds.get(0), neighborIds.get(1), neighbor1To2, neighbor2To1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                verticesToRemove.add(wv);
                neighbor1To2 = 0;
                neighbor2To1 = 0;
                neighborIds.clear();
            }
            for(WindyEdge we : edgesToRemove) {
                input.removeEdge(we);
            }
            edgesToRemove.clear();

        }

        for(WindyVertex wv : verticesToRemove)
                input.removeVertex(wv);

        //perhaps now re-index
        return CommonAlgorithms.collapseIndices(input);
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
