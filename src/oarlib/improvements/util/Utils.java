package oarlib.improvements.util;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 11/29/14.
 */
public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);
    /**
     * fetches the id of the longest route in the initial solution passed in.
     *
     * @return the id of the longest route in the initial solution provided by this object.
     */
    public static <V extends Vertex, E extends Link<V>> Route<V,E> findLongestRoute(Collection<Route<V,E>> routes){
        int max = Integer.MIN_VALUE;
        Route<V,E> ret = null;
        for(Route<V,E> r: routes) {
            if(r.getCost() > max) {
                max = r.getCost();
                ret = r;
            }
        }
        if(ret == null) {
            LOGGER.debug("We were unable to find a longest route.  This is most likely because the collection apssed in was empty. ");
        }
        return ret;
    }

}
