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
package oarlib.simulation;

import com.sun.jdi.event.WatchpointEvent;
import oarlib.core.Route;
import oarlib.graph.util.Pair;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.RFIDARP;
import oarlib.vertex.impl.WindyVertex;
import org.apache.xpath.operations.Bool;

import java.util.*;

/**
 * Created by oliverlum on 9/2/16.
 *
 * Class in charge of generating a realization of successfully read meters given a route and an instance of the
 * RFID-ARP
 */
public class InfoCollectingRealizer {

    RFIDARP mInstance; // problem instance, with read probabilities and meter locations

    public InfoCollectingRealizer(RFIDARP prob, DistributionGenerator gen){
        mInstance = prob;
    }

    public HashMap<Integer, Boolean> realizeSimulation(Route<WindyVertex, WindyEdge> route) {

        //init
        HashMap<Integer, Boolean> ans = new HashMap<Integer, Boolean>();

        //simulate
        ArrayList<WindyEdge> path = route.getPath();
        WindyEdge tempEdge;
        int tempId;
        double readProb;
        Set<Integer> meterIds = mInstance.getMeters().keySet();
        HashMap<Pair<Integer>, Double> readProbs =  mInstance.getReadProbabilities();
        Random rng = new Random(1000);

        HashSet<Integer> readMeters = new HashSet<Integer>();

        for(int i = 0; i < path.size(); i++) {
            tempEdge = path.get(i);
            tempId = tempEdge.getId();

            //go through and check the probabilities for each meter
            for(Integer meterId : meterIds){
                readProb = readProbs.get(new Pair<Integer>(tempId, meterId));
                if(rng.nextDouble() < readProb)
                    readMeters.add(meterId);
            }
        }

        for(Integer meterId : meterIds) {
            if(readMeters.contains(meterId))
                ans.put(meterId, true);
            else
                ans.put(meterId, false);
        }
        //ret
        return ans;
    }



}
