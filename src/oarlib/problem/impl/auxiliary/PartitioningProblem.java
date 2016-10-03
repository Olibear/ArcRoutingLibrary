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
package oarlib.problem.impl.auxiliary;

import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.metrics.Metric;
import oarlib.problem.impl.ProblemAttributes;

import java.util.Collection;

/**
 * Created by oliverlum on 3/13/15.
 */
public class PartitioningProblem extends Problem {

    public PartitioningProblem(Graph graph, String name, Metric objFunc) {
        super(graph, name, objFunc);
    }

    @Override
    public boolean isFeasible(Collection collection) {
        //this is meaningless for this problem; just to ensure no checks go awry
        return true;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(null, ProblemAttributes.Type.PARTITIONING, ProblemAttributes.NumVehicles.NO_VEHICLES, ProblemAttributes.NumDepots.NO_DEPOTS, null);
    }
}
