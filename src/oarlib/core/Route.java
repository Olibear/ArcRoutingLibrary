package oarlib.core;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import oarlib.display.GraphDisplay;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.UndirectedVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 *
 * @author oliverlum
 */
public abstract class Route<V extends Vertex, E extends Link<V>> {

    private static final Logger LOGGER = Logger.getLogger(Route.class);

    protected int mCost; // cost of the route
    protected int mReqCost;
    protected TIntIntHashMap mCustomIDMap;
    protected ArrayList<E> mRoute;
    protected TIntArrayList compactRepresentation;
    protected ArrayList<Boolean> compactTD;

    //default constructor
    protected Route() {

        mCost = 0;
        mReqCost = 0;
        mCustomIDMap = new TIntIntHashMap();
        mRoute = new ArrayList<E>();
        compactRepresentation = new TIntArrayList();
        compactTD = new ArrayList<Boolean>();

    }

    /**
     * A constructor that allows you to specify an ID remap, so that when the toString() is called, the mapped values are used instead of the original ones.
     * This is particularly helpful if you wish to use matchIds from another graph, since solvers will frequently solve the graph on a copy so that the original
     * is left untouched, but this results in the vertices referenced in the links contained in the Route not having the same matchIds as they went into the solver
     * with.
     *
     * @param customIDMap
     */
    protected Route(TIntIntHashMap customIDMap) {
        mCost = 0;
        mCustomIDMap = customIDMap;
    }

