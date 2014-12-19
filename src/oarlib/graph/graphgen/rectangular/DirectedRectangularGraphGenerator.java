package oarlib.graph.graphgen.rectangular;

import oarlib.graph.impl.DirectedGraph;
import oarlib.vertex.impl.DirectedVertex;

import java.util.Random;

/**
 * Created by oliverlum on 12/14/14.
 */
public class DirectedRectangularGraphGenerator extends RectangularGraphGenerator<DirectedGraph> {

    @Override
    protected DirectedGraph generate(int n, int maxCost, double reqDensity, boolean positiveCosts) {

        //trivial case
        if(n == 1)
            return new DirectedGraph(1);

        Random rng = new Random();

        DirectedGraph ans = new DirectedGraph((int)Math.pow(n,2));
        double interval = 100.0/(n - 1);

        int index = 1;
        int cost, cost2, coeff;
        int rngCeiling = maxCost+1;

        try {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    //modify properties
                    DirectedVertex newV = ans.getVertex(index);
                    newV.setLabel((j + 1) + "," + (i + 1));
                    newV.setCoordinates(j * interval, i * interval);

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling);
                        cost2 = rng.nextInt(rngCeiling);
                    }
                    else {
                        if (Math.random() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = rng.nextInt(rngCeiling) * coeff;
                        cost2 = rng.nextInt(rngCeiling) * coeff;
                    }

                    //add horizontal edges
                    if (j > 0) {
                        ans.addEdge(index, index - 1, cost, Math.random() < reqDensity);
                        ans.addEdge(index - 1, index, cost2, Math.random() < reqDensity);
                    }

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling);
                        cost2 = rng.nextInt(rngCeiling);
                    }
                    else {
                        if (Math.random() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = rng.nextInt(rngCeiling) * coeff;
                        cost2 = rng.nextInt(rngCeiling) * coeff;
                    }
                    //add vertical edges
                    if(i > 0) {
                        ans.addEdge(index, index - n, cost, Math.random() < reqDensity);
                        ans.addEdge(index - n, index, cost2, Math.random() < reqDensity);
                    }

                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ans;
    }
}
