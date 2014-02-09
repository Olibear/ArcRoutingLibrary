package oarlib.core;

import java.util.Stack;

import oarlib.exceptions.WrongEdgeTypeException;

/**
 * Originally intended as a way of keeping track of things in the Yaoyuenyong's algorithm,
 * it is intended simply to act as a way of bundling arcs / edges together.
 * @author oliverlum
 *
 */
public class MultiEdge<E extends Link<? extends Vertex>> {

	private int numCopies;
	private E first;
	private EDGETYPE myType;
	private boolean directedForward;
	private boolean directedBackward;
	public enum EDGETYPE{
		A,B,C,D,E,F;
	}
	public MultiEdge(E e)
	{
		numCopies = 0;
		if(e.isDirected())
		{
			myType = EDGETYPE.E;
			directedForward = true;
		}
		else
		{
			myType = EDGETYPE.A;
			directedForward = false;
		}
		first = e;
		directedBackward = false;
	}
	public MultiEdge<E> getCopy()
	{
		try {
			MultiEdge<E> ret = new MultiEdge<E>(first);
			//direct it properly
			if(directedForward && !first.isDirected())
			{
				ret.directForward();
			}
			else if(directedBackward && !first.isDirected())
			{
				ret.directBackward();
			}
			
			//now add copies
			if(numCopies == -1) //if we're type D, add reverse copy
			{
				ret.directForward();
				ret.addReverseCopy();
			}
			else
			{
				for(int i = 0 ; i < numCopies; i++)
				{
					ret.addCopy();
				}
			}
			return ret;
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	public int getNumCopies()
	{
		return numCopies;
	}
	public void directForward() throws WrongEdgeTypeException
	{
		if(myType == EDGETYPE.A)
		{
			myType = EDGETYPE.B;
			directedForward = true;
			directedBackward = false;
		}
		else if(myType == EDGETYPE.D)
		{
			myType = EDGETYPE.B;
			directedForward = true;
			directedBackward = false;
			numCopies = 0;
		}
		else
			throw new WrongEdgeTypeException("You may only direct a type A or type D edge.");
	}
	public void directBackward() throws WrongEdgeTypeException
	{
		if(myType == EDGETYPE.A)
		{
			myType = EDGETYPE.B;
			directedForward = false;
			directedBackward = true;
		}
		else if(myType == EDGETYPE.D)
		{
			myType = EDGETYPE.B;
			directedForward = false;
			directedBackward = true;
			numCopies = 0;
		}
		else
			throw new WrongEdgeTypeException("You may only direct a type A or type D edge.");

	}
	public void addCopy() throws WrongEdgeTypeException
	{
		if(myType == EDGETYPE.A)
			throw new WrongEdgeTypeException("Cannot copy an undirected edge, direct it first");
		else if(myType == EDGETYPE.D)
			throw new WrongEdgeTypeException("Cannot copy a type D edge, please decide on a direction first");
		//state transitions
		else if(myType == EDGETYPE.B)
			myType = EDGETYPE.C;
		else if(myType == EDGETYPE.E)
			myType = EDGETYPE.F;
		numCopies++;
	}
	public void addReverseCopy() throws WrongEdgeTypeException
	{
		if(myType != EDGETYPE.B)
			throw new WrongEdgeTypeException("Can only add a reverse copy if we are of type B.");
		numCopies = -1;
		//edges of type D have no direction
		directedForward = false;
		directedBackward = false;
		myType = EDGETYPE.D;
	}
	public boolean tryRemoveCopy()
	{
		if(numCopies < 1)
			return false;
		numCopies--;
		if(numCopies == 0)
		{
			if(myType == EDGETYPE.F)
				myType = EDGETYPE.E;
			else if(myType == EDGETYPE.C)
				myType = EDGETYPE.B;
		}
		return true;
	}
	public E getFirst()
	{
		return first;
	}
	public EDGETYPE getType()
	{
		return myType;
	}
	public boolean isDirectedForward()
	{
		return directedForward;
	}
	public boolean isDirectedBackward()
	{
		return directedBackward;
	}

}
