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
package oarlib.graph.graphgen.Util;

/**
 * Created by oliverlum on 10/7/14.
 */
public class BoundingBox {

    private double mMinLon, mMinLat, mMaxLon, mMaxLat;
    private String mTitle;

    public BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
        this(minLon, minLat, maxLon, maxLat, "");
    }

    public BoundingBox(double minLon, double minLat, double maxLon, double maxLat, String title) {
        mMinLat = minLat;
        mMinLon = minLon;
        mMaxLat = maxLat;
        mMaxLon = maxLon;
        mTitle = title;
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

    public String getTitle() {
        return this.mTitle;
    }
}
