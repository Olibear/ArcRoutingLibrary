package oarlib.improvements.impl;

import oarlib.core.Route;
import oarlib.metrics.Metric;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by oliverlum on 1/22/16.
 */
public class ComplexRouteComparator implements Comparator<Collection<Route>> {

    HashMap<Metric, Double> mWeights;

    /**
     *
     * @param weights
     */
    ComplexRouteComparator(HashMap<Metric, Double> weights){
        mWeights = weights;
    }
    @Override
    public int compare(Collection<Route> o1, Collection<Route> o2) {
        double total1 = 0;
        double total2 = 0;
        for(Metric m : mWeights.keySet()) {
            total1 += m.evaluate(o1) * mWeights.get(m);
            total2 += m.evaluate(o2) * mWeights.get(m);
        }

        if(total1 > total2)
            return 1;
        else if (total1 < total2)
            return -1;
        else
            return 0;
    }
}
