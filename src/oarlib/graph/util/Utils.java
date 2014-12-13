package oarlib.graph.util;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.link.impl.WindyEdge;
import oarlib.objfunc.ObjectiveFunction;
import oarlib.route.impl.Tour;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);

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

            if (candidates == null)
                System.out.println("DEBUG");

            for (Link<? extends Vertex> l2 : candidates) {
                if (l2.getEndpoints().getFirst().getId() == firstId && traversalCost == l2.getCost()) {
                    ans.appendEdge(l2);
                    foundIt = true;
                    break;
                } else if (!l2.isDirected()) {
                    secondCost = l2.getCost();
                    if (l2.isWindy())
                        secondCost = ((WindyEdge) l2).getReverseCost();
                    if (l2.getEndpoints().getFirst().getId() == secondId && traversalCost == secondCost) {
                        ans.appendEdge(l2);
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

    public static <V extends Vertex, E extends Link<V>> int getObjectiveValue(Collection<Route<V,E>> sol, ObjectiveFunction type) {

        if(type == ObjectiveFunction.MAX) {
            return calcMax(sol);
        }
        else if(type == ObjectiveFunction.SUM) {
            return calcSum(sol);
        }
        else if(type == ObjectiveFunction.AVG) {
            return calcAvg(sol);
        }
        else if(type == ObjectiveFunction.DEV) {
            return calcDev(sol);
        }

        //unrecognized case
        LOGGER.warn("This ObjectiveFunction type was not recognized.  Returning default value.");
        return  - 1;
    }

    public static <V extends Vertex, E extends Link<V>, G extends Graph<V,E>> double calcATD(Collection<Route<V,E>> sol, G g) {

        int numRoutes = sol.size();
        int numTasks = 0;
        HashSet<Integer> alreadyTraversed = new HashSet<Integer>();
        int sumNonReq = 0;

        for( Route<V,E> r: sol ) {
            sumNonReq += r.getCost() - r.getReqCost();
            for(E l: r.getRoute()) {
                if(l.isRequired() && !alreadyTraversed.contains(l.getId())) {
                    alreadyTraversed.add(l.getId());
                    numTasks++;
                }
            }
        }

        double denom = numTasks * (numTasks - numRoutes) / (2 * numRoutes);

        return (double)sumNonReq / denom;
    }

    public static <V extends Vertex, E extends Link<V>, G extends Graph<V,E>> double calcROI(Collection<Route<V,E>> sol, G g) {

        int no = calcNO(sol);
        int N = g.getVertices().size();
        int routes = sol.size();

        return (no - N)/(Math.pow(Math.sqrt(routes) + Math.sqrt(N) - 1, 2) - N);
    }

    public static <V extends Vertex, E extends Link<V>> int calcCI(Collection<Route<V,E>> sol) {
        int numRoutes = sol.size();
        //TODO

        return -1;
    }

    public static <V extends Vertex, E extends Link<V>> int calcNO(Collection<Route<V,E>> sol) {

        int NO = 0;

        for(Route<V,E> r: sol) {
            for(E link: r.getRoute()) {
                if(link.isRequired())
                    NO += 2;
            }
        }

        return NO;
    }

    private static <V extends Vertex, E extends Link<V>> int calcMax(Collection<Route<V,E>> sol) {
        int max = Integer.MIN_VALUE;
        for (Route r : sol)
            if (r.getCost() > max)
                max = r.getCost();
        return max;
    }

    private static <V extends Vertex, E extends Link<V>> int calcSum(Collection<Route<V,E>> sol) {
        int sum = 0;
        for (Route r : sol)
            sum += r.getCost();
        return sum;
    }

    private static <V extends Vertex, E extends Link<V>> int calcAvg(Collection<Route<V,E>> sol) {
        int max = Integer.MIN_VALUE;
        for (Route r : sol)
            if (r.getCost() > max)
                max = r.getCost();
        return (max / sol.size());
    }

    private static <V extends Vertex, E extends Link<V>> int calcDev(Collection<Route<V,E>> sol) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int tempCost;
        for (Route r : sol) {
            tempCost = r.getCost();
            if (tempCost > max)
                max = tempCost;
            if (tempCost < min)
                min = tempCost;
        }
        return (100 * max / min) - 100;
    }


}
