package oarlib.graph.graphgen.Util;

/**
 * Created by oliverlum on 10/7/14.
 */
public class BoundingBox {

    private double mMinLon, mMinLat, mMaxLon, mMaxLat;

    public BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
        mMinLat = minLat;
        mMinLon = minLon;
        mMaxLat = maxLat;
        mMaxLon = maxLon;
    }

    public double getMinLon() {
        return mMinLon;
    }

    public void setMinLon(double mMinLon) {
        this.mMinLon = mMinLon;
    }

    public double getMinLat() {
        return mMinLat;
    }

    public void setMinLat(double mMinLat) {
        this.mMinLat = mMinLat;
    }

    public double getMaxLon() {
        return mMaxLon;
    }

    public void setMaxLon(double mMaxLon) {
        this.mMaxLon = mMaxLon;
    }

    public double getMaxLat() {
        return mMaxLat;
    }

    public void setMaxLat(double mMaxLat) {
        this.mMaxLat = mMaxLat;
    }
}
