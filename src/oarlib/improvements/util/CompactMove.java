package oarlib.improvements.util;

import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import org.apache.log4j.Logger;

/**
 * Container to hold moves frequently made by improvement procedures.
 *
 * Created by oliverlum on 11/29/14.
 */
public class CompactMove<V extends Vertex, E extends Link<V>>  {

    private static final Logger LOGGER = Logger.getLogger(CompactMove.class);
    private Route<V,E> mFrom;
    private Route<V,E> mTo;
    private int mFromPos;
    private int mToPos;
    private boolean prudentDirection;
    private boolean pdSet;

    public CompactMove(Route<V,E> from, Route<V,E> to, int fromPos, int toPos) throws IllegalArgumentException{
        mFrom = from;
        mTo = to;

        int fromSize = mFrom.getCompactRepresentation().size();
        int toSize = mTo.getCompactRepresentation().size();

        if(fromPos < 0 || fromPos >= fromSize) {
            LOGGER.warn("This is an invalid position in the route.  This move may be legal if it is chained together with others.  Remember, this argument is intended to be from the flattened representation.");
            //throw new IllegalArgumentException();
        }
        if(toPos < 0 || toPos > toSize) {
            LOGGER.warn("This is an invalid position in the route.  This move may be legal if it is chained together with others.  Remember, this argument is intended to be from the flattened representation.");
            //throw new IllegalArgumentException();
        }

        mFromPos = fromPos;
        mToPos = toPos;
        pdSet = false;
        prudentDirection = true;
    }

    public Route<V,E> getFrom() {
        return mFrom;
    }

    public Route<V,E> getTo() {
        return mTo;
    }

    public int getFromPos() {
        return mFromPos;
    }

    public int getToPos() {
        return mToPos;
    }

    public boolean isPrudentDirection() {
        if(!pdSet)
            LOGGER.warn("The best direction for this move hasn't been set, so we're defaulting to true.");
        return prudentDirection;
    }

    public void setPrudentDirection(boolean prudentDirection) {
        this.prudentDirection = prudentDirection;
        pdSet = true;
    }
}
