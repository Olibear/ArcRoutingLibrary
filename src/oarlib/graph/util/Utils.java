package oarlib.graph.util;

import java.util.Comparator;

public class Utils {

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
