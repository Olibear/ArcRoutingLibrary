package oarlib.improvements;

/**
 * Created by Oliver on 12/25/2014.
 */
public class ImprovementStrategy {
    public enum Type {
        FirstImprovement, //stops when any improvement move is found and made
        SteepestDescent, //returns after exhausting the search space, and making the most advantageous move
        RandomMove //returns after making any random move (intended for use as a perturbation procedure
    }
}
