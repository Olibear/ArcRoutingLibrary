package oarlib.graph.transform.impl;

import oarlib.graph.impl.WindyGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.transform.GraphTransformer;
import oarlib.link.impl.ZigZagLink;
import org.apache.log4j.Logger;

/**
 * First attempt to transform a ZigZag problem into a WRPP problem that is solved using existing heuristics
 * <p/>
 * Created by oliverlum on 5/25/15.
 */
public class ZigZagToWindyTransform1 implements GraphTransformer<ZigZagGraph, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(EdgeInducedSubgraphTransform.class);

    ZigZagGraph mGraph;

    public ZigZagToWindyTransform1(ZigZagGraph input) {
        setGraph(input);
    }

    @Override
    public void setGraph(ZigZagGraph input) {
        mGraph = input;
    }

    @Override
    public WindyGraph transformGraph() {

        int n = mGraph.getVertices().size();
        WindyGraph ans = new WindyGraph(n);

        int BIG = 0;
        for (ZigZagLink zzl : mGraph.getEdges()) {
            BIG += zzl.getCost() + zzl.getReverseCost();
            if (zzl.isRequired())
                System.out.println(zzl.toString() + " is required.");
            if (zzl.isReverseRequired())
                System.out.println(zzl.toString() + " is reverse required.");
        }

        try {

            //cases separated for policy
            for (ZigZagLink zzl : mGraph.getEdges()) {

                //case 1: it's not required, then just add it
                if (!zzl.isRequired() && !zzl.isReverseRequired()) {
                    ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), false);
                }
                //if it's required, but not meanderable...
                else if (zzl.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                    //case 2: if it's only required in one direction, add a required 'arc', and a non-required other opposite arc
                    if (!zzl.isRequired()) {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", BIG, zzl.getReverseCost(), zzl.getId(), true);
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), false);
                    } else if (!zzl.isReverseRequired()) {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), BIG, zzl.getId(), true);
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), false);
                    } else {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), BIG, zzl.getId(), true);
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", BIG, zzl.getReverseCost(), zzl.getId(), true);
                    }

                }
                //case 3: if it's meander required, we add it, and simply incur the meander cost later
                else if (zzl.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY) {
                    ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), true);
                }
                //if it's optional...
                else if (zzl.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL) {

                    //case 4: but only required in one direction, model it as a required edge
                    if (!zzl.isRequired()) {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), false);
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", (int) zzl.getZigzagCost(), zzl.getReverseCost(), zzl.getId(), true);
                    } else if (!zzl.isReverseRequired()) {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), false);
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), (int) zzl.getZigzagCost(), zzl.getId(), true);
                    }
                    //case 5: but bidirectional, add it; if we happen to traverse in both directions, we reap savings, otherwise meander
                    else {
                        ans.addEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getId(), true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return ans;
    }

}
