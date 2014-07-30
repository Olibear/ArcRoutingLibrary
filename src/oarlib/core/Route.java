package oarlib.core;

import java.util.List;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 * @author oliverlum
 *
 */
public abstract class Route {

	protected int mCost; // cost of the route

	//constructor
	protected Route(){
		mCost = 0;
	}

	/**
	 * @return the cost of the route
	 */
	public int getCost()
	{
		return mCost;
	}

	/**
	 * Retrieve a copy of the current route.
	 * @return List of edges to be traversed from first to last
	 */
	public abstract List<? extends Link<? extends Vertex>> getRoute();
	/**
	 * Add a edge to the end of this route.
	 * @param l
	 */
	public abstract void appendEdge(Link<? extends Vertex> l);
	/**
	 * check to make sure that the route is actually a route, (i.e. that consecutive 
	 * links in the list are adjacent, and any other checks that different types
	 * of routes may wish to perform).
	 * @return true if route is feasible in the provided graph
	 */
	public abstract boolean checkRoutes(Graph<? extends Vertex, ? extends Link<? extends Vertex>> g);
	/**
	 * Outputs a string representation of the route.
	 */
	public String toString()
	{
		String ans = "";
		List<? extends Link<? extends Vertex>> list = this.getRoute();
		int n = list.size();
		Link<? extends Vertex> tempL, tempL2;
		int prevId1, prevId2, prevIdReal, beginningCycleLength;
		Vertex tempV1, tempV2, tempV12, tempV22;
		boolean firstToSecond = false;

		//edge case
		if (n == 0)
			return ans;
		if(n == 1)
		{
			tempL = list.get(0);
			ans += tempL.getEndpoints().getFirst().getId() + "-";
			ans += tempL.getEndpoints().getSecond().getId();
			return ans;
		}


		tempL = list.get(0);
		tempV1 = tempL.getEndpoints().getFirst();
		tempV2 = tempL.getEndpoints().getSecond();
		prevId1 = tempV1.getId();
		prevId2 = tempV2.getId();

		//first vertex
		tempL = list.get(1);
		tempV1 = tempL.getEndpoints().getFirst();
		tempV2 = tempL.getEndpoints().getSecond();

		//special case where the first n edges form consecutive 2-cycles and everybody is undirected, we have to look forward to decide how to orient
		if(tempV1.getId() == prevId1 && tempV2.getId() == prevId2 || tempV1.getId() == prevId2 && tempV2.getId() == prevId1)
		{
			beginningCycleLength = 2;
			for(int i = 2; i < n; i++)
			{
				tempL2 = list.get(i);
				tempV12 = tempL2.getEndpoints().getFirst();
				tempV22 = tempL2.getEndpoints().getSecond();
				if(tempV12.getId() == prevId1 && tempV22.getId() == prevId2 || tempV12.getId() == prevId2 && tempV22.getId() == prevId1)
					beginningCycleLength++;
				else
				{
					//figure out the launch point; was it prevId1, or prevId2
					if(tempV12.getId() == prevId1)
					{
						if(beginningCycleLength % 2 == 0) //we need to start at prevId1 - prevId2 - prevId1 - ...
							firstToSecond = true;
						else
							firstToSecond = false;
					}
					else if(tempV12.getId() == prevId2)
					{
						if(beginningCycleLength % 2 == 0) //we need to start at prevId2 - prevId1 - prevId2 - ...
							firstToSecond = false;
						else
							firstToSecond = true;
					}
					else
					{
						ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
						return ans;
					}
					break;
				}
			}
		}

		if(tempV1.getId() == prevId1)
		{
			if(tempV2.getId() == prevId2)
			{
				if(firstToSecond)
				{
					ans += prevId1 + "-" + prevId2 + "-";
					prevIdReal = prevId1;
				}
				else
				{
					ans += prevId2 + "-" + prevId1 + "-";
					prevIdReal = prevId2;
				}
			}
			else
			{
				ans += prevId2 + "-" + prevId1 + "-";
				prevIdReal = tempV2.getId();
			}
		}
		else if(tempV2.getId() == prevId1)
		{
			if(tempV1.getId() == prevId2)
			{
				if(firstToSecond)
				{
					ans += prevId1 + "-" + prevId2 + "-";
					prevIdReal = prevId1;
				}
				else
				{
					ans += prevId2 + "-" + prevId1 + "-";
					prevIdReal = prevId2;
				}
			}
			else
			{
				ans += prevId2 + "-" + prevId1 + "-";
				prevIdReal = tempV1.getId();
			}
		}
		else if(tempV1.getId() == prevId2)
		{
			ans += prevId1 + "-" + prevId2 + "-";
			prevIdReal = tempV2.getId();
		}
		else if(tempV2.getId() == prevId2)
		{
			ans += prevId1 + "-" + prevId2 + "-";
			prevIdReal = tempV1.getId();
		}
		else
		{
			ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
			return ans;
		}

		for(int i = 2; i < n; i++)
		{
			tempL = list.get(i);
			tempV1 = tempL.getEndpoints().getFirst();
			tempV2 = tempL.getEndpoints().getSecond();

			if(tempV1.getId() == prevIdReal)
			{
				ans += tempV1.getId() + "-";
				prevIdReal = tempV2.getId();
			}
			else if(tempV2.getId() == prevIdReal)
			{
				ans += tempV2.getId() + "-";
				prevIdReal = tempV1.getId();
			}
			else
			{
				ans = "Adjacent links in this route didn't share a common vertex.  Please try running checkRoutes to verify the integrity of the route.";
				return ans;
			}
		}

		if(n >= 2)
		{
			ans += prevIdReal;
		}
		return ans;
	}


}
