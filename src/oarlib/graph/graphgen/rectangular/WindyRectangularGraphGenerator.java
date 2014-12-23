package oarlib.graph.graphgen.rectangular;

import oarlib.graph.impl.WindyGraph;
import oarlib.vertex.impl.WindyVertex;

/**
 * Created by oliverlum on 12/14/14.
 */
public class WindyRectangularGraphGenerator extends RectangularGraphGenerator<WindyGraph> {
    @Override
    protected WindyGraph generate(int n, int maxCost, double reqDensity, boolean positiveCosts) {

        //trivial case
        if(n == 1)
            return new WindyGraph(1);

        WindyGraph ans = new WindyGraph((int)Math.pow(n,2));
        double interval = 100.0/(n - 1);

        int index = 1;
        int cost, revCost, coeff;
        int rngCeiling = maxCost;

        try {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    //modify properties
                    WindyVertex newV = ans.getVertex(index);
                    newV.setLabel((j + 1) + "," + (i + 1));
                    newV.setCoordinates(j * interval, i * interval);

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                        revCost = rng.nextInt(rngCeiling) + 1;
                    }
                    else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                        revCost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }

                    //add horizontal edges
                    if (j > 0) {
                        ans.addEdge(index, index - 1, cost, revCost, rng.nextDouble() < reqDensity);
                    }

                    //cost
                    if (positiveCosts) {
                        cost = rng.nextInt(rngCeiling) + 1;
                        revCost = rng.nextInt(rngCeiling) + 1;
                    }
                    else {
                        if (rng.nextDouble() < .5)
                            coeff = 1;
                        else
                            coeff = -1;
                        cost = (rng.nextInt(rngCeiling) + 1) * coeff;
                        revCost = (rng.nextInt(rngCeiling) + 1) * coeff;
                    }
                    //add vertical edges
                    if(i > 0) {
                        ans.addEdge(index, index - n, cost, revCost, rng.nextDouble() < reqDensity);
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
