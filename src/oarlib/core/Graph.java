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
package oarlib.core;

import gnu.trove.TIntObjectHashMap;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Graph abstraction.  Provides most general contract for all Graph objects.
 *
 * @author oliverlum
 */
public abstract class Graph<V extends Vertex, E extends Link<V>> {

    private static int graphIdCounter = 1;  //for assigning global ids to graphs
    private int vidCounter; //for assigning internal ids of vertices
    private int eidCounter; //for assigning internal ids of edges
    private int graphId; //id of the graph
    private int depotId; //internal vertex id of the depot
    private int[][] mDist; //shortest paths dist matrix
    private int[][] mPath; //shortest paths matrix

    private boolean distGenerated; //for lazy design pattern; whether or not dist matrix was calculated

    /**
     * Default constructor
     */
    protected Graph() {

        //init
        vidCounter = 1;
        eidCounter = 1;
        depotId = 1; //default
        distGenerated = false;
        assignGraphId();

    }

    //region Constructors
    /**
     * For extending classes to grab a vertex id, presumably to assign to a new vertex
     * @return - the next vertex id available for this graph
     */
    protected int assignVertexId() //returns the current vidCounter, and increments
    {
        vidCounter++;
        return vidCounter - 1;
    }

    /**
     * For extending classes to grab a link id, presumably to assign to a new link
     * @return - the next link id available for this graph
     */
    protected int assignEdgeId() //returns the current eidCounter, and increments
    {
        eidCounter++;
        return eidCounter - 1;
    }

    /**
     * For extending classes to grab an id, presumably to assign to the graph
     * @return - the next available graph id
     */
    protected int assignGraphId() {
        graphIdCounter++;
        return graphIdCounter - 1;
    }
    //endregion


    //region Getters and Setters

    public int getEidCounter() {
        return eidCounter;
    }

    public int getVidCounter() {
        return vidCounter;
    }

    public int getGraphId() {
        return graphId;
    }

    public int getDepotId() {
        return depotId;
    }

    public void setDepotId(int newDepotId) {
        if (newDepotId < 0 || newDepotId > getVertices().size())
            throw new IllegalArgumentException();
        depotId = newDepotId;
    }

    //endregion

    //region shortest paths
    /**
     * Lazy getter for the dist matrix
     *
     * @return
     */
    public int[][] getAllPairsDistMatrix() {
        if (!distGenerated) {
            //generate it
            int n = getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(this, dist, path);

            mDist = dist;
            mPath = path;

            distGenerated = true;
        }

        return mDist;
    }

    /**
     * Lazy getter for the path matrix
     *
     * @return
     */
    public int[][] getAllPairsPathMatrix() {
        if (!distGenerated) {
            int n = getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(this, dist, path);

            mDist = dist;
            mPath = path;

            distGenerated = true;
        }

        return mPath;
    }

    //endregion

    /**
     * Callback for when the graph changes, (e.g. to set a flag that the distance matrix
     * isn't up to date).
     */
    public void onStateChange() {
        distGenerated = false;
    }

    /**
     * Returns whether or not this graph has asymmetric travel costs.
     * Directed and mixed graphs should not return true here even though
     * they could be modeled as a windy graph.  However, any graph that may
     * extend the functionality of a windy graph should return true.
     *
     * @return - true if the network has asymmetric travel distances, false oth.
     */
    public abstract boolean isWindy();

    //region Vertex
    /**
     * Getter for the vertices.
     *
     * @return a Collection of vertices belonging to this graph
     */
    public abstract Collection<V> getVertices();

    /**
     * To add a vertex to the graph.
     *
     * @param v - vertex to be added
     */
    public abstract void addVertex(V v);

    /**
     * To add a vertex to the graph.
     */
    public abstract void addVertex();

    /**
     * To remove a vertex with the specified index from the graph.
     * @param i - the index of the vertex to be removed.
     * @return - true if the vertex was successfully removed, false oth.
     */
    public abstract boolean removeVertex(int i);

    /**
     * To remove a vertex from the graph.
     * @param v - the vertex to be removed.
     * @return - true if the vertex was successfully removed, false oth.
     */
    public abstract boolean removeVertex(V v);

    /**
     * To query for a specific vertex in the graph, as opposed to having to create another local duplicate map.
     *
     * @param i - the id in the internal map of the graph for the desired vertex
     * @return - the appropriate vertex
     */
    public abstract V getVertex(int i) throws IllegalArgumentException;

    /**
     * To change the id of a vertex in the graph
     *
     * @param oldId - the old id of the vertex
     * @param newId - the new id of the vertex
     * @throws IllegalArgumentException - if no vertex with oldId exists in the graph, or if there is already a vertex with newId
     */
    public abstract void changeVertexId(int oldId, int newId) throws IllegalArgumentException;

    /**
     * @return - a hash map that has ids as keys to the vertices
     */
    public abstract TIntObjectHashMap<V> getInternalVertexMap();

    /**
     * Factory method for generating a vertex.
     *
     * @param desc - description for the vertex created
     * @return - a vertex satisfying these properties
     */
    public abstract V constructVertex(String desc);
    //endregion

    //region Link
    /**
     * Getter for the edges.
     *
     * @return a Collection of edges belonging to this graph
     */
    public abstract Collection<E> getEdges();

    /**
     * Erases all edges in the graph, leaving only the vertices behind
     */
    public abstract void clearEdges();

    /**
     * Resets the Edge counter / id assignment to start over.
     */
    protected void resetEdgeCounter() {
        eidCounter = 1;
    }

    /**
     * To add an edge to the graph.  This updates the degrees of the vertices, and throws to the specific implementation of the graph.
     * Throws an InvalidEndpointsException if the endpoints haven't yet been added to the graph.
     *
     * @param e - edge to be added
     */
    public abstract void addEdge(E e) throws InvalidEndpointsException;

