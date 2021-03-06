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
 * The bounding boxes for a set of larger test instances that we present.
 * Created by oliverlum on 10/7/14.
 */
public class OSM_BoundingBoxes {

    public static final BoundingBox[] STEFAN_INSTANCES = new BoundingBox[]{
            //CP
            new BoundingBox(-76.952876, 38.983533, -76.934680, 38.998043),
            //JHU
            new BoundingBox(-76.630753, 39.322049, -76.619724, 38.332473),
            //Chi
            new BoundingBox(-87.643286, 41.877017, -87.618975, 41.897473),
            //Sea
            new BoundingBox(-122.316545, 47.597885, -122.279837, 47.638979),
            //Bei
            new BoundingBox(116.356952, 39.924082, 116.365685, 39.931980),
    };

    public static final BoundingBox[] CITY_INSTANCES = new BoundingBox[]{
            //NYC
            //new BoundingBox(-73.978468, 40.787057, -73.966280, 40.792076, "NYC"),
            //SF
            //new BoundingBox(-122.416549, 37.782041, -122.402816, 37.791249, "San Francisco"),
            //DC
            //new BoundingBox(-77.035293, 38.930782, -77.024693, 38.936974, "Washington D.C."),
            //Paris
            //new BoundingBox(2.284695, 48.867812, 2.303707, 48.878453, "Paris, France"),
            //London
            //new BoundingBox(-.137157, 51.510221, -.126986, 51.518527, "London, UK"),
            //Istanbul
            //new BoundingBox(28.954437, 41.004398, 28.969264, 41.009030, "Istanbul, Turkey"),
            //Perth
            //new BoundingBox(115.858599, -31.944743, 115.869693, -31.939681, "Perth, Australia"),
            //Auckland
            //new BoundingBox(174.728553, -36.860062, 174.743359, -36.843990, "Auckland, Australia"),
            //Helsinki
            //new BoundingBox(24.935060, 60.164540, 24.943771, 60.169360, "Helsinki, Finland"),
            //Calgary
            new BoundingBox(-114.090764, 51.037411, -114.061281, 51.049795, "Calgary, Canada"),
            //Vienna
            //new BoundingBox(16.367078, 48.205786, 16.375082, 48.211377, "Vienna, Austria")
    };

    public static final BoundingBox[] CITY_INSTANCES_SMALL = new BoundingBox[]{
            //NYC
            //new BoundingBox(-73.967990, 40.792020, -73.966280, 40.792076, "NYC"),
            //SF
            new BoundingBox(-122.404000, 37.787041, -122.402816, 37.791249, "San Francisco"),
            //DC
            //new BoundingBox(-77.025293, 38.933782, -77.024693, 38.936974, "Washington D.C."),
            //Paris
            new BoundingBox(2.298695, 48.876812, 2.303707, 48.878453, "Paris, France"),
            //London
            //new BoundingBox(-.127157, 51.510221, -.126986, 51.518527, "London, UK"),
            //Istanbul
            new BoundingBox(28.967437, 41.007398, 28.969264, 41.009030, "Istanbul, Turkey"),
            //Perth
            //new BoundingBox(115.858599, -31.944743, 115.869693, -31.939681, "Perth, Australia"),
            //Auckland
            //new BoundingBox(174.728553, -36.860062, 174.743359, -36.843990, "Auckland, Australia"),
            //Helsinki
            //new BoundingBox(24.935060, 60.164540, 24.943771, 60.169360, "Helsinki, Finland"),
            //Calgary
            //new BoundingBox(-114.090764, 51.037411, -114.061281, 51.049795, "Calgary, Canada"),
            //Vienna
            //new BoundingBox(16.367078, 48.205786, 16.375082, 48.211377, "Vienna, Austria")

    };

    public static final BoundingBox[] SUBURBAN_INSTANCES = new BoundingBox[]{
            //home
            new BoundingBox(-77.1420210, 39.0426980, -77.1290200, 39.0492390, "Bethesda, MD"),
            //Newton, MA
            new BoundingBox(-71.217725, 42.329616, -71.198092, 42.338468, "Newton, MA"),
            //Plymouth, MN
            new BoundingBox(-93.377998, 45.048841, -93.362548, 45.056117, "Plymouth, MN"),
            //Ellicott, MD
            new BoundingBox(-76.811887, 39.259472, -76.796223, 39.269107, "Ellicott, MD"),
            //Minnetonka, MN
            new BoundingBox(-93.478582, 44.923115, -93.461674, 44.929511, "Minnetonka, MN"),
            //Dublin, OH
            new BoundingBox(-83.150151, 40.110098, -83.133027, 40.116170, "Dublin, OH"),
            //Wauwatosa, WI
            new BoundingBox(-88.026792, 43.068119, -88.007737, 43.075078, "Wauwatosa, WI"),
            //Evanston, IL
            new BoundingBox(-87.686764, 42.052459, -87.677472, 42.060218, "Evanston, IL"),
            //Columbia, MD
            new BoundingBox(-76.839548, 39.196106, -76.828283, 39.203505, "Columbia, MD"),
            //Towson, MD
            new BoundingBox(-76.624798, 39.397671, -76.609949, 39.404055, "Towson, MD")

    };

    public static final BoundingBox[] RURAL_INSTANCES = new BoundingBox[]{
            //Mauritania
            new BoundingBox(-11.423925, 16.609865, -11.397489, 16.631783, "Mauritania"),
            //Iceland
            new BoundingBox(-21.794402, 64.098088, -21.762430, 64.104086, "Iceland"),
            //Suriname
            new BoundingBox(-55.223577, 5.687858, -55.172250, 5.709038, "Suriname"),
            //Greenland
            new BoundingBox(-51.749314, 64.169166, -51.710433, 64.186850, "Greenland"),
            //Namibia
            new BoundingBox(17.050057, -22.495008, 17.059992, -22.483747, "Namibia"),
            //French Guiana
            new BoundingBox(-52.671360, 5.155302, -52.663307, 5.165410, "French Guiana"),
            //Mongolia
            new BoundingBox(107.027021, 47.914561, 107.040325, 47.921162, "Mongolia"),
            //Morocco
            new BoundingBox(-7.520074, 33.553118, -7.507693, 33.566297, "Morocco"),
    };

    public static final BoundingBox[] BIG_INSTANCES = new BoundingBox[]{
            //College Park
            new BoundingBox(-76.947205, 38.979588, -76.921027, 39.002603, "College Park")
    };


}
