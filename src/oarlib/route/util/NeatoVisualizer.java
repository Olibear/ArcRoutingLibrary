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
package oarlib.route.util;

import gnu.trove.TIntHashSet;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.util.CommonAlgorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by oliverlum on 12/14/15.
 */
public class NeatoVisualizer {

    public static <V extends Vertex, E extends Link<V>> void visualize(Route<V,E> r, Graph<V,E> g, String filePath) {

        //figure out whom we need
        ArrayList<E> path = r.getPath();
        TIntHashSet vIds = new TIntHashSet();
        for(E e: path) {
            vIds.add(e.getFirstEndpointId());
            vIds.add(e.getSecondEndpointId());
        }
        int[] uniqueIds = vIds.toArray();

        //shortest path
        int n = g.getVertices().size();
        int[][] distMat = new int[n+1][n+1];
        int[][] pathMat = new int[n+1][n+1];

        CommonAlgorithms.fwLeastCostPaths(g, distMat, pathMat);

        //write the shortest path matrix
        try {
            PrintWriter pw = new PrintWriter(new File(filePath));
            String line;
            for(int i = 0; i < uniqueIds.length; i++) {
                line = "";
                for(int j = 0; j < uniqueIds.length; j++) {
                    if(i == j)
                        line += "0 ";
                    else
                        line += distMat[uniqueIds[i]][uniqueIds[j]] + " ";
                }
                pw.println(line);
            }
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //call the python script
        try {
            ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/visualize.py", "-i", filePath);
            Process run = pb.start();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line: " + line);
            while ((line = bfr.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
            System.out.println("Complete");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
