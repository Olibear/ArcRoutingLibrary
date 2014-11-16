package oarlib.graph.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.InvalidEndpointsException;
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
    private TIntObjectHashMap<V> mInternalVertexMap; //indexed by ids
    private TIntObjectHashMap<E> mInternalEdgeMap;

    protected MutableGraph() {
        super();
        mVertices = new HashSet<V>();
        mEdges = new HashSet<E>();
        mInternalVertexMap = new TIntObjectHashMap<V>();
        mInternalEdgeMap = new TIntObjectHashMap<E>();
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
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setMatchId(matchId);
        this.addEdge(temp);
    }

    public void addEdge(int i, int j, String desc, int cost, int matchId, boolean isReq)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setRequired(isReq);
        temp.setMatchId(matchId);
        this.addEdge(temp);
    }

    public void addEdge(int i, int j, int cost, int matchId) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
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
        mInternalEdgeMap = new TIntObjectHashMap<E>();
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
        mInternalEdgeMap.put(e.getId(), e);
        e.setFinalized(true);
    }

    @Override
    public void addEdge(int i, int j, int cost) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        this.addEdge(this.constructEdge(i, j, "", cost));
    }

    @Override
    public void addEdge(int i, int j, int cost, boolean isReq) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        this.addEdge(i, j, "", cost, isReq);
    }

    @Override
    public void addEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        this.addEdge(this.constructEdge(i, j, desc, cost));
    }

    @Override
    public void addEdge(int i, int j, String desc, int cost, boolean isReq)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        E temp = this.constructEdge(i, j, desc, cost);
        temp.setRequired(isReq);
        this.addEdge(temp);
    }

    @Override
    public E getEdge(int i) {
        if (!mInternalEdgeMap.containsKey(i))
            throw new IllegalArgumentException("The link with this id does not appear to exist in this graph.");
        return mInternalEdgeMap.get(i);
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
        mInternalVertexMap.put(v.getId(), v);
    }

    @Override
    public void addVertex() {
        this.addVertex(this.constructVertex(""));
    }

    @Override
    public V getVertex(int i) {
        if(!mInternalVertexMap.containsKey(i))
            throw new IllegalArgumentException("No vertex with the specified id exists in this graph.");
        return mInternalVertexMap.get(i);
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
    public void removeEdge(E e) throws IllegalArgumentException {
        if (!mEdges.contains(e))
            throw new IllegalArgumentException("Could not remove edge because it wasn't detected as existing in the first place!");
        mEdges.remove(e);
        mInternalEdgeMap.remove(e.getId());
        e.setFinalized(false);
    }

    @Override
    public void removeEdge(int i) throws IllegalArgumentException {
        if (!mInternalEdgeMap.containsKey(i))
            throw new IllegalArgumentException("Could not remove edge because it wasn't detected as existing in the first place!");
        E temp = mInternalEdgeMap.get(i);
        this.removeEdge(temp);
    }

    @Override
    public TIntObjectHashMap<V> getInternalVertexMap() {
        return mInternalVertexMap;
    }

    @Override
    public TIntObjectHashMap<E> getInternalEdgeMap() {
        return mInternalEdgeMap;
    }
}
