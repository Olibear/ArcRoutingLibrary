/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
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
 *
 */
package oarlib.problem.impl;

import oarlib.core.Graph;

import java.util.Arrays;

/**
 * Created by oliverlum on 1/18/15.
 */
public class ProblemAttributes {

    /**
     * *******************************
     * END GETTERS
     * ********************************
     */

    private Graph.Type mGraphType;
    private ProblemAttributes.Type mProblemType;
    private ProblemAttributes.NumVehicles mNumVehicles;
    private ProblemAttributes.NumDepots mNumDepots;

    /**
     * *******************************
     * END ENUM TYPES
     * ********************************
     */
    private ProblemAttributes.Properties[] mProperties;

    public ProblemAttributes(Graph.Type graphType, ProblemAttributes.Type problemType, ProblemAttributes.NumVehicles numVehicles,
                             ProblemAttributes.NumDepots numDepots, ProblemAttributes.Properties... properties) {
        mGraphType = graphType;
        mProblemType = problemType;
        mNumVehicles = numVehicles;
        mNumDepots = numDepots;
        mProperties = properties;
    }

    /**
     * *******************************
     * BEGIN GETTERS
     * ********************************
     */
    public Graph.Type getmGraphType() {
        return mGraphType;
    }

    public Type getmProblemType() {
        return mProblemType;
    }

    public NumVehicles getmNumVehicles() {
        return mNumVehicles;
    }

    public NumDepots getmNumDepots() {
        return mNumDepots;
    }

    public Properties[] getmProperties() {
        return mProperties;
    }

    public boolean isCompatibleWith(ProblemAttributes other) {

        if (mGraphType != null && other.getmGraphType() != null && mGraphType != other.getmGraphType())
            return false;
        if (mProblemType != null && other.getmProblemType() != null && mProblemType != other.getmProblemType())
            return false;
        if (mNumVehicles != null && other.getmNumVehicles() != null && mNumVehicles != other.getmNumVehicles())
            return false;
        if (mNumDepots != null && other.getmNumDepots() != null && mNumDepots != other.getmNumDepots())
            return false;
        if (!Arrays.deepEquals(mProperties, other.getmProperties()))
            return false;

        return true;
    }

    /**
     * *******************************
     * BEGIN ENUM TYPES
     * ********************************
     */
    public enum Type {
        CHINESE_POSTMAN,
        RURAL_POSTMAN,
        PARTITIONING
    }

    public enum NumVehicles {
        SINGLE_VEHICLE,
        MULTI_VEHICLE,
        NO_VEHICLES //for auxiliary problems
    }

    public enum NumDepots {
        SINGLE_DEPOT,
        MULTI_DEPOT,
        NO_DEPOTS //for auxiliary problems
    }

    public enum Properties {
        TIME_WINDOWS,
        SERVICE_COSTS,
        ZIGZAG_COSTS
    }

}
