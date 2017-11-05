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

import oarlib.core.Route;
import oarlib.link.impl.ZigZagLink;
import oarlib.route.impl.ZigZagTour;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by oliverlum on 11/21/15.
 */
public class RouteExporter {

    public static final double ZZ_TIME_WINDOW_THRESHOLD = 1e5; //if a zigzag time window is above this threshold, it will be treated as though it doesn't have a time window by the heuristic
    private static Logger LOGGER = Logger.getLogger(RouteExporter.class);

    /**
     * Catch all for exporting routes.  To add another format, just add it to the type
     * and then an appropriate clause and method.
     * @param r - the route to be exported
     * @param rf - the format to use
     * @param path - file path for output to be written.
     */
    public static void exportRoute(Route r, RouteFormat rf, String path) {

        switch (rf) {
            case ZHANG:
                exportZhang(r, path, true);
                return;
            case JSON:
                ArrayList<Route> temp = new ArrayList<Route>();
                temp.add(r);
                exportJSON(temp, path);
                return;
        }


        LOGGER.error("The format you have passed in appears to be not supported.");
        return;

    }

    public static void exportRoutes(Collection<? extends Route> routes, RouteFormat rf, String path) {
        switch (rf) {
            case ZHANG:
                LOGGER.error("The ZHANG format does not support multiple simultaneous exports.  Please export them separately.");
                return;
            case JSON:
                exportJSON(routes, path);
                return;
        }
    }

    private static void exportJSON(Collection<? extends Route> routes, String path) {

        try {

            PrintWriter pw = new PrintWriter(new File(path));

            //front matter
            pw.println("{");
            pw.println("\t\"routes\":");
            pw.println("\t[");

            //routes
            int id = 1;
            String toPrint;
            String[] nodeIds;
            Iterator<? extends Route> iter = routes.iterator();
            while(iter.hasNext()) {
                Route r = iter.next();
                pw.println("\t\t{");
                pw.println("\t\t\t\"id\":" + id);
                pw.println("\t\t\t\"nodes\": [");

                //nodes
                nodeIds = r.toString().split("-");
                for(int i = 0; i < nodeIds.length - 1; i++) {
                    pw.println("\t\t\t\t{\"id\":" + nodeIds[i] + "},");
                }
                pw.println("\t\t\t\t{\"id\":" + nodeIds[nodeIds.length - 1] + "}");

                pw.println("\t\t\t]");
                toPrint = "\t\t}";
                if(iter.hasNext())
                    toPrint += ",";
                pw.println(toPrint);

                id++;
            }

            pw.println("\t]");
            pw.println("}");
            pw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }


    }

    private static void exportZhang(Route r, String path, boolean truncate) {

        if(!(r instanceof ZigZagTour))
            LOGGER.error("The Zhang format only works with Zig Zag Tours.");
        try {
            PrintWriter pw = new PrintWriter(new File(path));
            ZigZagTour realRoute = (ZigZagTour) r;

            String orig = realRoute.toString();
            String[] nodes = orig.split("-");

            ArrayList<Boolean> zzList = realRoute.getCompactZZList();
            ArrayList<Boolean> service = realRoute.getServicingList();
            ArrayList<ZigZagLink> fullPath = realRoute.getPath();

            String temp;
            int tempIndex = 0;

            int truncLimit = 0;
            boolean noTWZ = true;
            if(truncate) {
                for(int i = 0; i < fullPath.size(); i++)
                    if(service.get(i)) {
                        if (zzList.get(tempIndex) && fullPath.get(i).getTimeWindow().getSecond() < ZZ_TIME_WINDOW_THRESHOLD) {
                            truncLimit = tempIndex;
                            noTWZ = false;
                        }
                        tempIndex++;
                    }
            }

            //no time-restricted zigzags exercised
            if(noTWZ) {
                new File(path).createNewFile();
                pw.close();
                return;
            }

            tempIndex = 0;
            for (int i = 0; i < nodes.length-1; i++) {

                temp = nodes[i] + "-" + nodes[i + 1];
                if (service.get(i)) {
                    if (zzList.get(tempIndex))
                        temp += ",z";
                    else
                        temp += ",r";
                    tempIndex++;
                } else
                    temp += ",n";
                pw.println(temp);

                if(tempIndex > truncLimit)
                    break;
            }
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum RouteFormat {
        ZHANG, //Rui's requested output format
        JSON // for visualization
    }
}
