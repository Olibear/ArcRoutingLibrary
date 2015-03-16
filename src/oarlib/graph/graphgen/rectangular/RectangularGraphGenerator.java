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
package oarlib.graph.graphgen.rectangular;

import oarlib.core.Graph;
import org.apache.log4j.Logger;

import java.util.Random;

/**
 * Created by oliverlum on 12/14/14.
 */
public abstract class RectangularGraphGenerator<S extends Graph> {

    private static final Logger LOGGER = Logger.getLogger(RectangularGraphGenerator.class);
    protected static Random rng = new Random(1000);
    private double defaultDensity;

    public RectangularGraphGenerator() {
        this(1000);
    }

    public RectangularGraphGenerator(long seed) {
        rng.setSeed(seed);
        defaultDensity = rng.nextDouble();
    }

    /**
     * Check inputs to the graph generator methods
     *
     * @param n       - number of vertices in the graph
     * @param maxCost - the maximum (magnitude) that the cost of a link in this graph can have
     * @param density - the approximate density of the graph, must be between [0,1]
     */
    private static void checkArgs(int n, int maxCost, double density) {
        boolean problem = false;
        if (n <= 0) {
            LOGGER.error("Cannot create a graph with negative number of vertices.");
            problem = true;
        }
        if (density < 0 || density > 1) {
            LOGGER.error("Invalid density.");
            problem = true;
        }
        if (maxCost < 0) {
            LOGGER.error("maxCost is a cardinality argument; it should not be signed.");
            problem = true;
        }
        if (problem)
            throw new IllegalArgumentException();

    }

    /**
     * Randomly generates a square graph with n^2 vertices.
     *
     * @param n - the number of vertices in one row of the graph
     * @return - a rectangular graph with n^2 vertices.
     */
    public S generateGraph(int n) {
        return this.generateGraph(n, 10, defaultDensity);
    }

    /**
     * Randomly generates a square graph with n^2 vertices, and max edge cost of maxCost.
     *
     * @param n       - the number of vertices in one row of the graph
     * @param maxCost - the maximum (magnitude) that the cost of a link in this graph can have
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost) {
        return this.generateGraph(n, maxCost, defaultDensity);
    }

    /**
     * Randomly generates a square graph with n^2 vertices, and max edge cost of maxCost.
     *
     * @param n          - number of vertices in the graph
     * @param maxCost    - the maximum (magnitude) that the cost of a link in this graph can have
     * @param reqDensity - the probability that a link will be required in the graph.
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, double reqDensity) {
        return this.generateGraph(n, maxCost, reqDensity, false);
    }

    /**
     * Randomly generates an Erdos-Renyi graph with n vertices, and max edge cost of maxCost.
     * If the connected flag is true, then we ensure that the number (strongly) connected components is 1.
     * The density specified will be the probability that a link from vertex i to vertex j is included.
     *
     * @param n             - number of vertices in the graph
     * @param maxCost       - the maximum (magnitude) that the cost of a link in this graph can have
     * @param reqDensity    - the probability that a link will be required in the graph.
     * @param positiveCosts - flag to enforce whether all edge weights are strictly positive.
     * @return - a graph with the specified properties.
     */
    public S generateGraph(int n, int maxCost, double reqDensity, boolean positiveCosts) {
        checkArgs(n, maxCost, reqDensity);
        return this.generate(n, maxCost, reqDensity, positiveCosts);
    }

    protected abstract S generate(int n, int maxCost, double reqDensity, boolean positiveCosts);
}
