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
package oarlib.problem.impl.rpp;

import oarlib.core.Graph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.Pair;
import oarlib.link.impl.WindyEdge;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.WindyVertex;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 9/2/16.
 *
 * The RFID Arc Routing Problem
 */
public class RFIDARP extends RuralPostmanProblem<WindyVertex, WindyEdge, WindyGraph> {

    HashMap<Integer, Pair<Double>> meters = new HashMap<Integer, Pair<Double>>(); //key = id; value = coordinates
    HashMap<Pair<Integer>, Double> readProbabilities = new HashMap<Pair<Integer>, Double>(); //key = edgeId, meterId; value = prob [0,1]

    public RFIDARP(WindyGraph g) {
        this(g, "");
    }

    public RFIDARP(WindyGraph g, String name) {
        super(g, name, new SumMetric());
        mGraph = g;
    }

    public HashMap<Integer, Pair<Double>> getMeters(){
        return meters;
    }

    public void setMeters(HashMap<Integer, Pair<Double>> newMeters) {
        meters = newMeters;
    }

    public void putMeter(int id, Pair<Double> coordinates) {
        meters.put(id, coordinates);
    }

    public HashMap<Pair<Integer>, Double> getReadProbabilities(){
        return readProbabilities;
    }

    public void setReadProbabilities(HashMap<Pair<Integer>, Double> newProbabilities) {
        readProbabilities = newProbabilities;
    }

    public void removeMeter(int meterId) {
        if(!meters.keySet().contains(meterId))
            throw new IllegalArgumentException("You are attempting to remove a meter that doesn't exist");
        meters.remove(meterId);
    }

    public void setReadProbability(int edgeId, int meterId, double newProb){

        if(newProb<0 || newProb>1)
            throw new IllegalArgumentException("The probability you provided was invalid.");
        readProbabilities.put(new Pair<Integer>(edgeId, meterId), newProb);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
