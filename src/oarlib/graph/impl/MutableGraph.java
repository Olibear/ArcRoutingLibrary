package oarlib.graph.impl;

import java.util.HashMap;
import java.util.HashSet;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.InvalidEndpointsException;

/**
 * A mutable graph.  In the future, plans are to optimize performance by writing a finalized one with
 * immutable data structures that have faster access time.
 * @author Oliver
 *
 */
public abstract class MutableGraph<V extends Vertex, E extends Link<V>> extends Graph<V,E>{

	private HashSet<V> mVertices;
	private HashSet<E> mEdges;
	private HashMap<Integer, V> mGlobalVertexMap; //indexed by guids
	private HashMap<Integer, V> mInternalVertexMap; //indexed by ids
	private HashMap<Integer, E> mGlobalEdgeMap;
	private HashMap<Integer, E> mInternalEdgeMap; 
	
	protected MutableGraph(){
		mVertices = new HashSet<V>();
		mEdges = new HashSet<E>();
		mGlobalVertexMap = new HashMap<Integer, V>();
		mInternalVertexMap = new HashMap<Integer, V>();
		mGlobalEdgeMap = new HashMap<Integer, E>();
		mInternalEdgeMap = new HashMap<Integer, E>();
	}
	
	//==============================
	//Graph overrides
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
	public void addEdge(E e) throws InvalidEndpointsException{
		// enforce endpoints being added first
		if (!mVertices.contains(e.getEndpoints().getFirst()) || !mVertices.contains(e.getEndpoints().getSecond()))
			throw new InvalidEndpointsException();
		e.setId(this.assignEdgeId());
		mEdges.add(e);
		mGlobalEdgeMap.put(e.getGuid(), e);
		mInternalEdgeMap.put(e.getId(), e);
	}
	public void addEdge(E e, int matchId) throws InvalidEndpointsException
	{
		this.addEdge(e);
		e.setMatchId(matchId);
	}

	@Override
	public void addEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException
	{
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		this.addEdge(this.constructEdge(i, j, desc, cost));
	}
	public void addEdge(int i, int j, String desc, int cost, int matchId) throws InvalidEndpointsException
	{
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		E temp = this.constructEdge(i, j, desc, cost);
		temp.setMatchId(matchId);
		this.addEdge(temp);
	}
	@Override
	public void addVertex(V v) {
		v.setId(this.assignVertexId());
		mVertices.add(v);
		mGlobalVertexMap.put(v.getGuid(), v);
		mInternalVertexMap.put(v.getId(), v);
	}
	public void addVertex(V v, int matchId)
	{
		this.addVertex(v);
		v.setMatchId(matchId);
	}
	@Override
	public void removeEdge(E e)
	{
		mEdges.remove(e);
		mGlobalEdgeMap.remove(e.getGuid());
		mInternalEdgeMap.remove(e.getId());
	}
	@Override
	public HashMap<Integer, V> getGlobalVertexMap()
	{
		return mGlobalVertexMap;
	}
	@Override 
	public HashMap<Integer, V>getInternalVertexMap()
	{
		return mInternalVertexMap;
	}
	@Override
	public HashMap<Integer, E> getGlobalEdgeMap()
	{
		return mGlobalEdgeMap;
	}
	@Override
	public HashMap<Integer, E> getInternalEdgeMap()
	{
		return mInternalEdgeMap;
	}
}
