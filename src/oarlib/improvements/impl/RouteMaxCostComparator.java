package oarlib.improvements.impl;

import oarlib.core.Route;

import java.util.Collection;
import java.util.Comparator;

/**
 * Created by oliverlum on 1/22/16.
 */
public class RouteMaxCostComparator implements Comparator<Collection<Route>> {
    @Override
    public int compare(Collection<Route> o1, Collection<Route> o2) {
        int max1 = Integer.MIN_VALUE;
        int max2 = Integer.MIN_VALUE;
        for(Route r: o1)
            if(r.getCost() > max1)
                max1 = r.getCost();
        for(Route r: o2)
            if(r.getCost() > max2)
                max2 = r.getCost();

        if(max1 > max2)
            return 1;
        else if (max1 < max2)
            return -1;
        else
            return 0;
    }
}
