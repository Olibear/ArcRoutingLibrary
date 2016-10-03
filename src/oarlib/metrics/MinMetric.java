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
package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class MinMetric extends Metric {

    private Graph mGraph;

    public MinMetric() {
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        int min = Integer.MAX_VALUE;
        for (Route r : routes)
            if (r.getCost() < min)
                min = r.getCost();
        return min;
    }

    @Override
    public Type getType() {
        return Type.MIN;
    }

    @Override
    public String toString() {
        return "Min Route Length";
    }
}
