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

import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.Pair;
import oarlib.link.impl.ZigZagLink;
import oarlib.vertex.impl.ZigZagVertex;

/**
 * Created by oliverlum on 03/08/16.
 */
public class ZigzagRectangularGraphGenerator extends RectangularGraphGenerator<ZigZagGraph> {

    public ZigzagRectangularGraphGenerator(long seed) {
        super(seed);
    }

    @Override
    protected ZigZagGraph generate(int n, int maxCost, double reqDensity, boolean positiveCosts) {
        return generate(n, maxCost, maxCost * 10, reqDensity, positiveCosts);
    }

    public ZigZagGraph generate(int n, int maxCost, int timeWindow, double reqDensity, boolean positiveCosts) {

        //trivial case
        if (n == 1)
            return new ZigZagGraph(1);

        ZigZagGraph ans = new ZigZagGraph((int) Math.pow(n, 2));
        double interval = 100.0 / (n - 1);

        int index = 1;
        int cost, revCost, coeff;
        int rngCeiling = maxCost;
        boolean zzable;

        try {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    //modify properties
                    ZigZagVertex newV = ans.getVertex(index);
                    newV.setLabel((j + 1) + "," + (i + 1));
                    newV.setCoordinates(j * interval, i * interval);

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                        revCost = rng.nextInt(rngCeiling) + 1;
                    } else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                        revCost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }

                    //add horizontal edges
                    if (j > 0) {

                        ZigZagLink toAdd = ans.constructEdge(index, index - 1, "", Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0, 0, ZigZagLink.ZigZagStatus.NOT_AVAILABLE);

                        //traversal costs
                        toAdd.setCost(cost);
                        toAdd.setmReverseCost(revCost);

                        //service costs
                        if (rng.nextDouble() < reqDensity) {
                            toAdd.setServiceCost(rng.nextInt(rngCeiling) + 1);
                            toAdd.setReverseServiceCost(rng.nextInt(rngCeiling) + 1);
                        }


                        //zigzag cost
                        zzable = rng.nextDouble() < .5;
                        if (toAdd.isRequired() || toAdd.isReverseRequired())
                            if (zzable)
                                toAdd.setZigzagCost((toAdd.getCost() + toAdd.getReverseCost()) * 1.3);

                        //zigzag status
                        if (toAdd.isRequired() || toAdd.isReverseRequired()) {
                            if (zzable)
                                toAdd.setStatus(ZigZagLink.ZigZagStatus.OPTIONAL);
                            else
                                toAdd.setStatus(ZigZagLink.ZigZagStatus.NOT_AVAILABLE);
                        }

                        //zigzag time window
                        if (toAdd.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL) {
                            if (rng.nextDouble() < .75)
                                toAdd.setTimeWindow(new Pair<Integer>(0, timeWindow));
                        }

                        ans.addEdge(toAdd);

                    }

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                        revCost = rng.nextInt(rngCeiling) + 1;
                    } else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                        revCost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }
                    //add vertical edges
                    if (i > 0) {

                        ZigZagLink toAdd = ans.constructEdge(index, index - n, "", Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0, 0, ZigZagLink.ZigZagStatus.NOT_AVAILABLE);

                        //traversal costs
                        toAdd.setCost(cost);
                        toAdd.setmReverseCost(revCost);

                        //service costs
                        if (rng.nextDouble() < reqDensity) {
                            toAdd.setServiceCost(rng.nextInt(rngCeiling) + 1);
                            toAdd.setReverseServiceCost(rng.nextInt(rngCeiling) + 1);
                        }


                        //zigzag cost
                        zzable = rng.nextDouble() < .5;
                        if (toAdd.isRequired() || toAdd.isReverseRequired())
                            if (zzable)
                                toAdd.setZigzagCost((toAdd.getCost() + toAdd.getReverseCost()) * 1.3);

                        //zigzag status
                        if (toAdd.isRequired() || toAdd.isReverseRequired()) {
                            if (zzable)
                                toAdd.setStatus(ZigZagLink.ZigZagStatus.OPTIONAL);
                            else
                                toAdd.setStatus(ZigZagLink.ZigZagStatus.NOT_AVAILABLE);
                        }

                        //zigzag time window
                        if (toAdd.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL) {
                            if (rng.nextDouble() < .75)
                                toAdd.setTimeWindow(new Pair<Integer>(0, timeWindow));
                        }

                        ans.addEdge(toAdd);
                    }

                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ans;
    }
}
