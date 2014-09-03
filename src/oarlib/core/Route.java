package oarlib.core;

import java.util.HashMap;
import java.util.List;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 *
 * @author oliverlum
 */
public abstract class Route {

    protected int mCost; // cost of the route
    protected HashMap<Integer, Integer> mCustomIDMap;

    //default constructor
    protected Route() {

        mCost = 0;
        mCustomIDMap = new HashMap<Integer, Integer>();

    }

    /**
     * A constructor that allows you to specify an ID remap, so that when the toString() is called, the mapped values are used instead of the original ones.
     * This is particularly helpful if you wish to use matchIds from another graph, since solvers will frequently solve the graph on a copy so that the original
     * is left untouched, but this results in the vertices referenced in the links contained in the Route not having the same matchIds as they went into the solver
     * with.
     *
     * @param customIDMap
     */
    protected Route(HashMap<Integer, Integer> customIDMap) {

        mCost = 0;
        mCustomIDMap = customIDMap;

    }

    /**
     * @return the cost of the route
     */
    public int getCost() {
        return mCost;
    }

    /**
     * Alter the id mapping used.
     *
     * @param newMapping
     */
    public void setMapping(HashMap<Integer, Integer> newMapping) {
        mCustomIDMap = newMapping;
    }

    /**
     * Fetch the id mapping used.
     *
     */
    public HashMap<Integer, Integer> getMapping() {
        return mCustomIDMap;
    }



    /**
     * Retrieve a copy of the current route.
     *
     * @return List of edges to be traversed from first to last
     */
    public abstract List<? extends Link<? extends Vertex>> getRoute();

    /**
     * Add a edge to the end of this route.
     *
     * @param l
     */
    public abstract void appendEdge(Link<? extends Vertex> l);

    /**
     * check to make sure that the route is actually a route, (i.e. that consecutive
     * links in the list are adjacent, and any other checks that different types
     * of routes may wish to perform).
     *
     * @return true if route is feasible in the provided graph
     */
    public abstract boolean checkRoutes(Graph<? extends Vertex, ? extends Link<? extends Vertex>> g);

    /**
     * Outputs a string representation of the route.
     */
    public String toString() {
        String ans = "";
        List<? extends Link<? extends Vertex>> list = this.getRoute();
        int n = list.size();
        Link<? extends Vertex> tempL, tempL2;
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
