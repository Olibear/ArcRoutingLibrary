package oarlib.link.impl;

/**
 * Created by oliverlum on 7/8/15.
 */
public interface AsymmetricLink {
    int getCost();

    int getServiceCost();

    int getReverseCost();

    int getReverseServiceCost();

    boolean isReverseRequired();
}
