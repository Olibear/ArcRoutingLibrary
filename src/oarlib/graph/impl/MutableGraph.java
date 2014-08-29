package oarlib.graph.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.InvalidEndpointsException;

import java.util.HashMap;
import java.util.HashSet;

/**
 * A mutable graph.  In the future, plans are to optimize performance by writing a finalized one with
 * immutable data structures that have faster access time.
 *
 * @author Oliver
 */
public abstract class MutableGraph<V extends Vertex, E extends Link<V>> extends Graph<V, E> {

    private HashSet<V> mVertices;
    private HashSet<E> mEdges;
    private HashMap<Integer, V> mGlobalVertexMap; //indexed by guids
    private HashMap<Integer, V> mInternalVertexMap; //indexed by ids
    private HashMap<Integer, E> mGlobalEdgeMap;
    private HashMap<Integer, E> mInternalEdgeMap;

    protected MutableGraph() {
        super();
        mVertices = new HashSet<V>();
        mEdges = new HashSet<E>();
        mGlobalVertexMap = new HashMap<Integer, V>();
        mInternalVertexMap = new HashMap<Integer, V>();
        mGlobalEdgeMap = new HashMap<Integer, E>();
        mInternalEdgeMap = new HashMap<Integer, E>();
        this.assignGraphId();
    }

    protected MutableGraph(int n) {
        this();
        for (int i = 0; i < n; i++)
            this.addVertex(this.constructVertex("from constructor"));
    }

    protected MutableGraph(int n, int depotId) {
        this(n);
        this.setDepotId(depotId);
    }

    //=====================================
    //
    // Graph Overrides With Match Ids
    //
    //=====================================
    public void addEdge(E e, int matchId) throws InvalidEndpointsException {
        this.addEdge(e);
        e.setMatchId(matchId);
    }

    public void addEdge(int i, int j, String desc, int cost, int matchId) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setMatchId(matchId);
        this.addEdge(temp);
    }

    public void addEdge(int i, int j, String desc, int cost, int matchId, boolean isReq)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setRequired(isReq);
        temp.setMatchId(matchId);
        this.addEdge(temp);
    }

    public void addEdge(int i, int j, int cost, int matchId) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, "", cost);
        temp.setMatchId(matchId);
        this.addEdge(temp);
    }

    public void addVertex(V v, int matchId) {
        this.addVertex(v);
        v.setMatchId(matchId);
        v.setFinalized(true);
    }

    public void addVertex(int matchId) {
        this.addVertex(this.constructVertex(""), matchId);
    }

    //==============================
    //
    // Graph overrides
    //
    //==============================

    @Override
    public HashSet<V> getVertices() {
        return mVertices;
    }

    @Override
    public HashSet<E> getEdges() {
        return mEdges;
    }

    @Override
    public void clearEdges() {
        mEdges = new HashSet<E>();
        mInternalEdgeMap = new HashMap<Integer, E>();
        mGlobalEdgeMap = new HashMap<Integer, E>();
        super.resetEdgeCounter();
    }

    @Override
    public void addEdge(E e) throws InvalidEndpointsException {
        // enforce endpoints being added first
        if (!mVertices.contains(e.getEndpoints().getFirst()) || !mVertices.contains(e.getEndpoints().getSecond()))
            throw new InvalidEndpointsException();
        e.setId(this.assignEdgeId());
        e.setGraphId(this.getGraphId());
        mEdges.add(e);
        mGlobalEdgeMap.put(e.getGuid(), e);
        mInternalEdgeMap.put(e.getId(), e);
        e.setFinalized(true);
    }

    @Override
    public void addEdge(int i, int j, int cost) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        this.addEdge(this.constructEdge(i, j, "", cost));
    }

    @Override
    public void addEdge(int i, int j, int cost, boolean isReq) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        this.addEdge(i, j, "", cost, isReq);
    }

    @Override
    public void addEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        this.addEdge(this.constructEdge(i, j, desc, cost));
    }

    @Override
    public void addEdge(int i, int j, String desc, int cost, boolean isReq)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size())
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setRequired(isReq);
        this.addEdge(temp);
    }

    @Override
    public void changeLinkId(int oldId, int newId) throws IllegalArgumentException {
        if (!mInternalEdgeMap.containsKey(oldId))
            throw new IllegalArgumentException("No link with the oldId specified exists in this graph.");
        if (mInternalEdgeMap.containsKey(newId))
            throw new IllegalArgumentException("A link with newId already exists in this graph.");

        E temp = mInternalEdgeMap.get(oldId);
        mInternalEdgeMap.remove(oldId);
        temp.setId(newId);
        mInternalEdgeMap.put(newId, temp);

    }

    @Override
    public void addVertex(V v) {
        v.setId(this.assignVertexId());
        v.setGraphId(this.getGraphId());
        mVertices.add(v);
        mGlobalVertexMap.put(v.getGuid(), v);
        mInternalVertexMap.put(v.getId(), v);
    }

    @Override
    public void addVertex() {
        this.addVertex(this.constructVertex(""));
    }


    @Override
    public void changeVertexId(int oldId, int newId) throws IllegalArgumentException {
        if (!mInternalVertexMap.containsKey(oldId))
            throw new IllegalArgumentException("No vertex with the oldId specified exists in this graph.");
        if (mInternalVertexMap.containsKey(newId))
            throw new IllegalArgumentException("A vertex with newId already exists in this graph.");

        V temp = mInternalVertexMap.get(oldId);
        mInternalVertexMap.remove(oldId);
        temp.setId(newId);
        mInternalVertexMap.put(newId, temp);
    }

    @Override
    public void removeEdge(E e) {
        mEdges.remove(e);
        mGlobalEdgeMap.remove(e.getGuid());
        mInternalEdgeMap.remove(e.getId());
        e.setFinalized(false);
    }

    @Override
    public HashMap<Integer, V> getGlobalVertexMap() {
        return mGlobalVertexMap;
    }

    @Override
    public HashMap<Integer, V> getInternalVertexMap() {
        return mInternalVertexMap;
    }

    @Override
    public HashMap<Integer, E> getGlobalEdgeMap() {
        return mGlobalEdgeMap;
    }

    @Override
    public HashMap<Integer, E> getInternalEdgeMap() {
        return mInternalEdgeMap;
    }
}
