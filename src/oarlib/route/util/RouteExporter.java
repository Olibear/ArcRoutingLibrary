package oarlib.route.util;

import oarlib.core.Route;
import oarlib.link.impl.ZigZagLink;
import oarlib.route.impl.ZigZagTour;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

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
            case ZHANG: exportZhang(r, path, true);
                return;
        }


        LOGGER.error("The format you have passed in appears to be not supported.");
        return;

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
    }
}
