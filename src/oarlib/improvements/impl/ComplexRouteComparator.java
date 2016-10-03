/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
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
