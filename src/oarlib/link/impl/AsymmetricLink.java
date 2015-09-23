package oarlib.link.impl;

/**
 * Created by oliverlum on 7/8/15.
 */
public interface AsymmetricLink {
    public int getCost();

    public int getReverseCost();

    public boolean isReverseRequired();
}