    /**
     * A more notationally elegant way of adding an edge to a graph, it will create a new edge from vertex i to vertex j,
     * with the appropriate cost and description.  If this is a mixed graph, it will default to adding an edge, but look for
     * a version that takes directedness as an argument
     *
     * @param i    - add edge from vertex i
     * @param j    - add edge to vertex j
     * @param cost - cost of traversing the edge
     */
    public abstract void addEdge(int i, int j, int cost) throws InvalidEndpointsException;

    /**
     * A more notationally elegant way of adding an edge to a graph, it will create a new edge from vertex i to vertex j,
     * with the appropriate cost and description.  If this is a mixed graph, it will default to adding an edge, but look for
     * a version that takes directedness as an argument
     *
     * @param i          - add edge from vertex i
     * @param j          - add edge to vertex j
     * @param cost       - cost of traversing the edge
     * @param isRequired - whether or not this edge is required in the solution
     */
    public abstract void addEdge(int i, int j, int cost, boolean isRequired) throws InvalidEndpointsException;

    /**
     * A more notationally elegant way of adding an edge to a graph, it will create a new edge from vertex i to vertex j,
     * with the appropriate cost and description.  If this is a mixed graph, it will default to adding an edge, but look for
     * a version that takes directedness as an argument
     *
     * @param i    - add edge from vertex i
     * @param j    - add edge to vertex j
     * @param desc - description for the edge
     * @param cost - cost of traversing the edge
     */
    public abstract void addEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException;

    /**
     * A more notationally elegant way of adding an edge to a graph, it will create a new edge from vertex i to vertex j,
     * with the appropriate cost and description.  If this is a mixed graph, it will default to adding an edge, but look for
     * a version that takes directedness as an argument
     *
     * @param i          - add edge from vertex i
     * @param j          - add edge to vertex j
     * @param desc       - description for the edge
     * @param cost       - cost of traversing the edge
     * @param isRequired - whether or not this edge is required in the solution
     */
    public abstract void addEdge(int i, int j, String desc, int cost, boolean isRequired) throws InvalidEndpointsException;

    /**
     * To remove an edge from the graph.  This updates the degrees of the vertices, and throws to the specific implementation of the graph.
     * Throws an IllegalArgumentException if the edge isn't a member of the edge collection belonging to the graph.
     *
     * @param e - edge to be removed from the graph
     * @throws IllegalArgumentException
     */
    public abstract void removeEdge(E e) throws IllegalArgumentException;

    /**
     * To remove an edge from the graph.  This updates the degrees of the vertices, and throws to the specific implementation of the graph.
     * Throws an IllegalArgumentException if the edge isn't a member of the edge collection belonging to the graph.
     *
     * @param i - index of edge to be removed from the graph
     * @throws IllegalArgumentException
     */
    public abstract void removeEdge(int i) throws IllegalArgumentException;

    /**
     * To query for a specific link in the graph, as opposed to having to create another local duplicate map.
     *
     * @param i - the internal id of the desired edge
     * @throws IllegalArgumentException
     */
    public abstract E getEdge(int i) throws IllegalArgumentException;

    /**
     * To change the id of a link in the graph
     *
     * @param oldId - the old id of the link
     * @param newId - the new id of the link
     * @throws IllegalArgumentException - if no link with oldId exists in the graph, or if there is already a link with newId
     */
    public abstract void changeLinkId(int oldId, int newId) throws IllegalArgumentException;

    /**
     * Looks for edges between the two provided endpoints, and returns them in a collection.
     *
     * @return a collection of edges directly connecting the two vertices
     */
    public abstract List<E> findEdges(Pair<V> endpoints);

    /**
     * Looks for edges between the two provided endpoints, and returns them in a collection.
     *
     * @param v1 - the id of the first endpoint
     * @param v2 - the id of the second endpoint
     * @return - a collection of edges directly connecting the two vertices
     */
    public List<E> findEdges(int v1, int v2) {
        return findEdges(new Pair<V>(getVertex(v1), getVertex(v2)));
    }

    /**
     * @return - a hash map that has ids as keys to the edges
     */
    public abstract TIntObjectHashMap<E> getInternalEdgeMap();

    /**
     * Factory method for generating an edge.
     *
     * @param i    - first endpoint index for the edge created
     * @param j    - second endpoint index for the edge created
     * @param desc - description of edge to be created
     * @param cost - cost of edge to be created
     * @return - an instance of an edge satisfying these properties
     * @throws InvalidEndpointsException - if i or j > number of vertices
     */
    public abstract E constructEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException;
    //endregion

    /**
     * Provides a means of getting a by value copy of this graph.  The conventions used to produce this
     * copy are slightly idiosyncratic in order to make this method slightly more useful:
     * <p/>
     * -The vertices are copied in a straight-forward manner.  That is, vertex i in the original graph
     * ends up as vertex i in the copy.  The match id of the vertex in the copy will be i also.  If demands
     * are set, they are also copied.
     * <p/>
     * -The edges are collapsed to produce consecutive ids.  Thus, if the original graph had edges with ids
     * 1, 3, and 6, the copy will have three edges, with ids 1, 2, 3 respectively.  The new match ids are set to
     * 1, 3, and 6 to retain information about where they originated from.  Properties (required-ness, and
     * capacity) also transfer.
     *
     * @return - a deep copy of the graph
     */
    public abstract Graph<V, E> getDeepCopy();

    /**
     * @return - the type that this graph structure represents
     */
    public abstract Graph.Type getType();

    public enum Type {
        DIRECTED,
        UNDIRECTED,
        MIXED,
        WINDY
    }
}
