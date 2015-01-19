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
package oarlib.improvements;

import oarlib.core.*;
import oarlib.problem.impl.ProblemAttributes;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 11/16/14.
 */
public abstract class ImprovementProcedure<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> {

    private static final Logger LOGGER = Logger.getLogger(ImprovementProcedure.class);
    protected ImprovementStrategy.Type mStrat;
    private G mGraph;
    private Collection<Route<V, E>> mInitialSol;
    private Problem<V, E, G> mProblem;

    protected ImprovementProcedure(Problem<V, E, G> instance) {
        this(instance, null, null);
    }

    protected ImprovementProcedure(Problem<V, E, G> instance, ImprovementStrategy.Type strat) {
        this(instance, strat, null);
    }

    protected ImprovementProcedure(Problem<V, E, G> instance, ImprovementStrategy.Type strat, Collection<Route<V, E>> initialSol) {

        boolean err = false;
        Collection<Route<V, E>> candidateSol;

        if (initialSol == null)
            candidateSol = instance.getSol();
        else
            candidateSol = initialSol;

        if (strat == null)
            mStrat = ImprovementStrategy.Type.FirstImprovement;

        G g = instance.getGraph();

        //check preconditions
        if (!(getProblemAttributes().isCompatibleWith(instance.getProblemAttributes()))) {
            LOGGER.error("The problem instance is type incompatible with this improvement procedure");
            err = true;
        }
        if (candidateSol == null) {
            LOGGER.error("The solution you passed in is null; perhaps the instance has not yet been solved.");
            err = true;
        }
        if (candidateSol.size() == 0) {
            LOGGER.error("The solution you passed in is empty.");
            err = true;
        }
        if (g.getVertices().size() == 0 || g.getEdges().size() == 0) {
            LOGGER.error("The problem graph seems to be trivial, and does not permit non-empty routes.");
            err = true;
        }

        if (err)
            throw new IllegalArgumentException();

        mGraph = g;
        mInitialSol = candidateSol;
        mProblem = instance;
        mStrat = strat;

    }

    public abstract ProblemAttributes getProblemAttributes();

    protected Collection<Route<V, E>> getInitialSol() {
        return mInitialSol;
    }

    protected G getGraph() {
        return mGraph;
    }

    protected Problem<V, E, G> getProblem() {
        return mProblem;
    }

    public abstract Collection<Route<V, E>> improveSolution();

}
