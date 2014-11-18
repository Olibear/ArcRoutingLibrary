package oarlib.improvements.impl;

import oarlib.core.Route;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.problem.impl.rpp.WindyRPP;

import java.util.Collection;

/**
 * Created by oliverlum on 11/16/14.
 */
public class TwoInterchange extends IntraRouteImprovementProcedure<WindyRPP> {


    protected TwoInterchange(WindyRPP g, Collection<Route> candidateRoute) {
        super(g, candidateRoute);
    }

    @Override
    protected Route improveRoute(Route r) {


        return null;
    }


}
