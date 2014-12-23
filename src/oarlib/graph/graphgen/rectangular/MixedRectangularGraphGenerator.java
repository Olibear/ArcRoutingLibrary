package oarlib.graph.graphgen.rectangular;

import oarlib.graph.impl.MixedGraph;
import oarlib.vertex.impl.MixedVertex;

import java.util.Random;

/**
 * Created by oliverlum on 12/14/14.
 */
public class MixedRectangularGraphGenerator extends RectangularGraphGenerator<MixedGraph> {

    @Override
    protected MixedGraph generate(int n, int maxCost, double reqDensity, boolean positiveCosts) {

        //trivial case
        if(n == 1)
            return new MixedGraph(1);

        Random rng = new Random();

        MixedGraph ans = new MixedGraph((int)Math.pow(n,2));
        double interval = 100.0/(n - 1);

        int index = 1;
        int cost, coeff;
        int rngCeiling = maxCost;

        try {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    //modify properties
                    MixedVertex newV = ans.getVertex(index);
                    newV.setLabel((j + 1) + "," + (i + 1));
                    newV.setCoordinates(j * interval, i * interval);

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                    }
                    else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }

                    //add horizontal edges
                    if (j > 0) {
                        if (rng.nextDouble() < .5) {
                            ans.addEdge(index, index - 1, cost, true, rng.nextDouble() < reqDensity);
                            ans.addEdge(index - 1, index, cost, true, rng.nextDouble() < reqDensity);
                        }
                        else
                            ans.addEdge(index, index - 1, cost, false, rng.nextDouble() < reqDensity);
                    }

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                    }
                    else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }
                    //add vertical edges
                    if(i > 0) {
                        if (rng.nextDouble() < .5) {
                            ans.addEdge(index, index - n, cost, true, rng.nextDouble() < reqDensity);
                            ans.addEdge(index - n, index, cost, true, rng.nextDouble() < reqDensity);
                        }
                        else
                            ans.addEdge(index, index - n, cost, false, rng.nextDouble() < reqDensity);
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
