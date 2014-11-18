package oarlib.route.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.List;

/**
 * Turns a route that consists of windy links into the compact Benavent representation (an ordered list of internal ids
 * that indicates the order that the required edges are traversed.
 * Created by oliverlum on 11/16/14.
 */
public class FlattenWindyRoute {

    public static TIntArrayList flattenWindyRoute(Route r) {

        TIntArrayList ans = new TIntArrayList();

        List<? extends Link<? extends Vertex>> path = r.getRoute();
        int n = path.size();
        for(int i = 0; i < n; i ++) {
            Link l = path.get(i);
            if(l.isRequired())
                ans.add(l.getId());
        }

        return ans;
    }
}
