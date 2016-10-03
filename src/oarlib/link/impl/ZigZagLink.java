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
package oarlib.link.impl;

import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.ZigZagVertex;

/**
 * Created by oliverlum on 3/22/15.
 */
public class ZigZagLink extends Link<ZigZagVertex> implements AsymmetricLink {

    private int mReverseCost;
    private int mReverseServiceCost;
    private double zigzagCost;
    private ZigZagStatus mStatus;


    /**
     * Constructor for a zig zag link.
     *
     * @param label              - A string title / label for the link
     * @param endpoints          - the endpoints of the link.
     * @param cost               - the deadhead cost (excluding service) of going from the first endpoint to the second endpoint.
     * @param reverseCost        - the deadhead cost (excluding service) of going from the second endpoint to the first endpoint.
     * @param zigzagCost         - the cost of zigzagging this link.
     * @param serviceCost        - The service cost of going from the first endpoint to the second endpoint.
     * @param reverseServiceCost -  The service cost of going form the second endpoint to the first endpoint.
     * @param status             - an option indicating whether the link must be zig zagged, can be, or can't be.
     */
    public ZigZagLink(String label, Pair<ZigZagVertex> endpoints, int cost, int reverseCost, double zigzagCost, int serviceCost, int reverseServiceCost, ZigZagStatus status) {
        super(label, endpoints, cost);
        setmReverseCost(reverseCost);
        setZigzagCost(zigzagCost);
        setServiceCost(serviceCost);
        setReverseServiceCost(reverseServiceCost);
        setStatus(status);
    }

    /**
     * Constructor for a zig zag link.
     *
     * @param label              - A string title / label for the link
     * @param endpoints          - the endpoints of the link.
     * @param cost               - the deadhead cost (excluding service) of going from the first endpoint to the second endpoint.
     * @param reverseCost        - the deadhead cost (excluding service) of going from the second endpoint to the first endpoint.
     * @param zigzagCost         - the cost of zigzagging this link.
     * @param serviceCost        - The service cost of going from the first endpoint to the second endpoint.
     * @param reverseServiceCost -  The service cost of going form the second endpoint to the first endpoint.
     * @param status             - an option indicating whether the link must be zig zagged, can be, or can't be.
     * @param required           - true if this link must be serviced, false oth.  (note, a non-required link will ignore service costs, status, and zigzag cost)
     */
    public ZigZagLink(String label, Pair<ZigZagVertex> endpoints, int cost, int reverseCost, double zigzagCost, int serviceCost, int reverseServiceCost, ZigZagStatus status, boolean required) {
        super(label, endpoints, cost);
        setmReverseCost(reverseCost);
        setZigzagCost(zigzagCost);
        setServiceCost(serviceCost);
        setReverseServiceCost(reverseServiceCost);
        setStatus(status);
        setRequired(required);
    }

    //=============================================
    //
    //    Getters and Setters
    //
    //=============================================
    public int getReverseCost() {
        return mReverseCost;
    }

    public void setmReverseCost(int mReverseCost) {
        this.mReverseCost = mReverseCost;
    }

    public int getReverseServiceCost() {
        return mReverseServiceCost;
    }

    public void setReverseServiceCost(int mReverseServiceCost) {
        this.mReverseServiceCost = mReverseServiceCost;
    }

    public double getZigzagCost() {
        return zigzagCost;
    }

    public void setZigzagCost(double zigzagCost) {
        this.zigzagCost = zigzagCost;
    }

    public ZigZagStatus getStatus() {
        return mStatus;
    }

    public void setStatus(ZigZagStatus mStatus) {
        this.mStatus = mStatus;
    }

    public boolean isRequired() {
        return getServiceCost() != 0;
    }

    @Override
    public void setReverseRequired(boolean newRequired) {
        if(!newRequired)
            mReverseServiceCost = 0;
        else
            mReverseServiceCost = 1;
    }

    //=============================================
    //
    //     Constructors
    //
    //=============================================

    public boolean isReverseRequired() {
        return mReverseServiceCost != 0;
    }

    @Override
    public Link<ZigZagVertex> getCopy() {
        return new ZigZagLink("copy", this.getEndpoints(), this.getCost(), this.getReverseCost(), this.getZigzagCost(), this.getServiceCost(), this.getReverseServiceCost(), this.getStatus(), this.isRequired());
    }

    //==================================
    //
    //     Graph Overrides
    //
    //==================================

    @Override
    public boolean isWindy() {
        return true;
    }

    @Override
    public Type getLinkType() {
        return Type.WINDY;
    }

    public enum ZigZagStatus {
        OPTIONAL, //can ZigZag == 3
        MANDATORY, //must ZigZag == 1
        NOT_AVAILABLE //can't ZigZag == 2
    }
}
