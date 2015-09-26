package oarlib.bmgt831;

import oarlib.graph.impl.UndirectedGraph;
import oarlib.link.impl.Edge;

/**
 * Created by oliverlum on 5/2/15.
 */
public class EquatorialInstanceGenerator {

    private int mNumTrucks; // defines how many great circles
    private int mNumDrones; // defines how many
    private int mAlapha; // we'll set this to make sure they're fast enough to coincide

    public EquatorialInstanceGenerator(int numTrucks, int numDrones) {

        mNumDrones = numDrones;
        mNumTrucks = numTrucks;
        mAlapha = 1;

    }

    public UndirectedGraph generateInstance() {

        int nodesPerGreatCircle = 4;
        int nodesPerLesserCircle = nodesPerGreatCircle + 1;

        //generate as in Xingyin's spec
        int n = mNumTrucks * (nodesPerGreatCircle + mNumDrones * nodesPerLesserCircle) + 1;
        UndirectedGraph ans = new UndirectedGraph(n);

        try {
            int index2;
            int index = 2;
            for (int i = 1; i <= mNumTrucks; i++) {

                //add connection to depot
                ans.addEdge(1, index, 2);

                //add connections for the great circle
                for (int j = 1; j <= nodesPerGreatCircle - 1; j++) {
                    ans.addEdge(index, index + 1, 2);
                    index++;
                }

                //add connection to depot
                ans.addEdge(index, 1, 2);

                index++;
                //add connections to the lesser circles
                for (int j = 1; j <= mNumDrones; j++) {

                    index2 = index - nodesPerGreatCircle - 1 - (j - 1) * nodesPerLesserCircle;
                    ans.addEdge(1, index, 1);
                    ans.addEdge(index, index2 + 1, 1);
                    index++;
                    index2++;

                    for (int k = 2; k <= nodesPerLesserCircle - 1; k++) {
                        ans.addEdge(index, index2, 1);
                        ans.addEdge(index, index2 + 1, 1);
                        index++;
                        index2++;
                    }

                    ans.addEdge(index, index2, 1);
                    ans.addEdge(index, 1, 1);
                    index++;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        for (Edge e : ans.getEdges())
            System.out.println(e.toString());
        return ans;
    }


}
