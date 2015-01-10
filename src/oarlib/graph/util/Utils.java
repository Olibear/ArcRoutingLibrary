package oarlib.graph.util;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    public static <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> Route reclaimTour(Route<? extends Vertex, ? extends Link<? extends Vertex>> origAns, G g) {

        Tour ans = new Tour();

        List<? extends Link> path = origAns.getRoute();
        List<? extends Link<? extends Vertex>> candidates;
        int n = path.size();
        int firstId, secondId, traversalCost, secondCost;
        boolean foundIt;

        for (int i = 0; i < n; i++) {

            Link<? extends Vertex> l = path.get(i);
            firstId = l.getEndpoints().getFirst().getId();
            secondId = l.getEndpoints().getSecond().getId();
            traversalCost = l.getCost();
            foundIt = false;

            candidates = g.getVertex(firstId).getNeighbors().get(g.getVertex(secondId));

            if (firstId == secondId && firstId == g.getDepotId())
                continue;

            for (Link<? extends Vertex> l2 : candidates) {
                if (l.isRequired() && !l2.isRequired())
                    continue;
                if (l2.getEndpoints().getFirst().getId() == firstId && traversalCost == l2.getCost()) {
                    ans.appendEdge(l2, l.isRequired());
                    foundIt = true;
                    break;
                } else if (!l2.isDirected()) {
                    secondCost = l2.getCost();
                    if (l2.isWindy())
                        secondCost = ((WindyEdge) l2).getReverseCost();
                    if (l2.getEndpoints().getFirst().getId() == secondId && traversalCost == secondCost) {
                        ans.appendEdge(l2, l.isRequired());
                        foundIt = true;
                        break;
                    }
                }
            }
            if (!foundIt) {
                LOGGER.error("It seems as though this solution is invalid(?)");
                return null;
            }
        }

        return ans;

    }

    public static class DijkstrasComparator implements Comparator<Pair<Integer>> {
        @Override
        public int compare(Pair<Integer> arg0, Pair<Integer> arg1) {
            if (arg0.getSecond() > arg1.getSecond())
                return 1;
            else if (arg0.getSecond() < arg1.getSecond())
                return -1;
            return 0;
        }
    }

    public static class InverseDijkstrasComparator implements Comparator<Pair<Integer>> {
        @Override
        public int compare(Pair<Integer> arg0, Pair<Integer> arg1) {
            if (arg0.getSecond() < arg1.getSecond())
                return 1;
            else if (arg0.getSecond() > arg1.getSecond())
                return -1;
            return 0;
        }
    }

    public static class DijkstrasComparatorForRecords implements Comparator<UnmatchedPair<Integer, IndexedRecord<Integer>>> {
        @Override
        public int compare(UnmatchedPair<Integer, IndexedRecord<Integer>> arg0, UnmatchedPair<Integer, IndexedRecord<Integer>> arg1) {
            return arg0.getSecond().getRecord().compareTo(arg1.getSecond().getRecord());
        }
    }

    public static class InverseDijkstrasComparatorForRecords implements Comparator<UnmatchedPair<Integer, IndexedRecord<Integer>>> {
        @Override
        public int compare(UnmatchedPair<Integer, IndexedRecord<Integer>> arg0, UnmatchedPair<Integer, IndexedRecord<Integer>> arg1) {
            return arg1.getSecond().getRecord().compareTo(arg0.getSecond().getRecord());
        }
    }


}
