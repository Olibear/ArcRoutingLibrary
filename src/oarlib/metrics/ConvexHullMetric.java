/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Carmine Cerrone
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
package oarlib.metrics;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.*;

/**
 * Created by Carmine Cerrone on 04/12/2015.
 */
public class ConvexHullMetric extends Metric {

    public ConvexHullMetric() {
    }

    @Override
    public Type getType() {
        return Type.CONVEXOVERLAP;
    }

    @Override
    public String toString() {
        return "Convex Hull Percentage Area Overlapped";
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {

        //Compute the convex hull for each routs
        int nRoutes = routes.size();
        if (nRoutes <= 1) {
            return 0;
        }
        Route<V, E>[] routesAr = new Route[nRoutes];
        routesAr = routes.toArray(routesAr);

        Point[][] convexH = new Point[nRoutes][];

        Double[][] hull_intersec_area = new Double[nRoutes][nRoutes];

        for (int i = 0; i < nRoutes; i++) {
            Route<V, E> r = routesAr[i];
            Point[] p = routeInPoints(r.getRoute());
            p = convex_hull(p);
            convexH[i] = p;
        }
        double intersec = 0;
        for (int i = 0; i < nRoutes; i++) {
            Point[] c1 = convexH[i];
            double area1 = area_hull(c1);
            if (area1 > 0) {
                for (int j = 0; j < nRoutes; j++) {
                    if (i != j) {
                        if (hull_intersec_area[i][j] != null) {
                            double area = hull_intersec_area[i][j].doubleValue();
                            intersec += area / area1;
                        } else {

                            Point[] c2 = convexH[j];
                            Point[] inters = new SutherlandHodgmanPanel().intersection(c1, c2);

                            if (inters.length > 2) {
                                inters = convex_hull(inters);

                                if (inters.length > 2) {
                                    double area = area_hull(inters);
                                    hull_intersec_area[i][j] = area;
                                    hull_intersec_area[j][i] = area;
                                    intersec += area / area1;
                                } else {
                                    hull_intersec_area[i][j] = 0.0;
                                    hull_intersec_area[j][i] = 0.0;
                                }
                            } else {
                                hull_intersec_area[i][j] = 0.0;
                                hull_intersec_area[j][i] = 0.0;
                            }
                        }

                    }
                }
            }
        }

        return (intersec) / nRoutes;
    }

    private double cross(Point O, Point A, Point B) {
        return (A.x - O.x) * (B.y - O.y) - (A.y - O.y) * (B.x - O.x);
    }

    private double area_hull(Point[] P) {
        //ref:http://www.mathwords.com/a/area_convex_polygon.htm
        double area = 0;
        int size = P.length;
        for (int i = 0; i < size; i++) {
            int j = (i + 1) % size;
            Point p1 = P[i];
            Point p2 = P[j];
            area += p1.x * p2.y - p1.y * p2.x;
        }

        return area / 2;
    }

    private Point[] convex_hull(Point[] P) {
        //ref: http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
        if (P.length > 1) {
            int n = P.length, k = 0;
            Point[] H = new Point[2 * n];

            Arrays.sort(P);

            // Build lower hull
            for (int i = 0; i < n; ++i) {
                while (k >= 2 && cross(H[k - 2], H[k - 1], P[i]) <= 0) {
                    k--;
                }
                H[k++] = P[i];
            }

            // Build upper hull
            for (int i = n - 2, t = k + 1; i >= 0; i--) {
                while (k >= t && cross(H[k - 2], H[k - 1], P[i]) <= 0) {
                    k--;
                }
                H[k++] = P[i];
            }
            if (k > 1) {
                H = Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
            }
            return H;
        } else if (P.length <= 1) {
            return P;
        } else {
            return null;
        }
    }

    private boolean contains(Point test, Point[] points) {
        //ref: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y)
                    && (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    private <V extends Vertex, E extends Link<V>> Point[] routeInPoints(List<E> r) {

        HashSet<V> hs = new HashSet<V>(r.size());
        for (E link : r) {
            hs.add(link.getEndpoints().getFirst());
            hs.add(link.getEndpoints().getSecond());
        }
        int i = 0;
        Point[] p = new Point[hs.size()];
        for (V v : hs) {
            Point pnt = new Point();
            pnt.v = v;
            pnt.x = v.getX();
            pnt.y = v.getY();
            p[i++] = pnt;
        }
        return p;
    }

    private static class Point implements Comparable<Point> {

        double x, y;
        Vertex v = null;

        static void print(Point[] pt) {
            System.out.println("Points: " + pt.length);
            for (int i = 0; i < pt.length; i++) {
                Point p = pt[i];
                if (p.v == null) {
                    System.out.println(i + " \t: " + p.x + " : " + p.y);
                } else {
                    System.out.println(i + " \t: " + p.v.getLabel());
                }
            }
        }

        public int compareTo(Point p) {
            if (this.x == p.x) {
                if (this.y == p.y) {
                    return 0;
                }
                return this.y - p.y < 0 ? -1 : 1;
            } else {
                return this.x - p.x < 0 ? -1 : 1;
            }
        }
    }

    private static class SutherlandHodgmanPanel {
        //ref: http://rosettacode.org/wiki/Sutherland-Hodgman_polygon_clipping#Java

        public Point[] intersection(Point[] r1, Point[] r2) {
            ArrayList<double[]> subject = new ArrayList<double[]>(r1.length);
            ArrayList<double[]> clipper = new ArrayList<double[]>(r2.length);
            for (Point p : r1) {
                subject.add(new double[]{p.x, p.y});
            }
            for (Point p : r2) {
                clipper.add(new double[]{p.x, p.y});
            }
            List<double[]> result = clipPolygon(subject, clipper);
            Point[] pres = new Point[result.size()];
            for (int i = 0; i < result.size(); i++) {
                double[] ds = result.get(i);
                pres[i] = new Point();
                pres[i].x = ds[0];
                pres[i].y = ds[1];
            }
            return pres;
        }

        private List<double[]> clipPolygon(List<double[]> subject, List<double[]> clipper) {
            ArrayList<double[]> result = new ArrayList<double[]>(subject);
            int len = clipper.size();
            for (int i = 0; i < len; i++) {

                int len2 = result.size();
                List<double[]> input = result;
                result = new ArrayList<double[]>(len2);

                double[] A = clipper.get((i + len - 1) % len);
                double[] B = clipper.get(i);

                for (int j = 0; j < len2; j++) {

                    double[] P = input.get((j + len2 - 1) % len2);
                    double[] Q = input.get(j);

                    if (isInside(A, B, Q)) {
                        if (!isInside(A, B, P)) {
                            double[] inte = intersection(A, B, P, Q);
                            if (inte != null) {
                                result.add(inte);
                            }
                        }
                        result.add(Q);
                    } else if (isInside(A, B, P)) {
                        double[] inte = intersection(A, B, P, Q);
                        if (inte != null) {
                            result.add(inte);
                        }
                    }
                }
            }
            return result;
        }

        private boolean isInside(double[] a, double[] b, double[] c) {
            return (a[0] - c[0]) * (b[1] - c[1]) > (a[1] - c[1]) * (b[0] - c[0]);
        }

        private double[] intersection(double[] a, double[] b, double[] p, double[] q) {
            double A1 = b[1] - a[1];
            double B1 = a[0] - b[0];
            double C1 = A1 * a[0] + B1 * a[1];

            double A2 = q[1] - p[1];
            double B2 = p[0] - q[0];
            double C2 = A2 * p[0] + B2 * p[1];

            double det = A1 * B2 - A2 * B1;
            if (det == 0) {
                return null;
            }
            double x = (B2 * C1 - B1 * C2) / det;
            double y = (A1 * C2 - A2 * C1) / det;

            return new double[]{x, y};
        }

    }
}
