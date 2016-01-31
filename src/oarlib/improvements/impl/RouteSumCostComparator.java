package oarlib.improvements.impl;

import oarlib.core.Route;

import java.util.Collection;
import java.util.Comparator;

/**
 * Simple way of comparing two sets of routes for local search.  Note that this probably isn't
 * the most computationally efficient way, since it calculates the metric on each object instead
 * of just calculating the difference.  Still, it's the easiest way of doing business, and will
 * only be a performance bottleneck if a metric is very compute intensive.
 *
 * Created by oliverlum on 1/22/16.
 */
public class RouteSumCostComparator implements Comparator<Collection<Route>> {
    @Override
    public int compare(Collection<Route> o1, Collection<Route> o2) {
        int total1 = 0;
        int total2 = 0;
        for(Route r: o1)
            total1 += r.getCost();
        for(Route r: o2)
            total2 += r.getCost();

        if(total1 > total2)
            return 1;
        else if (total1 < total2)
            return -1;
        else
            return 0;
    }
}
