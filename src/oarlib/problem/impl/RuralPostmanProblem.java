/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
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
 */
package oarlib.problem.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import oarlib.objfunc.ObjectiveFunction;
import org.apache.log4j.Logger;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class RuralPostmanProblem<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> extends Problem<V, E, G> {

    private static final Logger LOGGER = Logger.getLogger(RuralPostmanProblem.class);

    protected RuralPostmanProblem(G graph, String name, ObjectiveFunction objFunc) {
        super(graph, name, objFunc);
        boolean isCpp = true;
        for (Link l : graph.getEdges())
            if (!l.isRequired())
                isCpp = false;
        if (isCpp)
            LOGGER.warn("It appears as though every link in this graph is required.  Consider running a Chinese Postman solver.");
    }
}
