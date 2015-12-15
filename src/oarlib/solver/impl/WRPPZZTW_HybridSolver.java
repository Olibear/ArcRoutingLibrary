package oarlib.solver.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.*;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.graph.util.UnmatchedPair;
import oarlib.graph.util.Utils;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by oliverlum on 10/2/15.
 */
public class WRPPZZTW_HybridSolver extends SingleVehicleSolver<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static final Logger LOGGER = Logger.getLogger(WRPPZZTW_PFIH.class);

    //4 weights on the cost for tuning later
    private double latePenalty; //if you are late 1 unit of time, this costs you latePenalty extra cost units

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public WRPPZZTW_HybridSolver(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> instance) throws IllegalArgumentException {
        this(instance, 1, 1, 1, 1, 1);
    }

    public WRPPZZTW_HybridSolver(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> instance, double alpha, double beta, double gamma, double lambda, double penalty) throws IllegalArgumentException {
        super(instance);
        setLatePenalty(penalty);
    }

    private Route<ZigZagVertex, ZigZagLink> solveIP(TIntArrayList partialRoute) {
        //TODO
        return null;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            UndirectedGraph connectedCheck = new UndirectedGraph(mInstance.getGraph().getVertices().size());
            try {
                for (ZigZagLink l : mInstance.getGraph().getEdges())
                    connectedCheck.addEdge(l.getFirstEndpointId(), l.getSecondEndpointId(), 1);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (!CommonAlgorithms.isConnected(connectedCheck))
                return false;
        }
        return true;
    }

    @Override
    protected Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> getInstance() {
        return mInstance;
    }

    /**
     * Runs a push-first insertion heuristic for the WRPP with zig-zags and time windows.
     *
     * @return
     */
    @Override
    protected Collection<? extends Route> solve() {

        int numIterations = 5;
        int numCandidateSeeds = 10;

        //initialize ans.
        int bestCost = Integer.MAX_VALUE;
        Collection<Tour> Sbest = new ArrayList<Tour>();

        for (int iter = 0; iter < numIterations; iter++) {

            Collection<Tour> S = new ArrayList<Tour>();

            ZigZagGraph g = mInstance.getGraph();
            int n = g.getVertices().size();

            //calculate least cost paths
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(g, dist, path);

            //zero out the diagonal
            for (int i = 1; i <= n; i++) {
                dist[i][i] = 0;
            }

            //find out how many we have to serve
            int mReq = 0;
            HashSet<Integer> req = new HashSet<Integer>();
            PriorityQueue<UnmatchedPair<Integer, Double>> toServe = new PriorityQueue<UnmatchedPair<Integer, Double>>(1, new Utils.PFIHComparator());

            for (ZigZagLink zzl : g.getEdges()) {
                if (zzl.isRequired() || zzl.isReverseRequired()) {
                    mReq++;
                    req.add(zzl.getId());
                }
            }

            //route construction
            TIntArrayList compactAns = new TIntArrayList();
            ArrayList<Boolean> compactDir = new ArrayList<Boolean>();

            //pick a seed
            int firstId = seedRoute(req, dist, numCandidateSeeds);

            toServe.remove(firstId);
            req.remove(firstId);
            compactAns.add(firstId);
            compactDir.add(true);

            ZigZagLink temp;
            for (Integer i : req) {
                temp = g.getEdge(i);
                //toServe.add(new UnmatchedPair<Integer, Double>(temp.getId(), assessPFIHCost(temp, dist)));
            }

            //while you still need to service edges...
            while (!toServe.isEmpty()) {
                ZigZagLink toRoute = g.getEdge(toServe.poll().getFirst());
                PriorityQueue<Pair<Integer>> moves = cheapestInsertion(compactAns, compactDir, toRoute, dist);
                if (moves.isEmpty()) {
                    LOGGER.warn("No feasible moves exist.");
                    break;
                } else {
                    makeMove(moves, compactAns, compactDir, toRoute, dist);
                }
            }

            //ZigZagTour r = determineZZ(compactAns, compactDir, dist);
            //S.add(r);

            int currCost = 0;//r.getCost();
            LOGGER.info("This candidate route costs: " + currCost);

            if (currCost < bestCost) {
                Sbest = S;
                bestCost = currCost;
            }
        }

        LOGGER.info("The best route costs: " + bestCost);
        return Sbest;
    }

    /**
     * Perform the insertion specified.
     *
     * @param moveList     - a list of candidate moves, from which the cheapest feasible one (or
     *                     if none exist, then the least violating infeasible one) will be chosen.
     * @param compactRoute - the compact route representation of the route to be modified (that is,
     *                     the ordered list of required ids which it services, with shortest paths
     *                     between them assumed).
     * @param compactDir   - the companion to the compact route representation which specifies whether
     *                     or not the required edge in the same position of compactRoute is traversed
     *                     forward (true) or backwards (false)
     * @param toRoute      - the link to be added
     * @param dist         - the distance matrix for the graph (all pairs; memory saver)
     */
    private void makeMove(PriorityQueue<Pair<Integer>> moveList, TIntArrayList compactRoute, ArrayList<Boolean> compactDir, ZigZagLink toRoute, int[][] dist) {

        ZigZagGraph g = mInstance.getGraph();
        Pair<Integer> move = moveList.poll();
        int index = move.getFirst().intValue();

        compactRoute.insert(index, toRoute.getId());

        //if we're constrained (meaning must service in one direction with no zig-zag available), then add in the appropriate direction
        //if(toRoute.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
        if (!toRoute.isRequired()) {
            compactDir.add(move.getFirst().intValue(), false);
            return;
        } else if (!toRoute.isReverseRequired()) {
            compactDir.add(move.getFirst().intValue(), true);
            return;
        }
        //}


        //figure out which direction is cheaper
        int prevIndex = index - 1;
        int nextIndex = index + 1;

        int index1 = -1;
        int index2 = -1;

        //figure out the first index
        if (prevIndex == -1)
            index1 = mInstance.getGraph().getDepotId();
        else if (compactDir.get(prevIndex))
            index1 = g.getEdge(compactRoute.get(prevIndex)).getSecondEndpointId();
        else
            index1 = g.getEdge(compactRoute.get(prevIndex)).getFirstEndpointId();

        //figure out the second index
        if (index >= compactDir.size())
            index2 = mInstance.getGraph().getDepotId();
        else if (compactDir.get(index))
            index2 = g.getEdge(compactRoute.get(nextIndex)).getFirstEndpointId();
        else
            index2 = g.getEdge(compactRoute.get(nextIndex)).getSecondEndpointId();

        //figure out the cheaper dir
        int dist1 = dist[index1][toRoute.getFirstEndpointId()] + dist[toRoute.getSecondEndpointId()][index2] + toRoute.getCost();
        int dist2 = dist[index1][toRoute.getSecondEndpointId()] + dist[toRoute.getFirstEndpointId()][index2] + toRoute.getReverseCost();
        if (dist1 < dist2)
            compactDir.add(move.getFirst().intValue(), true);
        else
            compactDir.add(move.getFirst().intValue(), false);

    }

    /**
     * Computes the insertion cost for the operation involving 3 elements:
     * These can be any combination of edges and vertices, and this returns the
     * cheapest possible insertion move subject to the stipulated constraints
     *
     * @param vertexOrLink - array of size 3 specifying whether the id numbers passed in
     *                     in ids are for vertices or links in the graph. A value of 0
     *                     means the id in that position of ids is for a vertex.  A value
     *                     of 1 means the id in that position of ids is for a link.  For
     *                     example, if vertexOrLink = [0,1,0] and ids = [1,2,3], then that
     *                     would mean that we consider the insertion of edge 2 in between
     *                     vertices 1 and 3.
     * @param ids          - the ids of the elements to be considered in the insertion.  For example,
     *                     if ids = [1,2,3], then this function considers the insertion of element 2
     *                     in between elements 1 and 3.
     * @param dist         - the all pairs shortest path matrix for the graph (memory saver)
     * @return - A pair of integers: the first is the cost of insertion;
     * the second is either
     * <p/>
     * -1 if the element to be inserted (the middle one) if a vertex,
     * 0  if the element to be inserted is a link, and should be traversed forward to get this cost
     * 1  if the element to be inserted is a link, and should be traversed backward to get this cost
     * @throws IllegalArgumentException - If ids.length or vertexOrLink.length != 3, or if vertexOrLink contains
     *                                  entries that are not 0 or 1.
     */
    private Pair<Integer> insertCost(int[] vertexOrLink, int[] ids, int[][] dist) throws IllegalArgumentException {

        //arg check
        if (vertexOrLink.length != 3 || ids.length != 3) {
            LOGGER.error("You are attempting to fetch an insertion cost for an operation with more than 3 elements.");
            throw new IllegalArgumentException("Please use arrays of size 3 for this operation.");
        }

        Pair<Integer> ans = new Pair<Integer>(-1, -1);

        ZigZagGraph g = mInstance.getGraph();
        ZigZagLink temp;
        int e11, e12, e21, e22, e31, e32;
        if (vertexOrLink[0] == 0) {
            e11 = ids[0];
            e12 = -1;
        } else if (vertexOrLink[0] == 1) {
            temp = g.getEdge(ids[0]);
            e11 = temp.getFirstEndpointId();
            e12 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        if (vertexOrLink[1] == 0) {
            e21 = ids[1];
            e22 = -1;
        } else if (vertexOrLink[1] == 1) {
            temp = g.getEdge(ids[1]);
            e21 = temp.getFirstEndpointId();
            e22 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        if (vertexOrLink[2] == 0) {
            e31 = ids[2];
            e32 = -1;
        } else if (vertexOrLink[2] == 1) {
            temp = g.getEdge(ids[2]);
            e31 = temp.getFirstEndpointId();
            e32 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        int min = Integer.MAX_VALUE;
        int minDir = -1;
        int orientation = -1;
        //try out the various possibilities

        int candidate;
        //if we're inserting a vertex
        if (vertexOrLink[1] == 0) {

            //case 1
            candidate = dist[e11][e21] + dist[e21][e31] - dist[e11][e31];
            if (candidate < min) {
                min = candidate;
            }
            //case 2
            if (e12 > 0) {
                candidate = dist[e12][e21] + dist[e21][e31] - dist[e21][e31];
                if (candidate < min) {
                    min = candidate;
                }
            }
            //case 3
            if (e32 > 0) {
                candidate = dist[e11][e21] + dist[e21][e32] - dist[e11][e32];
                if (candidate < min) {
                    min = candidate;
                }
            }
            //case 4
            if (e32 > 0 && e12 > 0) {
                candidate = dist[e12][e21] + dist[e21][e32] - dist[e12][e32];
                if (candidate < min) {
                    min = candidate;
                }
            }
        } else {

            temp = g.getEdge(ids[1]);

            //case 1
            candidate = dist[e11][e21] + dist[e22][e31] - dist[e11][e31] + temp.getCost() + temp.getServiceCost();
            if (candidate < min) {
                min = candidate;
                minDir = 0;
            }
            candidate = dist[e11][e22] + dist[e21][e31] - dist[e11][e31] + temp.getReverseCost() + temp.getReverseServiceCost();
            if (candidate < min) {
                min = candidate;
                minDir = 1;
            }

            //case 2
            if (e12 > 0) {
                candidate = dist[e12][e21] + dist[e22][e31] - dist[e12][e31] + temp.getCost() + temp.getServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                candidate = dist[e12][e22] + dist[e21][e31] - dist[e12][e31] + temp.getReverseCost() + temp.getReverseServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
            //case 3
            if (e32 > 0) {
                candidate = dist[e11][e21] + dist[e22][e32] - dist[e11][e32] + temp.getCost() + temp.getServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                candidate = dist[e11][e22] + dist[e21][e32] - dist[e11][e32] + temp.getReverseCost() + temp.getReverseServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
            //case 4
            if (e32 > 0 && e12 > 0) {
                candidate = dist[e12][e21] + dist[e22][e32] - dist[e12][e32] + temp.getCost() + temp.getServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                candidate = dist[e12][e22] + dist[e21][e32] - dist[e12][e32] + temp.getReverseCost() + temp.getReverseServiceCost();
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
        }

        ans.setFirst(min);
        ans.setSecond(minDir);
        return ans;

    }

    private PriorityQueue<Pair<Integer>> cheapestInsertion(TIntArrayList compactRoute, ArrayList<Boolean> compactDir, ZigZagLink toRoute, int[][] dist) {

        //the graph
        ZigZagGraph g = mInstance.getGraph();
        int n = g.getVertices().size();

        //go through and find the cheapest insertion location

        //first order by standard insertion formula
        int maxi = compactRoute.size() - 1;
        PriorityQueue<Pair<Integer>> moves = new PriorityQueue<Pair<Integer>>(1, new Utils.DijkstrasComparator());
        ZigZagLink temp1, temp2;
        boolean dir1, dir2;
        int insertionCost;
        int startTime = 0;
        int addedTime = 0;
        double penalty = 0; //measures how much the proposed move violates time windows

        //calculate costs for inserting as the first element
        temp1 = g.getEdge(compactRoute.get(0));
        dir1 = compactDir.get(0);
        int depotId = g.getDepotId();
        int[] vertexOrLink = new int[3];
        vertexOrLink[0] = 0;
        vertexOrLink[1] = 1;
        vertexOrLink[2] = 0;
        int[] ids = new int[3];
        ids[0] = depotId;
        ids[1] = toRoute.getId();
        ids[2] = temp1.getFirstEndpointId();


        if (dir1) {
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[depotId][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
            startTime += dist[depotId][temp1.getFirstEndpointId()];
        } else {
            ids[2] = temp1.getSecondEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[depotId][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
            startTime += dist[depotId][temp1.getSecondEndpointId()];
        }

        if (startTime + insertionCost > g.getEdge(compactRoute.get(0)).getTimeWindow().getSecond())
            penalty += latePenalty * (startTime + insertionCost - g.getEdge(compactRoute.get(0)).getTimeWindow().getSecond());

        //check feasibility of the rest of the moves
        boolean violated = false;
        int addedTime2 = 0;
        for (int j = 1; j < maxi + 1; j++) {

            dir1 = compactDir.get(j - 1);
            dir2 = compactDir.get(j);
            temp1 = g.getEdge(compactRoute.get(j - 1));
            temp2 = g.getEdge(compactRoute.get(j));

            if (dir1 && dir2) {
                addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
            } else if (dir1 && !dir2) {
                addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
            } else if (dir2) {
                addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];
            } else {
                addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
            }

            if (startTime + insertionCost + addedTime2 > g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + insertionCost + addedTime2 - g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond());
            }
        }
        moves.add(new Pair<Integer>(0, insertionCost + (int) penalty));

        //iterate through the list
        for (int i = 0; i < maxi; i++) {

            penalty = 0;
            temp1 = g.getEdge(compactRoute.get(i));
            temp2 = g.getEdge(compactRoute.get(i + 1));
            dir1 = compactDir.get(i);
            dir2 = compactDir.get(i + 1);

            if (dir1 && dir2) {

                ids[0] = temp1.getSecondEndpointId();
                ids[2] = temp2.getFirstEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 1).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
            } else if (dir1 && !dir2) {

                ids[0] = temp1.getSecondEndpointId();
                ids[2] = temp2.getSecondEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 1).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
            } else if (dir2) {

                ids[0] = temp1.getFirstEndpointId();
                ids[2] = temp2.getFirstEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 3).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];

            } else {

                ids[0] = temp1.getFirstEndpointId();
                ids[2] = temp2.getSecondEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 3).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
            }

            //check feasibility of the rest of the moves
            violated = false;
            addedTime2 = 0;
            for (int j = i + 1; j < maxi + 1; j++) {

                dir1 = compactDir.get(j - 1);
                dir2 = compactDir.get(j);
                temp1 = g.getEdge(compactRoute.get(j - 1));
                temp2 = g.getEdge(compactRoute.get(j));

                if (dir1 && dir2) {
                    addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
                } else if (dir1 && !dir2) {
                    addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
                } else if (dir2) {
                    addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];
                } else {
                    addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
                }

                if (startTime + insertionCost + addedTime2 > g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + insertionCost + addedTime2 - g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond());
            }

            moves.add(new Pair<Integer>(i + 1, insertionCost + (int) penalty));

        }

        //last insertion spot
        ids[2] = depotId;
        penalty = 0;
        temp2 = g.getEdge(compactRoute.get(maxi));
        dir2 = compactDir.get(maxi);
        if (dir2) {
            ids[0] = temp2.getSecondEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[temp2.getSecondEndpointId()][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
        } else {
            ids[0] = temp2.getFirstEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[temp2.getFirstEndpointId()][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
        }
        moves.add(new Pair<Integer>(maxi + 1, insertionCost + (int) penalty));

        return moves;
    }

    /**
     * Seeds the route for the WRPPZZTW solution.
     *
     * @param req      - contains ids of the required edges in the graph
     * @param dist     - the distance matrix (memory saver)
     * @param lenience - n, where the routine will produce the id of one of
     *                 the n required edges furthest from the depot
     * @return - iterates through the required edges and returns one randomly from the
     * n edges furthest from the depot, (n = lenience).
     */
    private int seedRoute(HashSet<Integer> req, int[][] dist, int lenience) throws IllegalArgumentException {

        if (lenience <= 0)
            throw new IllegalArgumentException("Lenience must be at least 1.");
        if (lenience > req.size()) {
            LOGGER.warn("Lenience is greater than the number of required edges, setting lenience to be the size of req.");
            lenience = req.size();
        }

        Random rng = new Random(1000);
        PriorityQueue<Pair<Integer>> pq = new PriorityQueue<Pair<Integer>>(req.size(), new Utils.InverseDijkstrasComparator());
        ZigZagGraph g = mInstance.getGraph();
        int depotId = g.getDepotId();
        int max = Integer.MIN_VALUE;
        int maxi = -1;
        int j, k;

        ZigZagLink temp;

        for (Integer i : req) {

            temp = g.getEdge(i);
            j = temp.getFirstEndpointId();
            k = temp.getSecondEndpointId();
            max = -2;

            if (dist[depotId][k] > max) {
                max = dist[depotId][k];
            }
            if (dist[depotId][j] > max) {
                max = dist[depotId][j];
            }

            pq.add(new Pair<Integer>(i, max));

        }

        int rank = rng.nextInt(lenience) + 1;

        int ans = -1;
        for (int i = 1; i <= rank; i++) {
            ans = pq.poll().getFirst();
        }

        return ans;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }

    @Override
    public String getSolverName() {
        return "Hybrid IP-Heuristic for the Windy Rural Postman Problem with Time Windows";
    }

    @Override
    public Solver<ZigZagVertex, ZigZagLink, ZigZagGraph> instantiate(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> p) {
        return new WRPPZZTW_PFIH(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        return new HashMap<String, Double>();
    }

    public double getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(double latePenalty) {
        this.latePenalty = latePenalty;
    }
}