    public void exportRouteToPDF(String instanceName) {

        UndirectedGraph toExport = new UndirectedGraph();

        try {

            HashMap<Integer, Integer> vertexMap = new HashMap<Integer, Integer>();
            Set<Integer> keySet = vertexMap.keySet();
            List<E> route = this.getRoute();
            int vertexIndex = 1;
            Vertex first, second;
            UndirectedVertex toAdd;

            for (E l : route) {

                first = l.getEndpoints().getFirst();
                if (!keySet.contains(first.getId())) {
                    toAdd = new UndirectedVertex("");
                    toAdd.setCoordinates(first.getX(), first.getY());
                    toExport.addVertex(toAdd);
                    vertexMap.put(first.getId(), vertexIndex++);
                }

                second = l.getEndpoints().getSecond();
                if (!keySet.contains(second.getId())) {
                    toAdd = new UndirectedVertex("");
                    toAdd.setCoordinates(second.getX(), second.getY());
                    toExport.addVertex(toAdd);
                    vertexMap.put(second.getId(), vertexIndex++);
                }

                toExport.addEdge(vertexMap.get(first.getId()), vertexMap.get(second.getId()), 1, l.isRequired());

            }

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toExport, instanceName);
            gd.export(GraphDisplay.ExportType.PDF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the cost of the route
     */
    public int getCost() {
        return mCost;
    }

    /**
     * @return the cost of the required links on the route
     */
    public int getReqCost() {
        return mReqCost;
    }

    /**
     * Fetch the id mapping used.
     */
    public TIntIntHashMap getMapping() {
        return mCustomIDMap;
    }

    /**
     * Alter the id mapping used.
     *
     * @param newMapping
     */
    public void setMapping(TIntIntHashMap newMapping) {
        mCustomIDMap = newMapping;
    }

    /**
     * Retrieve a copy of the current route.
     *
     * @return List of edges to be traversed from first to last
     */
    public List<E> getRoute() {
        return mRoute;
    }


    /**
     * Retrive a copy of the traversal direction arraylist.  An ith entry value of
     * true means the ith entry of the compact route
     * is traversed first -> second, false second -> first.
     *
     * @return List of booleans corresponding to whether or not the link is traversed 'forward.'
     */
    public ArrayList<Boolean> getCompactTraversalDirection(){
        return compactTD;
    }

    /**
     * Add a edge to the end of this route.
     *
     * @param l
     */
    public void appendEdge(E l) {

            //TODO:Fix this make it less clunky, and to perhaps calculate the toString here, maybe a different WindyRoute class, maybe use isWindy property of links
            boolean isWindy;
            if(l.isWindy())
                isWindy = true;
            else
                isWindy = false;

            //check for a common endpoint
            int lFirst = l.getEndpoints().getFirst().getId();
            int lSecond = l.getEndpoints().getSecond().getId();

            if(mRoute.size() == 0) {
                mRoute.add(l);
                return;
            }


            E temp = mRoute.get(mRoute.size()-1);
            int tempFirst = temp.getEndpoints().getFirst().getId();
            int tempSecond = temp.getEndpoints().getSecond().getId();

            int trueCost;
            if(mRoute.size() == 1) {
                if(lFirst == tempFirst && isWindy) {
                    trueCost = ((WindyEdge) temp).getReverseCost();
                    if(temp.isRequired()) {
                        mReqCost += trueCost;
                        compactTD.add(false);
                        compactRepresentation.add(temp.getId());
                    }
                }
                else {
                    trueCost = temp.getCost();
                    if(temp.isRequired()) {
                        mReqCost += trueCost;
                        compactTD.add(true);
                        compactRepresentation.add(temp.getId());
                    }
                }

                mCost += trueCost;

            }

            //TODO: Check for directed contraints as well

            if(!isWindy) {
                trueCost = l.getCost();
                if(l.isRequired())
                    compactRepresentation.add(l.getId());
            }
            //check for same conn.
            else if(lFirst == tempFirst && lSecond == tempSecond) {
                boolean tempTD = !compactTD.get(compactTD.size()-1);
                if(tempTD) {
                    trueCost = l.getCost();
                    if(l.isRequired()) {
                        compactTD.add(true);
                        compactRepresentation.add(l.getId());
                    }
                }
                else {
                    trueCost = ((WindyEdge)l).getReverseCost();
                    if(l.isRequired()) {
                        compactTD.add(false);
                        compactRepresentation.add(l.getId());
                    }
                }
            }
            else if(lFirst == tempSecond && lSecond == tempFirst) {
                boolean tempTD = compactTD.get(compactTD.size()-1);
                if(tempTD) {
                    trueCost = l.getCost();
                    if(l.isRequired()) {
                        compactTD.add(true);
                        compactRepresentation.add(l.getId());
                    }
                }
                else {
                    trueCost = ((WindyEdge)l).getReverseCost();
                    if(l.isRequired()) {
                        compactTD.add(false);
                        compactRepresentation.add(l.getId());
                    }
                }
            }
            else if(lFirst == tempFirst || lFirst == tempSecond) {
                trueCost = l.getCost();
                if(l.isRequired()) {
                    compactTD.add(true);
                    compactRepresentation.add(l.getId());
                }
            } else if(lSecond == tempFirst || lSecond == tempSecond) {
                trueCost = ((WindyEdge)l).getReverseCost();
                if(l.isRequired()) {
                    compactTD.add(false);
                    compactRepresentation.add(l.getId());
                }
            } else {
                LOGGER.error("The link you're attempting to add doens't share an endpoint with the previous one.");
                throw new IllegalArgumentException();
            }

            mRoute.add(l);
            mCost += trueCost;
            if (l.isRequired())
                mReqCost += trueCost;

    }

    /**
     * check to make sure that the route is actually a route, (i.e. that consecutive
     * links in the list are adjacent, and any other checks that different types
     * of routes may wish to perform).
     *
     * @return true if route is feasible in the provided graph
     */
    public abstract boolean checkRoutes(Graph<V,E> g);

    /**
     * Outputs a string representation of the route.
     */
    public String toString() {
        String ans = "";
        List<E> list = this.getRoute();
        int n = list.size();
        E tempL, tempL2;
        int prevId1, prevId2, prevAltId1, prevAltId2, prevIdReal, beginningCycleLength;
        Vertex tempV1, tempV2, tempV12, tempV22;
        boolean firstToSecond = false;
        boolean useCustomMapping = true;

        if (mCustomIDMap.isEmpty())
            useCustomMapping = false;

        //edge case
        if (n == 0)
            return ans;

        if (n == 1) {
            tempL = list.get(0);
            if (useCustomMapping) {
                ans += mCustomIDMap.get(tempL.getEndpoints().getFirst().getId()) + "-";
                ans += mCustomIDMap.get(tempL.getEndpoints().getSecond().getId());
            } else {
                ans += tempL.getEndpoints().getFirst().getId() + "-";
                ans += tempL.getEndpoints().getSecond().getId();
            }

            return ans;
        }


        tempL = list.get(0);
        tempV1 = tempL.getEndpoints().getFirst();
        tempV2 = tempL.getEndpoints().getSecond();
        prevId1 = tempV1.getId();
        prevId2 = tempV2.getId();
        if (useCustomMapping) {
            prevAltId1 = mCustomIDMap.get(tempV1.getId());
            prevAltId2 = mCustomIDMap.get(tempV2.getId());
        } else {
            prevAltId1 = tempV1.getId();
            prevAltId2 = tempV2.getId();
        }

        //first vertex
        tempL = list.get(1);
        tempV1 = tempL.getEndpoints().getFirst();
        tempV2 = tempL.getEndpoints().getSecond();

        //special case where the first n edges form consecutive 2-cycles and everybody is undirected, we have to look forward to decide how to orient
        if (tempV1.getId() == prevId1 && tempV2.getId() == prevId2 || tempV1.getId() == prevId2 && tempV2.getId() == prevId1) {
            beginningCycleLength = 2;
            for (int i = 2; i < n; i++) {
                tempL2 = list.get(i);
                tempV12 = tempL2.getEndpoints().getFirst();
                tempV22 = tempL2.getEndpoints().getSecond();
                if (tempV12.getId() == prevId1 && tempV22.getId() == prevId2 || tempV12.getId() == prevId2 && tempV22.getId() == prevId1)
                    beginningCycleLength++;
                else {
                    //figure out the launch point; was it prevId1, or prevId2
                    if (tempV12.getId() == prevId1 || tempV22.getId() == prevId1) {
                        //we need to start at prevId1 - prevId2 - prevId1 - ...
                        firstToSecond = beginningCycleLength % 2 == 0;
                    } else if (tempV12.getId() == prevId2 || tempV22.getId() == prevId2) {
                        firstToSecond = beginningCycleLength % 2 != 0;
                    } else {
                        ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
                        return ans;
                    }
                    break;
                }
            }
        }

        if (tempV1.getId() == prevId1) {
            if (tempV2.getId() == prevId2) {
                if (firstToSecond) {
                    ans += prevAltId1 + "-" + prevAltId2 + "-";
                    prevIdReal = prevId1;
                } else {
                    ans += prevAltId2 + "-" + prevAltId1 + "-";
                    prevIdReal = prevId2;
                }
            } else {
                ans += prevAltId2 + "-" + prevAltId1 + "-";
                prevIdReal = tempV2.getId();
            }
        } else if (tempV2.getId() == prevId1) {
            if (tempV1.getId() == prevId2) {
                if (firstToSecond) {
                    ans += prevAltId1 + "-" + prevAltId2 + "-";
                    prevIdReal = prevId1;
                } else {
                    ans += prevAltId2 + "-" + prevAltId1 + "-";
                    prevIdReal = prevId2;
                }
            } else {
                ans += prevAltId2 + "-" + prevAltId1 + "-";
                prevIdReal = tempV1.getId();
            }
        } else if (tempV1.getId() == prevId2) {
            ans += prevAltId1 + "-" + prevAltId2 + "-";
            prevIdReal = tempV2.getId();
        } else if (tempV2.getId() == prevId2) {
            ans += prevAltId1 + "-" + prevAltId2 + "-";
            prevIdReal = tempV1.getId();
        } else {
            ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
            return ans;
        }

        for (int i = 2; i < n; i++) {
            tempL = list.get(i);
            tempV1 = tempL.getEndpoints().getFirst();
            tempV2 = tempL.getEndpoints().getSecond();

            if (tempV1.getId() == prevIdReal) {
                if (useCustomMapping)
                    ans += mCustomIDMap.get(tempV1.getId()) + "-";
                else
                    ans += tempV1.getId() + "-";
                prevIdReal = tempV2.getId();
            } else if (tempV2.getId() == prevIdReal) {
                if (useCustomMapping)
                    ans += mCustomIDMap.get(tempV2.getId()) + "-";
                else
                    ans += tempV2.getId() + "-";
                prevIdReal = tempV1.getId();
            } else {
                ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
                return ans;
            }
        }

        if (n >= 2) {
            if (useCustomMapping)
                ans += mCustomIDMap.get(prevIdReal);
            else
                ans += prevIdReal;
        }
        return ans;
    }


}
