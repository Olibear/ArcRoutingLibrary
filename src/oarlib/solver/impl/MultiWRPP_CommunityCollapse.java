package oarlib.solver.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.display.GraphDisplay;
import oarlib.graph.factory.impl.WindyGraphFactory;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.impl.EdgeInducedRequirementTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.auxiliary.PartitioningProblem;
import oarlib.problem.impl.io.PartitionFormat;
import oarlib.problem.impl.io.PartitionReader;
import oarlib.problem.impl.io.ProblemFormat;
import oarlib.problem.impl.io.ProblemWriter;
import oarlib.problem.impl.multivehicle.MinMaxKWRPP;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteExpander;
import oarlib.route.util.SolutionImporter;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by oliverlum on 3/17/16.
 */
public class MultiWRPP_CommunityCollapse extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(MultiWRPP_CollapseSimple.class);
    private HashMap<Integer, HashSet<Integer>> packMap;
    private double alpha;
    private int mNumPartitions;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWRPP_CommunityCollapse(Problem<WindyVertex, WindyEdge, WindyGraph> instance) throws IllegalArgumentException {
        super(instance);
        packMap = new HashMap<Integer, HashSet<Integer>>();
        alpha = .5;
        mNumPartitions = 20;
    }

    public MultiWRPP_CommunityCollapse(Problem<WindyVertex, WindyEdge, WindyGraph> instance, double alphaWeight, int numParts) throws IllegalArgumentException {
        super(instance);
        packMap = new HashMap<Integer, HashSet<Integer>>();
        alpha = alphaWeight;
        mNumPartitions = numParts;
    }

    @Override
    protected boolean checkGraphRequirements() {
        //make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            WindyGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }

    @Override
    protected Problem<WindyVertex, WindyEdge, WindyGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<? extends Route> solve() {

        WindyGraph mGraph = mInstance.getGraph();

        //identify communities
        //key = vertexId
        //value = partition #
        HashMap<Integer, Integer> communities = identifyCommunitiesMETIS(mGraph, mNumPartitions);

        WindyGraph collapsedGraph = collapseGraph(communities);

        //solve exactly
        //key = edgeId
        //value = partition #
        HashSet<HashSet<Integer>> subans = callCorberan(collapsedGraph);

        HashMap<Integer, Integer> vPart = processVertices(subans, collapsedGraph);
        HashMap<Integer, Integer> ePart = processEdges(subans);

        /*try {
            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, collapsedGraph, "CollapsedCorberanSolve");
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, ePart);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }*/

        //expand into partition
        HashMap<Integer, HashSet<Integer>> finalPartitions = expand(vPart, ePart, collapsedGraph.getVertices().size());

        //route
        ArrayList<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();
        for (HashSet<Integer> partition : finalPartitions.values()) {
            ans.add(route(partition));
        }

        getInstance().setSol(ans);

        display(ans);

        return ans;
    }

    private HashMap<Integer, Integer> processVertices(HashSet<HashSet<Integer>> routes, WindyGraph collpaseGraph) {

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();

        HashMap<Integer, HashSet<Integer>> vParts = new HashMap<Integer, HashSet<Integer>>();
        int id1, id2;
        int part = 1;
        for (HashSet<Integer> r : routes) {
            for (Integer i : r) {
                WindyEdge we = collpaseGraph.getEdge(i);
                id1 = we.getFirstEndpointId();
                id2 = we.getSecondEndpointId();

                if (!vParts.containsKey(id1))
                    vParts.put(id1, new HashSet<Integer>());
                if (!vParts.containsKey(id2))
                    vParts.put(id2, new HashSet<Integer>());

                vParts.get(id1).add(part);
                vParts.get(id2).add(part);
            }
            part++;
        }

        //randomly assign
        Random rng = new Random(1);
        int num, toAdd = -1;
        for (Integer i : vParts.keySet()) {
            if (vParts.get(i).size() == 1)
                ans.put(i, vParts.get(i).iterator().next());
            else {
                num = rng.nextInt(vParts.get(i).size()) + 1;
                Iterator<Integer> iter = vParts.get(i).iterator();
                for (int j = 0; j < num; j++)
                    toAdd = iter.next();
                ans.put(i, toAdd);
            }
        }

        return ans;
    }

    private HashMap<Integer, Integer> processEdges(HashSet<HashSet<Integer>> routes) {

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();
        int j = 1;
        for (HashSet<Integer> r : routes) {
            for (Integer i : r) {
                ans.put(i, j);
            }
            j++;
        }

        return ans;
    }

    private HashMap<Integer, Integer> identifyCommunitiesMETIS(WindyGraph g, int n) {
        //use METIS to create n partitions, and collapse those

        try {

            //generate the vertex dual
            /*WindyGraph mGraph = mInstance.getGraph();
            int[][] dist = mGraph.getAllPairsDistMatrix();
            ClosestRequiredEdgeRebalancer<WindyGraph> beta = new ClosestRequiredEdgeRebalancer<WindyGraph>(mGraph, new WindyGraphFactory(), 1, new IndividualDistanceToDepotRebalancer(mGraph, 1));
            beta.setDistMatrix(dist);
            DuplicateEdgeCostRebalancer costRebalancer = new DuplicateEdgeCostRebalancer(mGraph, beta);

            //initialize transformer for turning edge-weighted grpah into vertex-weighted graph
            PreciseWindyKWayPartitionTransform transformer = new PreciseWindyKWayPartitionTransform(mGraph, true, costRebalancer);

            //transform the graph
            WindyGraph vWeightedTest = transformer.transformGraph();*/

            //for METIS
            for (WindyVertex wv : g.getVertices()) {
                wv.setCost(1);
            }

            //run METIS on the graph

            String filename = "C:\\Users\\Oliver\\Desktop\\RandomGraph.graph";

            //write it to a file
            ProblemWriter gw = new ProblemWriter(ProblemFormat.Name.METIS);
            gw.writeInstance(new PartitioningProblem(g, null, null), filename);

            //partition the graph
            runMetis(n, filename);

            //now read the partition and reconstruct the induced subrgraphs on which we solve the WPP to get our final solution
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);

            HashMap<Integer, Integer> ans = pr.readPartition(filename + ".part." + n);
            return ans;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private HashMap<Integer, Integer> removeHolesInHashMap(HashMap<Integer, Integer> input) {

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();
        HashSet<Integer> uniqueVals = new HashSet<Integer>();

        ArrayList<Integer> values = new ArrayList<Integer>();
        for (Integer val : input.values()) {
            if (!uniqueVals.contains(val)) {
                uniqueVals.add(val);
                values.add(val);
            }
        }

        HashMap<Integer, Integer> backMap = new HashMap<Integer, Integer>();
        Collections.sort(values);

        for (int i = 0; i < values.size(); i++) {
            backMap.put(values.get(i), i);
        }

        for (Integer key : input.keySet()) {
            ans.put(key, backMap.get(input.get(key)));
        }

        return ans;
    }

    private void display(Collection<Route<WindyVertex, WindyEdge>> record) {

        try {
            int counter = 1;
            HashMap<Integer, Integer> sol = new HashMap<Integer, Integer>();

            for (Route<WindyVertex, WindyEdge> r : record) {

                List<WindyEdge> tempPath = r.getPath();
                ArrayList<Boolean> service = r.getServicingList();
                for (int i = 0; i < tempPath.size(); i++) {
                    if (service.get(i))
                        sol.put(tempPath.get(i).getId(), counter);
                }
                counter++;
            }

            WindyGraph toDisplay = mInstance.getGraph().getDeepCopy();
            int limi = mInstance.getGraph().getEdges().size();
            for (int i = 1; i <= limi; i++) {
                WindyEdge we = toDisplay.getEdge(i);
                if (!sol.containsKey(we.getId()))
                    toDisplay.removeEdge(we.getId());
            }

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toDisplay, mInstance.getName());
            gd.setInstanceName("Collapse_" + mInstance.getName());
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, sol);

            //individual routes
            for (Route<WindyVertex, WindyEdge> r : record) {

                gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toDisplay, mInstance.getName());
                float[] scaling = getScaling();
                gd.setScaling(scaling[0], scaling[1], scaling[2], scaling[3]);

                if (r == null)
                    System.out.println();
                gd.setInstanceName("Collapse_" + mInstance.getName() + "_" + r.getCost());
                gd.exportRoute(GraphDisplay.ExportType.PDF, r);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    protected float[] getScaling() {

        //For the display
        WindyVertex tempV;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MAX_VALUE;
        float maxY = Float.MAX_VALUE;
        int n = mInstance.getGraph().getVertices().size();
        for (int i = 1; i <= n; i++) {
            tempV = mInstance.getGraph().getVertex(i);

            if (tempV.getX() > maxX)
                maxX = (float) tempV.getX();
            if (tempV.getX() < minX)
                minX = (float) tempV.getX();


            if (tempV.getY() > maxY)
                maxY = (float) tempV.getY();
            if (tempV.getY() < minY)
                minY = (float) tempV.getY();
        }

        float xRange = (maxX - minX);
        float yRange = (maxY - minY);
        float xScaleFactor = 100f / xRange * 100;
        float yScaleFactor = 100f / yRange * 100;

        float[] ans = new float[4];
        ans[0] = xScaleFactor;
        ans[1] = yScaleFactor;
        ans[2] = minX;
        ans[3] = minY;

        return ans;

    }

    private WindyGraph collapseGraph(HashMap<Integer, Integer> communities) {

        HashMap<Integer, Integer> refinedCommunities = removeHolesInHashMap(communities);

        WindyGraph ans = new WindyGraph();
        HashSet<Integer> partitions = new HashSet<Integer>();
        WindyGraph mGraph = mInstance.getGraph();

        HashMap<Integer, ArrayList<Double>> xCoords = new HashMap<Integer, ArrayList<Double>>();
        HashMap<Integer, ArrayList<Double>> yCoords = new HashMap<Integer, ArrayList<Double>>();

        //add vertices, one per partition
        int index = 1;
        for (Integer i : refinedCommunities.values()) {
            if (!partitions.contains(i)) {
                partitions.add(i);
                ans.addVertex();
                packMap.put(index, new HashSet<Integer>());
                index++;
            }
        }

        try {

            //set up the depot
            ans.setDepotId(refinedCommunities.get(mGraph.getDepotId()) + 1);

            int n = ans.getVertices().size();
            WindyEdge toUpdate;
            int i, j;
            for (WindyEdge we : mGraph.getEdges()) {

                i = refinedCommunities.get(we.getFirstEndpointId()) + 1;
                j = refinedCommunities.get(we.getSecondEndpointId()) + 1;
                if (i == j) {
                    packMap.get(i).add(we.getId());
                } else {
                    if (ans.findEdges(i, j) == null || ans.findEdges(i, j).size() == 0) {
                        packMap.put(n + ans.getEidCounter(), new HashSet<Integer>());
                        packMap.get(n + ans.getEidCounter()).add(we.getId());
                        ans.addEdge(i, j, we.getCost(), we.getReverseCost(), we.isRequired());

                        xCoords.put(i, new ArrayList<Double>());
                        xCoords.get(i).add(we.getEndpoints().getFirst().getX());

                        xCoords.put(j, new ArrayList<Double>());
                        xCoords.get(j).add(we.getEndpoints().getSecond().getX());

                        yCoords.put(i, new ArrayList<Double>());
                        yCoords.get(i).add(we.getEndpoints().getFirst().getY());

                        yCoords.put(j, new ArrayList<Double>());
                        yCoords.get(j).add(we.getEndpoints().getSecond().getY());


                    } else {
                        toUpdate = ans.findEdges(i, j).iterator().next();
                        toUpdate.setCost(toUpdate.getCost() + we.getCost());
                        toUpdate.setReverseCost(toUpdate.getReverseCost() + we.getReverseCost());
                        if (we.isRequired())
                            toUpdate.setRequired(true);
                        packMap.get(n + toUpdate.getId()).add(we.getId());
                        xCoords.get(i).add(we.getEndpoints().getFirst().getX());
                        xCoords.get(j).add(we.getEndpoints().getSecond().getX());
                        yCoords.get(i).add(we.getEndpoints().getFirst().getY());
                        yCoords.get(j).add(we.getEndpoints().getSecond().getY());
                    }
                }
            }

            for (Integer xKey : xCoords.keySet()) {
                double x = 0;
                double y = 0;
                for (double xCoord : xCoords.get(xKey))
                    x += xCoord;
                for (double yCoord : yCoords.get(xKey))
                    y += yCoord;
                ans.getVertex(xKey).setCoordinates(x / xCoords.get(xKey).size(), y / yCoords.get(xKey).size());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return ans;
    }

    private int getLowerBound(WindyGraph g) {
        //bad lower bound for now, but it's something fast
        int sum = 0;
        for (WindyEdge we : g.getEdges()) {
            if (we.isRequired())
                sum += we.getCost() + we.getServiceCost();
            if (we.isReverseRequired())
                sum += we.getReverseCost() + we.getReverseServiceCost();
        }
        return sum;
    }

    private HashSet<HashSet<Integer>> callCorberan(WindyGraph g) {

        if (CommonAlgorithms.isConnected(g))
            System.out.println();
        try {
            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, "collapsed");
            gd.export(GraphDisplay.ExportType.PDF);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        HashSet<HashSet<Integer>> ans = new HashSet<HashSet<Integer>>();

        //write the graph to a .txt in Corberan format
        try {
            ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
            MinMaxKWRPP prob = new MinMaxKWRPP(g, "collapsed", mInstance.getmNumVehicles());
            pw.writeInstance(prob, "/Users/oliverlum/Downloads/bnc2/collapsed.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //call the Corberan code
        try {

            ProcessBuilder pb = new ProcessBuilder("sudo", "/Users/oliverlum/Downloads/bnc2/a.out", "/Users/oliverlum/Downloads/bnc2/collapsed.txt", Integer.toString(mInstance.getmNumVehicles()), Double.toString(alpha), Integer.toString(getLowerBound(g)), "4");
            Process run = pb.start();

            OutputStream stdin = run.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            String input = "X\n";
            writer.write(input);
            writer.flush();

            System.out.println("Running Corberan Solver");
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));

            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);

            String line;
            while ((line = bfr.readLine()) != null) {
                System.out.println("Corberan Output: " + line);
            }

            System.out.println("Complete");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //parse the output
        Collection<Tour<DirectedVertex, Arc>> collapsedRoutes = SolutionImporter.importRoutes("/Users/oliverlum/Downloads/bnc2/collapsed_" + mInstance.getmNumVehicles() + "_3_" + Integer.toString((int) (alpha * 100)) + "_op.txt", g.getDepotId(), SolutionImporter.RouteFormat.CORBERAN);
        HashSet<Tour> finalCollapsedRoutes = SolutionImporter.mapToGraph(g, collapsedRoutes);

        HashSet<Integer> toAdd;
        for (Tour t : finalCollapsedRoutes) {
            toAdd = new HashSet<Integer>();
            for (int i : t.getCompactRepresentation().toNativeArray())
                toAdd.add(i);
            ans.add(toAdd);
        }

        return ans;
    }

    private HashMap<Integer, HashSet<Integer>> expand(HashMap<Integer, Integer> vParts, HashMap<Integer, Integer> eParts, int n) {

        //key = part #
        //value = set of edge ids in mGraph
        HashMap<Integer, HashSet<Integer>> ans = new HashMap<Integer, HashSet<Integer>>();

        for (int i = 1; i <= mInstance.getmNumVehicles(); i++) {
            ans.put(i, new HashSet<Integer>());
        }

        for (Integer i : vParts.keySet()) {
            ans.get(vParts.get(i)).addAll(packMap.get(i));
        }

        for (Integer i : eParts.keySet()) {
            ans.get(eParts.get(i)).addAll(packMap.get(n + i));
        }

        return ans;
    }

    private Tour<WindyVertex, WindyEdge> route(HashSet<Integer> edges) {

        WindyGraph mGraph = mInstance.getGraph();
        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedRequirementTransform<WindyGraph> subgraphTransform = new EdgeInducedRequirementTransform<WindyGraph>(mGraph, wgf, edges);

        //check to make sure we have at least 1 required edge
        TIntObjectHashMap<WindyEdge> mEdges = mGraph.getInternalEdgeMap();
        boolean hasReq = false;
        for (Integer i : edges) {
            if (mEdges.get(i).isRequired())
                hasReq = true;
        }
        if (!hasReq)
            return new Tour();

        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it

        //to use Rui's exact solver, we have to write the instance, and then call the python
        try {
            String filename = "/Users/oliverlum/Downloads/20node/WPPTZ20nodes_15_15_15.txt";
            WindyRPP subProblem = new WindyRPP(subgraph);
            ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Zhang_Matrix_Windy);
            pw.writeInstance(subProblem, filename);

            return runIP(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Tour<WindyVertex, WindyEdge> runIP(String inputFileName) {

        WindyGraph mGraph = mInstance.getGraph();

        TIntArrayList ansRoute = new TIntArrayList();
        ArrayList<Boolean> ansDir = new ArrayList<Boolean>();
        ArrayList<Boolean> ansZig = new ArrayList<Boolean>();
        File outputFile = new File("MTZ_NoTimeWindows__DetailRoute.txt");
        if (outputFile.exists())
            outputFile.delete();

        //run the python script which calls CPLEX
        try {

            ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/WRPP_BnC.py");
            Process run = pb.start();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line: " + line);
            while ((line = bfr.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
            System.out.println("Complete");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //parse the output so we can get the final solution back
        try {
            String line;
            String type = "";
            String[] temp = new String[1];
            String[] temp2 = new String[1];
            String[] temp3 = new String[1];


            BufferedReader br = new BufferedReader(new FileReader(outputFile));
            WindyEdge tempL;

            while ((line = br.readLine()) != null) {
                if (line.contains("Simplified Route")) {
                    line = br.readLine(); //the meat
                    temp = line.split("'");
                    continue;
                }
                if (line.contains("Action Route")) {
                    line = br.readLine();
                    temp2 = line.split("'");
                    break;
                }
            }

            int v1, v2;
            for (int l = 0; l < temp.length; l++) {
                if (!temp[l].contains("_"))
                    continue;
                temp3 = temp[l].split("_");
                v1 = Integer.parseInt(temp3[0]) + 1;
                v2 = Integer.parseInt(temp3[1]) + 1;

                if (v1 == 1)
                    v1 = mGraph.getDepotId();
                else if (v1 == mGraph.getDepotId())
                    v1 = 1;

                if (v2 == 1)
                    v2 = mGraph.getDepotId();
                else if (v2 == mGraph.getDepotId())
                    v2 = 1;

                List<WindyEdge> candidates = mGraph.findEdges(v1, v2);
                if (candidates.size() > 1)
                    LOGGER.warn("There appear to be multiple edges connecting" +
                            " these two vertices.  Behavior is not guaranteed" +
                            " to be correct.");

                tempL = candidates.get(0);

                //id
                ansRoute.add(tempL.getId());

                //dir
                if (v1 == tempL.getFirstEndpointId()) {
                    ansDir.add(true);
                } else {
                    ansDir.add(false);
                }

                //zig
                if (temp2[l].contains("Z")) {
                    ansZig.add(true);
                } else {
                    ansZig.add(false);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        RouteExpander<WindyGraph> we = new RouteExpander<WindyGraph>(mGraph);
        return we.unflattenRoute(ansRoute, ansDir);
    }

    public void setAlpha(double newAlpha) throws IllegalArgumentException {
        if (newAlpha < 0 || newAlpha > 1)
            throw new IllegalArgumentException("Alpha must be set to a value between 0 and 1, (inclusive).");
        alpha = newAlpha;
    }

    public void setNumPartitions(int newNumPartitions) throws IllegalArgumentException {
        if (newNumPartitions <= 1 || newNumPartitions > mInstance.getGraph().getVertices().size())
            throw new IllegalArgumentException("The number of partitions must be greater than 1, and fewer than the number of vertices in the graph.");
        mNumPartitions = newNumPartitions;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Community Collapse Aesthetic Min-Max K Windy Rural Postman Solver";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPP_CommunityCollapse(p, alpha, mNumPartitions);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        return ret;
    }
}
