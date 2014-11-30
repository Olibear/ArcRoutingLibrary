package oarlib.core;

import gnu.trove.TIntObjectHashMap;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;

import java.util.Collection;
import java.util.List;

/**
 * Graph abstraction.  Provides most general contract for all Graph objects.
 *
 * @author oliverlum
 */
public abstract class Graph<V extends Vertex, E extends Link<V>> {

    private static int graphIdCounter = 1;
    private int vidCounter; //for assigning internal ids of vertices
    private int eidCounter;
    private int graphId;
    private int depotId;

    //TODO: add notion of finalized

    public Graph() {
        vidCounter = 1;
        eidCounter = 1;
        depotId = 1; //default
        assignGraphId();
    }

    protected int assignVertexId() //returns the current vidCounter, and increments
    {
        vidCounter++;
        return vidCounter - 1;
    }

    protected int assignEdgeId() //returns the current eidCounter, and increments
    {
        eidCounter++;
        return eidCounter - 1;
    }

    protected int assignGraphId() {
        graphIdCounter++;
        return graphIdCounter - 1;
    }

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
        depotId = newDepotId;
    }

    /**
     * Getter for the vertices.
     *
     * @return a Collection of vertices belonging to this graph
     */
    public abstract Collection<V> getVertices();

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
     * To query for a specific vertex in the graph, as opposed to having to create another local duplicate map.
     *
     * @param i - the id in the internal map of the graph for the desired vertex
     * @return - the appropriate vertex
     */
    public abstract V getVertex(int i) throws IllegalArgumentException;

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
     * To change the id of a vertex in the graph
     *
     * @param oldId - the old id of the vertex
     * @param newId - the new id of the vertex
     * @throws IllegalArgumentException - if no vertex with oldId exists in the graph, or if there is already a vertex with newId
     */
    public abstract void changeVertexId(int oldId, int newId) throws IllegalArgumentException;

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
     * Looks for edges between the two provided endpoints, and returns them in a collection.
     *
     * @return a collection of edges directly connecting the two vertices
     */
    public abstract List<E> findEdges(Pair<V> endpoints);

    /**
     * @return - a hash map that has ids as keys to the vertices
     */
    public abstract TIntObjectHashMap<V> getInternalVertexMap();

    /**
     * @return - a hash map that has ids as keys to the edges
     */
    public abstract TIntObjectHashMap<E> getInternalEdgeMap();

    /**
     * @return - the type that this graph structure represents
     */
    public abstract Graph.Type getType();

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

    /**
     * Factory method for generating a vertex.
     *
     * @param desc - description for the vertex created
     * @return - a vertex satisfying these properties
     */
    public abstract V constructVertex(String desc);

    public enum Type {
        DIRECTED,
        UNDIRECTED,
        MIXED,
        WINDY
    }
}
