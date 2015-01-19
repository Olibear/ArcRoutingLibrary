package oarlib.problem.impl;

import oarlib.core.Graph;

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
        if (mProperties != other.getmProperties())
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
        RURAL_POSTMAN
    }

    public enum NumVehicles {
        SINGLE_VEHICLE,
        MULTI_VEHICLE
    }

    public enum NumDepots {
        SINGLE_DEPOT,
        MULTI_DEPOT
    }

    public enum Properties {
        TIME_WINDOWS,
        SERVICE_TIME,
    }

}
