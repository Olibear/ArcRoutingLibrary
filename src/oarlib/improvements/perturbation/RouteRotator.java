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
package oarlib.improvements.perturbation;

import oarlib.core.*;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.Pair;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.AsymmetricLink;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.solver.impl.WRPPSolver_Benavent_H1;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/20/16.
 */
public class RouteRotator {

    /**
     * Takes the existing solution (routes) and rotates the centers of the partitions, reassigning customers to the new closest
     * partition center, and using solver to route each of the resulting routes.
     * @param g - the graph in which the current solution lives.
     * @param routes - original set of routes
     * @param angle - how much (in radians, between -pi/2 to pi/2) to rotate
     * @return - the new routes
     */
    public static Collection<Route<WindyVertex,WindyEdge>> rotateRoutes(
            WindyGraph g, Collection<Route<WindyVertex,WindyEdge>> routes, double angle) {

        if(routes.size() == 1)
            return routes;

        ArrayList<Route<WindyVertex,WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();

        //find the partition centers
        ArrayList<Pair<Double>> partitionCenters = new ArrayList<Pair<Double>>();
        for(Route<WindyVertex,WindyEdge> r: routes) {

            ArrayList<Pair<Double>> customerCoords = new ArrayList<Pair<Double>>();
            ArrayList<WindyEdge> path = r.getPath();
            ArrayList<Boolean> service = r.getServicingList();

            for(int i = 0; i < path.size(); i++){
                if(service.get(i)) {
                    customerCoords.add(path.get(i).getEndpoints().getFirst().getCoordinates());
                    customerCoords.add(path.get(i).getEndpoints().getSecond().getCoordinates());
                }
            }

            partitionCenters.add(Utils.findCenter(customerCoords));
        }

        //find overall center
        ArrayList<Pair<Double>> vertCoords = new ArrayList<Pair<Double>>();
        for(WindyVertex v : g.getVertices()) {
            vertCoords.add(v.getCoordinates());
        }
        //Pair<Double> center = Utils.findCenter(partitionCenters);
        Pair<Double> center = Utils.findCenter(vertCoords);

        //find the new partition centers
        ArrayList<Pair<Double>> newPartitionCenters = new ArrayList<Pair<Double>>();
        for(Pair<Double> pc : partitionCenters) {
            double dist = Utils.geoDistance(center, pc);
            double currAngle = Utils.findAngle(center, pc);
            double newAngle = currAngle + angle;
            double yDiff = Math.sin(newAngle) * dist;
            double xDiff = Math.cos(newAngle) * dist;
            newPartitionCenters.add(new Pair<Double>(center.getFirst() + xDiff, center.getSecond() + yDiff));
        }


        //assign customers to the closest new partition
        HashMap<Integer, HashSet<Integer>> newPartitions = new HashMap<Integer, HashSet<Integer>>();

        int closestIndex;
        double closestDist;
        for(WindyEdge e : g.getEdges()) {
            closestIndex = -1;
            closestDist = Double.MAX_VALUE;
            if(e.isRequired() || ((AsymmetricLink)e).isReverseRequired()) {
                for(int i = 0; i < newPartitionCenters.size(); i++) {
                    Pair<Double> pc = newPartitionCenters.get(i);
                    double dist = Utils.geoDistance(pc, new Pair<Double>((e.getEndpoints().getFirst().getX() + e.getEndpoints().getSecond().getX())*.5,(e.getEndpoints().getFirst().getY() + e.getEndpoints().getSecond().getY())*.5));
                    if(dist < closestDist) {
                        closestIndex = i;
                        closestDist = dist;
                    }
                }

                if(!newPartitions.containsKey(closestIndex))
                    newPartitions.put(closestIndex, new HashSet<Integer>());
                newPartitions.get(closestIndex).add(e.getId());
            }
        }

        //create a graph for each of the new partitions, and route it
        for(Integer part : newPartitions.keySet()) {
            WindyGraph newG = g.getDeepCopy();
            for(WindyEdge e : newG.getEdges()) {
                if(e.isRequired() || ((AsymmetricLink)e).isReverseRequired()) {
                    if(!newPartitions.get(part).contains(e.getId())) {
                        e.setRequired(false);
                        ((AsymmetricLink)e).setReverseRequired(false);
                    }
                }
            }

            try {

                WindyRPP tempProb = new WindyRPP(newG);
                WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(tempProb);
                solver.trySolve();
                for(Route r : tempProb.getSol())
                    ans.add(r);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }


        return ans;
    }

}
