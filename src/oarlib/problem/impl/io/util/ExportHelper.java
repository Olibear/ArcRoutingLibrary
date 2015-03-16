package oarlib.problem.impl.io.util;

import oarlib.core.*;
import oarlib.metrics.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by oliverlum on 1/18/15.
 */
public class ExportHelper {

    /**
     * Writes a csv file that can be imported to Excel for easy viewing / analysis.
     *
     * @param instances      - Problem instances to e solved by solvers of the type provided
     * @param metrics        - Which metrics to evaluate and include in the spreadsheet
     * @param solverInstance - an instance of the solver to be run on the test problems provided
     * @param outputFile     - the file location (must end in .csv) for the output of the function
     * @throws IllegalArgumentException
     */
    public static <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> void exportToExcel(ArrayList<Problem<V, E, G>> instances, ArrayList<Metric.Type> metrics, Solver<V, E, G> solverInstance, String outputFile) throws IllegalArgumentException {

        //check for fileType
        if (!outputFile.endsWith(".csv"))
            throw new IllegalArgumentException("The file must be a .csv file for import into Microsoft Excel.");

        try {

            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

            int numInstances = instances.size();
            int numMetrics = metrics.size();

            ArrayList<String> parameterTitles = new ArrayList<String>();

            //header
            pw.print(outputFile.replace(".csv", ""));
            pw.print(",");
            pw.print(solverInstance.getSolverName());
            pw.println();
            pw.println();
            pw.print("Instance Name");
            for (int j = 0; j < numMetrics; j++) {
                pw.print(",");
                pw.print(metrics.get(j).toString());
            }
            pw.print(",");
            pw.print("Time (seconds)");
            pw.print(",");
            for (String key : solverInstance.getProblemParameters().keySet()) {
                pw.print(",");
                pw.print(key);
                parameterTitles.add(key);
            }
            pw.println();
            pw.println();

            for (int i = 0; i < numInstances; i++) {
                Problem<V, E, G> currProb = instances.get(i);
                pw.print(currProb.getName().replace(",", ""));
                pw.print(",");

                //run the solver
                Solver<V, E, G> currSolver = solverInstance.instantiate(currProb);
                long start = System.nanoTime();
                Collection<? extends Route> currSol = currSolver.trySolve();
                long end = System.nanoTime();

                //Metrics
                for (int j = 0; j < numMetrics; j++) {

                    Metric temp = null;
                    switch (metrics.get(j)) {
                        case ATD:
                            temp = new AverageTraversalMetric(currProb.getGraph());
                            break;
                        case ROI:
                            temp = new RouteOverlapMetric(currProb.getGraph());
                            break;
                        case EDGECOST:
                            temp = new EdgeCostMetric(currProb.getGraph());
                            break;
                        case N:
                            temp = new NumNodesMetric(currProb.getGraph());
                            break;
                        case M:
                            temp = new NumLinksMetric(currProb.getGraph());
                            break;
                        case AVG:
                            temp = new AvgMetric();
                            break;
                        case DEV:
                            temp = new DevMetric();
                            break;
                        case MAX:
                            temp = new MaxMetric();
                            break;
                        case MIN:
                            temp = new MinMetric();
                            break;
                        case SUM:
                            temp = new SumMetric();
                            break;
                        case VAR:
                            temp = new VarMetric();
                            break;
                        case DEPDIST:
                            temp = new DepotDistanceToCenter(currProb.getGraph());
                            break;
                    }


                    pw.print(temp.evaluate(currSol));
                    pw.print(",");
                }

                //Time
                pw.print((end - start) / 1000000000);
                pw.print(",");

                pw.print(",");
                //Parameters
                HashMap<String, Double> parameters = currSolver.getProblemParameters();
                for (int j = 0; j < parameterTitles.size(); j++) {
                    pw.print(parameters.get(parameterTitles.get(j)));
                    pw.print(",");
                }


                pw.println();
            }

            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }
}
