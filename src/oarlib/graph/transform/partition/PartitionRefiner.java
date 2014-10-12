package oarlib.graph.transform.partition;

import java.util.HashMap;

/**
 * Created by oliverlum on 9/18/14.
 */
public abstract class PartitionRefiner {
    /**
     * @param currMap
     * @return
     */
    public abstract HashMap<Integer, Integer> refinePartition(HashMap<Integer, Integer> currMap);
}
