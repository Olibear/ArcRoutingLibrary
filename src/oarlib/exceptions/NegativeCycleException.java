package oarlib.exceptions;

/**
 * Created by oliverlum on 11/5/14.
 */
public class NegativeCycleException extends Exception {

    private int violatingIndex;
    private int[] path;
    private int[] edgePath;

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath) {
        super();
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, String message) {
        super(message);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, String message, Throwable cause) {
        super(message, cause);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, Throwable cause) {
        super(cause);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public int getViolatingIndex(){
        return violatingIndex;
    }

    public int[] getViolatingPath() { return path; }

    public int[] getViolatingEdgePath() { return edgePath; }
}

