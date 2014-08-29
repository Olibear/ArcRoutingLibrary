package oarlib.graph.transform.rebalance;

import oarlib.core.Graph;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.GraphTransformer;

import java.util.ArrayList;

/**
 * Created by oliverlum on 8/9/14.
 */
public abstract class RebalanceTransformer<S extends Graph<?, ?>> implements GraphTransformer<S, S> {
    protected S mGraph;
    protected ArrayList<Integer> mPartition;

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input     - the input graph.
     * @param partition - an ArrayList that has an entry for each vertex in the graph; entry i has value j if vertex
     *                  with internal id i is currently assigned to partition j.
     * @throws FormatMismatchException - if the ArrayList is of the wrong size.
     */
    protected RebalanceTransformer(S input, ArrayList<Integer> partition) throws FormatMismatchException {
        if (partition.size() + 1 != input.getVertices().size())
            throw new FormatMismatchException();
        mGraph = input;
        mPartition = partition;
    }

}
