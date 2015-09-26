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
package oarlib.bmgt831;

import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Edge;
import oarlib.vertex.impl.UndirectedVertex;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 3/26/15.
 */
public class TruckAndDroneFeasibilityChecker {


    private static String formatWrong = "The input file specified does not appear to be generated from OAR Lib.  The parser will \n" +
            "still try to read the file, but ensures no guarantees about its performance.  Please re-run this on the \n" +
            "original input file distributed.";
    private String mInputPath;
    private String mOutputPath;
    private UndirectedGraph mGraph;
    private int mFleetSize;
    private double mAlpha;
    private int mDronesPerVehicle;
    private int mFlightDuration;
    private int mTruckCap;
    private int mDepotID;

    public TruckAndDroneFeasibilityChecker(String inputPath, String outputPath) {

        mInputPath = inputPath;
        mOutputPath = outputPath;
    }

    public boolean checkFeasible() {

        //parse the input file to grab the relevant parameters and create the graph
        if (!readInputFile())
            return false;

        //parse the output file to see if the described route is feasible
        if (!routeFeasible())
            return false;


        return true;
    }

    private boolean routeFeasible() {

        File outFile = new File(mOutputPath);
        FileReader outReader;

        try {
            outReader = new FileReader(outFile);
        } catch (FileNotFoundException e) {
            System.out.println("The output file specified does not appear to exist at this file location.");
            e.printStackTrace();
            return false;
        }

        BufferedReader br = new BufferedReader(outReader);
        String line;
        String[] temp;

        int m = mGraph.getEdges().size();
        HashSet<Integer> servedCustomers = new HashSet<Integer>();

        double waitPeriod;
        int load;
        int currId = -1;
        int returnId;
        boolean routeExists;
        boolean validMove = false;
        UndirectedVertex currentVertex;
        Edge toTraverse = null;
        HashMap<Integer, Double> costMap = new HashMap<Integer, Double>();
        HashMap<String, Pair<Integer>> deployedDrones = new HashMap<String, Pair<Integer>>();
        HashMap<String, Double> droneSync = new HashMap<String, Double>();

        //need to compute shortest paths first
        int n = mGraph.getVertices().size();
        int[][] dist = new int[n + 1][n + 1];
        int[][] path = new int[n + 1][n + 1];

        CommonAlgorithms.fwLeastCostPaths(mGraph, dist, path);

        try {
            for (int routeCounter = 1; routeCounter <= mFleetSize; routeCounter++) {
                //init
                load = 0;
                currentVertex = null;
                routeExists = false;

                //put it
                costMap.put(routeCounter, 0.0);

                while ((line = br.readLine()) != null && !line.equals("END ROUTE")) {
                    //another route exists, process it
                    routeExists = true;

                    //Logic for drone deploy
                    if (line.contains("DEPLOY")) {

                        //check to make sure we've still got space
                        if (deployedDrones.keySet().size() == mDronesPerVehicle) {
                            System.out.println("You have already deployed the maximum number of drones for route " + routeCounter + ". Please make sure the format is correct.");
                            return false;
                        }

                        //check to make sure we've still got cargo
                        if (load == mTruckCap) {
                            System.out.println("You are attempting to launch a drone when your truck's cargo has already been exhausted.  Please make sure the format is correct.");
                            return false;
                        }


                        //we're offloading a package
                        load++;

                        temp = line.split("\\(|,|\\)");


                        //check to make sure this isn't already deployed
                        if (deployedDrones.containsKey(temp[1])) {
                            System.out.println("You are attempting to deploy a drone which has not yet returned to the vehicle. Please make sure the format is correct.");
                            return false;
                        }

                        //add the customer to the list of served customers
                        int customerId = Integer.parseInt(temp[2]);
                        if (servedCustomers.contains(customerId)) {
                            System.out.println("You are sending a drone to service a customer that has already been serviced.  Please make sure the format is correct.");
                            return false;
                        }

                        servedCustomers.add(customerId);

                        //add drone to list of deployed drones, with start point, and customer to service
                        deployedDrones.put(temp[1], new Pair<Integer>(currId, customerId));

                        //add drone to list where we keep a running tally of the truck
                        droneSync.put(temp[1], 0.0);
                    } else if (line.contains("WAIT")) {
                        temp = line.split("\\(|,|\\)");

                        //how many time units to add
                        waitPeriod = Double.parseDouble(temp[1]);

                        //add the wait to this route's cost
                        costMap.put(routeCounter, costMap.get(routeCounter) + waitPeriod);

                        //add the wait to each of the sync times
                        for (String s : droneSync.keySet()) {
                            droneSync.put(s, droneSync.get(s) + waitPeriod);
                        }
                    } else if (line.contains("RETURN")) {
                        temp = line.split("\\(|,|\\)");

                        returnId = currId;

                        String nameOfReturningDrone = temp[1];

                        //compare the truck time (sync time) with the shortest path from start to service to the current id,
                        //and make the appropriate adjustments
                        double droneTime = 0;
                        Pair<Integer> droneInfo = deployedDrones.get(nameOfReturningDrone);
                        droneTime += dist[droneInfo.getFirst()][droneInfo.getSecond()];
                        droneTime += dist[droneInfo.getSecond()][returnId];
                        droneTime = (droneTime * mAlpha);

                        double truckTime = droneSync.get(nameOfReturningDrone);

                        //if the drone gets there first
                        if (droneTime <= truckTime) {
                            if (truckTime > mFlightDuration + .0001) {
                                System.out.println("A drone's flight exceeded the max duration.  Please make sure the format is correct.");
                                return false;
                            }
                        } else if (truckTime + .0001 < droneTime) {
                            System.out.println("A truck arrived at a vertex, and did not wait enough time for a drone that was scheduled to return." +
                                    "  Please make sure the format is correct.");
                            return false;
                        }
                        deployedDrones.remove(nameOfReturningDrone);
                        droneSync.remove(nameOfReturningDrone);

                    } else {

                        temp = line.split(",");
                        //just a vertex id
                        currId = Integer.parseInt(temp[0]);

                        //check for connectivity
                        if (currentVertex == null) {
                            //check for depot
                            if (currId != mGraph.getDepotId()) {
                                System.out.println("The first id listed in the route doesn't seem to be the depot.  Please make sure the format is correct.");
                                return false;
                            }
                            currentVertex = mGraph.getVertex(currId);
                        } else {
                            //check for connectedness
                            for (UndirectedVertex uv : currentVertex.getNeighbors().keySet()) {
                                if (uv.getId() == currId) {
                                    validMove = true;
                                    toTraverse = currentVertex.getNeighbors().get(uv).get(0);
                                    break;
                                }
                            }
                            if (!validMove) {
                                System.out.println("There doesn't seem to be a connection between " + currentVertex.getId() + " and " + currId + ".  Please make sure the format is correct.");
                                return false;
                            }
                            currentVertex = mGraph.getVertex(currId);

                            //add the cost
                            costMap.put(routeCounter, costMap.get(routeCounter) + toTraverse.getCost());
                            for (String s : droneSync.keySet()) {
                                droneSync.put(s, droneSync.get(s) + toTraverse.getCost());
                            }

                        }

                        temp = line.split(",");
                        if (Boolean.parseBoolean(temp[1]))
                            servedCustomers.add(currId);
                    }
                }

                //make sure we returned
                if (currId != mGraph.getDepotId() && routeExists) {
                    System.out.println("The last id listed in the route doesn't seem to be the depot.  Please make sure the format is correct.");
                    return false;
                }

                System.out.println("The cost of route " + routeCounter + " is: " + costMap.get(routeCounter));
            }

            //make sure everybody got served
            if (servedCustomers.size() != n - 1) {
                System.out.println("It does not appear that all the customers were served.  Please make sure the format is correct.");
                return false;
            }

            double max = Integer.MIN_VALUE;
            for (Integer key : costMap.keySet())
                if (costMap.get(key) > max)
                    max = costMap.get(key);

            System.out.println("Total objective value: " + max);
            br.close();
        } catch (IOException e) {
            System.out.println("There was a problem (IO) reading the problem file.  Please make sure that the format is correct.");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean readInputFile() {

        File inFile = new File(mInputPath);
        FileReader inReader;

        try {
            inReader = new FileReader(inFile);
        } catch (FileNotFoundException e) {
            System.out.println("The input file specified does not appear to exist at this file location.");
            e.printStackTrace();
            return false;
        }

        BufferedReader br = new BufferedReader(inReader);


        String line;
        int n = -1;
        int m = -1;
        try {
            br.readLine();
            line = br.readLine();

            if (!line.contains("This is a file generated by the Open Source, Arc-Routing Library (OAR Lib)."))
                System.out.println(formatWrong);
            while ((line = br.readLine()) != null) {
                if (line.contains("Fleet Size (k)"))
                    mFleetSize = Integer.parseInt(line.split(":")[1]);
                else if (line.contains("Alpha"))
                    mAlpha = Double.parseDouble(line.split(":")[1]);
                else if (line.contains("Drones Per Vehicle"))
                    mDronesPerVehicle = Integer.parseInt(line.split(":")[1]);
                else if (line.contains("Drone Flight Duration (t_drone)"))
                    mFlightDuration = Integer.parseInt(line.split(":")[1]);
                else if (line.contains("Truck Capacity (cap_truck)"))
                    mTruckCap = Integer.parseInt(line.split(":")[1]);
                else if (line.contains("Depot ID(s)"))
                    mDepotID = Integer.parseInt(line.split(":")[1]);
                else if (line.contains("N:"))
                    n = Integer.parseInt(line.split(":|\\.")[1]);
                else if (line.contains("M:"))
                    m = Integer.parseInt(line.split(":|\\.")[1]);
                else if (line.contains("LINKS"))
                    break;
            }

            if (n == -1 || m == -1)
                System.out.println(formatWrong);

            br.readLine();
            mGraph = new UndirectedGraph(n);
            mGraph.setDepotId(mDepotID);

            //add links
            String[] temp;
            try {
                for (int i = 1; i <= m; i++) {
                    line = br.readLine();
                    temp = line.split(",");
                    mGraph.addEdge(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), Integer.parseInt(temp[2]));
                }
            } catch (InvalidEndpointsException e) {
                System.out.println("The links section could not be successfully processed.  Please make sure that the format is correct.");
                e.printStackTrace();
                br.close();
                return false;
            }

            br.close();
            return true;
        } catch (IOException e) {
            System.out.println("There was a problem (IO) reading the problem file.  Please make sure that the format is correct.");
            e.printStackTrace();
            return false;
        }
    }

    public String getmInputPath() {
        return mInputPath;
    }

    public void setmInputPath(String mInputPath) {
        this.mInputPath = mInputPath;
    }

    public String getmOutputPath() {
        return mOutputPath;
    }

    public void setmOutputPath(String mOutputPath) {
        this.mOutputPath = mOutputPath;
    }
}
