package oarlib.graph.transform.rebalance;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.link.impl.WindyEdge;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.impl.WindyGraph;

import java.util.HashMap;

/**
 * Created by oliverlum on 8/9/14.
 */
public abstract class CostRebalancer<S extends Graph<?, ?>> {
    protected S mGraph;
    protected CostRebalancer<S> mNextRebalancer;
    private HashMap<Integer, Integer> defaultMap;

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input - the input graph.
     * @throws FormatMismatchException - if the ArrayList is of the wrong size.
     */
    protected CostRebalancer(S input) throws FormatMismatchException {
        mGraph = input;
    }

    protected CostRebalancer(S input, CostRebalancer<S> nextRebalancer) {
        mGraph = input;
        mNextRebalancer = nextRebalancer;
        defaultMap = new HashMap<Integer, Integer>();
        if (input.getClass() == WindyGraph.class) {
            for (Link l : mGraph.getEdges())
                defaultMap.put(l.getId(), (int) (l.getCost() + ((WindyEdge) l).getReverseCost() * .5));
        } else {
            for (Link l : mGraph.getEdges()) {
                defaultMap.put(l.getId(), l.getCost());
            }
        }
    }

    public final HashMap<Integer, Integer> rebalance() {
        HashMap<Integer, Integer> baseAns = this.startRebalance(defaultMap);
        if (mNextRebalancer != null) ;
        return mNextRebalancer.startRebalance(baseAns);
    }

    protected abstract HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input);

}
