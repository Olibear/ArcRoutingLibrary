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

}
