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

import oarlib.exceptions.WrongEdgeTypeException;
import org.apache.log4j.Logger;

/**
 * Originally intended as a way of keeping track of things in the Yaoyuenyong's algorithm,
 * it is intended simply to act as a way of bundling arcs / edges together.
 *
 * @author oliverlum
 */
public class MultiEdge<E extends Link<? extends Vertex>> {

    private static final Logger LOGGER = Logger.getLogger(MultiEdge.class);

    private int numCopies; // how many copies does this edge represent (if this is zero, we still have the original)
    private E first; // the original edge / arc that this multi-edge represents
    private EDGETYPE myType; // the type of the multi-edge
    private boolean directedForward; // if the copies are directed from endpoint 1 to endpoint 2, this is true (D is the exception; it has both flags set to false)
    private boolean directedBackward; // if the copies are directed from endpoint 2 to endpoint 1, this is true (D is the exception; it has both flags set to false)

    /**
     * Default constructor for the multi-edge.
     *
     * @param e - the base edge.  We set numCopies to 0 initially.
     */
    public MultiEdge(E e) {
        numCopies = 0;
        if (e.isDirected()) {
            myType = EDGETYPE.E;
            directedForward = true;
        } else {
            myType = EDGETYPE.A;
            directedForward = false;
        }
        first = e;
        directedBackward = false;
    }

    /**
     * @return - a deep copy of the multi-edge (i.e. it's a completely different object where all fields are
     * set to this multi-edge's)
     */
    public MultiEdge<E> getCopy() {
        try {
            MultiEdge<E> ret = new MultiEdge<E>(first);
            //direct it properly
            if (directedForward && !first.isDirected()) {
                ret.directForward();
            } else if (directedBackward && !first.isDirected()) {
                ret.directBackward();
            }

            //now add copies
            if (numCopies == -1) //if we're type D, add reverse copy
            {
                ret.directForward();
                ret.addReverseCopy();
            } else {
                for (int i = 0; i < numCopies; i++) {
                    ret.addCopy();
                }
            }
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * @return - current number of copies that this multi-edge represents
     */
    public int getNumCopies() {
        return numCopies;
    }

    /**
     * A setter to change the orientation of future copies to forward.
     * Note that the edge must be of type A or D, or else we cannot change
     * the orientation.
     *
     * @throws WrongEdgeTypeException - if this multi-edge is not of type A or type D.
     */
    public void directForward() throws WrongEdgeTypeException {
        if (myType == EDGETYPE.A) {
            myType = EDGETYPE.B;
            directedForward = true;
            directedBackward = false;
        } else if (myType == EDGETYPE.D) {
            myType = EDGETYPE.B;
            directedForward = true;
            directedBackward = false;
            numCopies = 0;
        } else {
            LOGGER.error("You may only direct a type A or type D edge.");
            throw new WrongEdgeTypeException();
        }
    }

    /**
     * A setter to changet he orientation of future copies to backward.
     * Note that the edge must be of type A or type D, or else we cannot
     * change the orientation.
     *
     * @throws WrongEdgeTypeException - if this multi-edge is not of type A or type D
     */
    public void directBackward() throws WrongEdgeTypeException {
        if (myType == EDGETYPE.A) {
            myType = EDGETYPE.B;
            directedForward = false;
            directedBackward = true;
        } else if (myType == EDGETYPE.D) {
            myType = EDGETYPE.B;
            directedForward = false;
            directedBackward = true;
            numCopies = 0;
        } else {
            LOGGER.error("You may only direct a type A or type D edge.");
            throw new WrongEdgeTypeException();
        }

    }

    /**
     * Adds to numCopies so long as the edge type is acceptable.
     *
     * @throws WrongEdgeTypeException - if this edge is of type A, or D an exception is thrown (since we don't know what direction to add the copy
     */
    public void addCopy() throws WrongEdgeTypeException {
        if (myType == EDGETYPE.A) {
            LOGGER.error("Cannot copy an undirected edge, direct it first");
            throw new WrongEdgeTypeException();
        } else if (myType == EDGETYPE.D) {
            LOGGER.error("Cannot copy a type D edge, please decide on a direction first");
            throw new WrongEdgeTypeException();
        }
        //state transitions
        else if (myType == EDGETYPE.B)
            myType = EDGETYPE.C;
        else if (myType == EDGETYPE.E)
            myType = EDGETYPE.F;
        numCopies++;
    }

    /**
     * Adds a reverse copy to a type B edge to turn it into a type D edge
     *
     * @throws WrongEdgeTypeException - if the edge type is not B, then this operation is invalid
     */
    public void addReverseCopy() throws WrongEdgeTypeException {
        if (myType != EDGETYPE.B) {
            LOGGER.error("Can only add a reverse copy if we are of type B.");
            throw new WrongEdgeTypeException();
        }
        numCopies = -1;
        //edges of type D have no direction
        directedForward = false;
        directedBackward = false;
        myType = EDGETYPE.D;
    }

    /**
     * A setter that decrements the numCopies appropriately, and handles
     * the state transitions that accompany this operation.
     *
     * @return - false if the number of copies is non-positive, true oth.
     */
    public boolean tryRemoveCopy() {
        if (numCopies < 1)
            return false;
        numCopies--;
        if (numCopies == 0) {
            if (myType == EDGETYPE.F)
                myType = EDGETYPE.E;
            else if (myType == EDGETYPE.C)
                myType = EDGETYPE.B;
        }
        return true;
    }

    /**
     * Getter for the base edge that defines this multi-edge.
     *
     * @return - the base edge.
     */
    public E getFirst() {
        return first;
    }

    /**
     * Getter for the edge type of this multi-edge.
     *
     * @return - the edge type
     */
    public EDGETYPE getType() {
        return myType;
    }

    /**
     * @return - true if this multi-edge is directed forward, false oth.
     */
    public boolean isDirectedForward() {
        return directedForward;
    }

    /**
     * @return - true if this multie-edge is directed backward, false oth.
     */
    public boolean isDirectedBackward() {
        return directedBackward;
    }

    public enum EDGETYPE {
        /**
         * A = undirected edge
         * B = directed edge
         * C = directed edge + n copies (n > 0)
         * D = edge that has become two, opposite direction arcs
         * E = directed arc
         * F = directed arc + n copies (n > 0)
         */
        A, B, C, D, E, F
    }

}
