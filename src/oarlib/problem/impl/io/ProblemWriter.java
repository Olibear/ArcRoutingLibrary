/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.problem.impl.io;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Vertex;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.AsymmetricLink;
import oarlib.link.impl.Edge;
import oarlib.link.impl.WindyEdge;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.problem.impl.rpp.WindyRPPZZTW;
import oarlib.vertex.impl.UndirectedVertex;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Writer to output various file formats.  Plans to use Gephi for visualization.
 *
 * @author Oliver
 */
public class ProblemWriter {

    private static final Logger LOGGER = Logger.getLogger(ProblemWriter.class);

    private ProblemFormat.Name mFormat;

    public ProblemWriter(ProblemFormat.Name format) {
        mFormat = format;
    }

    public ProblemFormat.Name getFormat() {
        return mFormat;
    }

    public void setFormat(ProblemFormat.Name newFormat) {
        mFormat = newFormat;
    }

    public boolean writeInstance(Problem p, String filename) throws UnsupportedFormatException {
        switch (mFormat) {
            case OARLib:
                return writeOarlibInstance(p, filename);
            case Campos:
                break;
            case Corberan:
                return writeCorberanInstance(p, filename);
            case Simple:
                break;
            case Yaoyuenyong:
                break;
            case JSON:
                return writeJSON(p, filename);
            case METIS:
                return writeMETISInstance(p, filename);
            case Zhang_Matrix_Windy:
                return writeWindyZhangMatrixInstance(p, filename);
            case Zhang_Matrix_WRPP:
                if(p instanceof WindyRPP)
                    return writeWRPPZhangMatrixInstance((WindyRPP)p, filename);
                else
                    throw new IllegalArgumentException("Currently, this type of not supported for this output format.");
            case Zhang_Matrix_Zigzag:
                if (p instanceof WindyRPPZZTW)
                    return writeZigzagZhangMatrixInstance((WindyRPPZZTW) p, filename);
                else
                    throw new IllegalArgumentException("Currently, this type of not supported for this output format.");

            default:
                break;
        }
        LOGGER.error("While the format seems to have been added to the Format.Name type list,"
                + " there doesn't seem to be an appropriate write method assigned to it.  Support is planned in the future," +
                "but not currently available");
        throw new UnsupportedFormatException();
    }

    private int[][] swapDepot(int[][] realMatrix, int depotId) {

        //init
        int n = realMatrix.length;
        int[][] ans = new int[n][];
        int[] depotRow = new int[n];
        int[] depotColumn = new int[n];
        int[] firstRow = new int[n];
        int[] firstColumn = new int[n];

        //copy it fast
        for (int i = 0; i < realMatrix.length; i++) {
            int[] aMatrix = realMatrix[i];
            int aLength = aMatrix.length;
            ans[i] = new int[aLength];
            System.arraycopy(aMatrix, 0, ans[i], 0, aLength);
        }

        //store 'em
        for (int i = 1; i < n; i++) {
            depotRow[i] = realMatrix[depotId][i];
            depotColumn[i] = realMatrix[i][depotId];
            firstRow[i] = realMatrix[1][i];
            firstColumn[i] = realMatrix[i][1];
        }

        //swap 'em
        for (int i = 1; i < n; i++) {
            ans[1][i] = depotRow[i];
            ans[i][1] = depotColumn[i];
            ans[depotId][i] = firstRow[i];
            ans[i][depotId] = firstColumn[i];
        }

        //clean up
        ans[1][1] = 0;
        ans[1][depotId] = depotRow[1];
        ans[depotId][1] = firstRow[depotId];
        ans[depotId][depotId] = 0;

        return ans;

    }

    private <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> boolean writeWindyZhangMatrixInstance(Problem<V, E, G> p, String filename) {
        try {

            //init
            PrintWriter pw = new PrintWriter(new File(filename));
            G g = p.getGraph();
            if(g.getType() != Graph.Type.WINDY)
                throw new IllegalArgumentException("Currently, this type of not supported for this output format.");
            int n = g.getVertices().size();
            int[][] matrix = new int[n + 1][n + 1]; //indices will be off by 1

            //write the deadhead matrix
            for(E e : g.getEdges()) {
                matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = e.getCost();
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = ((AsymmetricLink)e).getReverseCost();
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getCost();
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the serviceCost matrix
            for(E e : g.getEdges()) {
                if (e.isRequired()) {
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 1;//e.getServiceCost();
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = ((AsymmetricLink)e).getReverseServiceCost();
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getServiceCost();
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the meander time matrix
            for (E e : g.getEdges()) {
                if (e.isRequired()) {
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 1; //should probably handle general case later
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1; //should probably handle general case later
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the finish time matrix
            for (E e : g.getEdges()) {
                matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 1000000;
                if (!e.isDirected()) {
                    if (e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1000000;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1000000;
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the type matrix
            for (E e : g.getEdges()) {
                if (e.isRequired()) {
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 3;
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }


            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean writeZigzagZhangMatrixInstance(WindyRPPZZTW p, String filename) {
        try {

            //init
            PrintWriter pw = new PrintWriter(new File(filename));
            ZigZagGraph g = p.getGraph();

            int n = g.getVertices().size();
            int[][] matrix = new int[n + 1][n + 1]; //indices will be off by 1

            //write the deadhead matrix
            for (ZigZagLink e : g.getEdges()) {
                matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = e.getCost();
                if (!e.isDirected()) {
                    if (e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getReverseCost();
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getCost();
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the serviceCost matrix
            for (ZigZagLink e : g.getEdges()) {
                if (e.isRequired() || e.isReverseRequired()) {
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = e.getServiceCost();
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getReverseServiceCost();
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = ((AsymmetricLink)e).getReverseServiceCost();
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getServiceCost();
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the meander time matrix
            for (ZigZagLink e : g.getEdges()) {
                if (e.isRequired() || e.isReverseRequired()) {
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = (int) e.getZigzagCost();
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = (int) e.getZigzagCost();
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the finish time matrix
            for (ZigZagLink e : g.getEdges()) {
                matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = e.getTimeWindow().getSecond();
                if (!e.isDirected()) {
                    if (e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getTimeWindow().getSecond();
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getTimeWindow().getSecond();
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the type matrix
            for (ZigZagLink e : g.getEdges()) {
                if (e.isRequired() || e.isReverseRequired()) {
                    if(e.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY) {
                        matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 3;
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                    }
                    else if (e.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL) {
                        matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 3;
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                    } else {
                        matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 2;
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 2;
                    }
                }
                /*
                if(!e.isDirected()) {
                    if(e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 3;
                }*/
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }


            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean writeWRPPZhangMatrixInstance(WindyRPP p, String filename) {
        try {

            //init
            PrintWriter pw = new PrintWriter(new File(filename));
            WindyGraph g = p.getGraph();

            int n = g.getVertices().size();
            int[][] matrix = new int[n + 1][n + 1]; //indices will be off by 1

            //write the deadhead matrix
            for (WindyEdge e : g.getEdges()) {
                matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = e.getCost()+1;
                if (!e.isDirected()) {
                    if (e.isWindy())
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getReverseCost()+1;
                    else
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = e.getCost()+1;
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the serviceCost matrix
            for (WindyEdge e : g.getEdges()) {
                if (e.isRequired() || e.isReverseRequired()) {
                    System.out.println(e.toString());
                    matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 1;
                    matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }

            matrix = new int[n + 1][n + 1]; //indices will be off by 1
            //write the type matrix
            for (WindyEdge e : g.getEdges()) {
                    if(e.isRequired() || e.isReverseRequired()) {
                        matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 1;
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 1;
                    } else {
                        matrix[e.getFirstEndpointId()][e.getSecondEndpointId()] = 0;
                        matrix[e.getSecondEndpointId()][e.getFirstEndpointId()] = 0;
                    }
            }

            matrix = swapDepot(matrix, g.getDepotId());

            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    pw.print(matrix[i][j] + "\t");
                }
                pw.println();
            }


            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    private <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> boolean writeJSON(Problem<V, E, G> p, String filename) {

        try {
            G g = p.getGraph();
            PrintWriter pw = new PrintWriter(filename, "UTF-8");

            pw.println("{");

            //nodes
            pw.println("\t\"nodes\":");
            pw.println("\t[");

            String toAdd;
            Iterator<? extends Vertex> iter = g.getVertices().iterator();
            HashMap<Integer, Pair<Integer>> newCoords = transformCoordinates(g.getVertices());

            while(iter.hasNext()){

                Vertex v = iter.next();
                Pair<Integer> vizCoords = newCoords.get(v.getId());
                toAdd = "\t\t{";
                toAdd += "\"id\":"+v.getId();
                toAdd += ", \"name\":\""+ v.getLabel() + "\"";
                toAdd += ", \"x\":"+ vizCoords.getFirst();
                toAdd += ", \"y\":"+ vizCoords.getSecond();

                if(v.getId() == g.getDepotId())
                    toAdd += ", \"depot\":true";

                toAdd += "}";



                if(iter.hasNext())
                    toAdd +=",";

                pw.println(toAdd);

            }

            pw.println("\t],");

            //links
            pw.println("\t\"links\":");
            pw.println("\t[");

            Iterator<? extends Link> iter2 = g.getEdges().iterator();
            while(iter2.hasNext()) {

                Link l = iter2.next();

                toAdd = "\t\t{";
                toAdd += "\"id\":"+l.getId();
                toAdd += ", \"label\":\""+ l.getLabel() + "\"";
                toAdd += ", \"source\":"+ l.getFirstEndpointId();
                toAdd += ", \"sink\":"+ l.getSecondEndpointId();
                toAdd += ", \"directed\":"+ l.isDirected();
                toAdd += ", \"required\":"+l.isRequired();
                toAdd += ", \"type\":\""+ l.getType() + "\"";
                toAdd += ", \"speed\":"+ l.getMaxSpeed();
                toAdd += ", \"zone\":\""+ l.getZone() + "\"}";

                if(iter2.hasNext())
                    toAdd +=",";

                pw.println(toAdd);

            }

            pw.println("\t]");

            pw.println("}");

            pw.close();



            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /**
     * @return key = v.getId(), val = newX, newY
     */
    private static HashMap<Integer, Pair<Integer>> transformCoordinates(Collection<? extends Vertex> verts) {
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;

        for(Vertex v : verts) {
            if(v.getX() > maxX)
                maxX = v.getX();
            if(v.getX() < minX)
                minX = v.getX();
            if(v.getY() > maxY)
                maxY = v.getY();
            if(v.getY() < minY)
                minY = v.getY();
        }

        HashMap<Integer, Pair<Integer>> ans = new HashMap<Integer, Pair<Integer>>();

        //scale
        int newX, newY;
        for(Vertex v : verts) {
            newX = (int)((v.getX() - minX) * 1000000);
            newY = (int)((v.getY() - minY) * -1000000);
            ans.put(v.getId(), new Pair<Integer>(newX, newY));
        }

        return ans;

    }

    private <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> boolean writeCorberanInstance(Problem<V, E, G> p, String filename) {
        try {
            G g = p.getGraph();
            if (g.getType() != Graph.Type.WINDY)
                throw new IllegalArgumentException("Currently, this type of not supported for this output format.");


            int n = g.getVertices().size();
            int mReq = 0;
            for (E edge : g.getEdges()) {
                if (edge.isRequired())
                    mReq++;
            }
            int mNoReq = g.getEdges().size() - mReq;

            //front matter
            PrintWriter pw = new PrintWriter(filename, "UTF-8");
            pw.println("NOMBRE : " + p.getName());
            pw.println("COMENTARIO : " + g.getDepotId() + " depot");
            pw.println("VERTICES : " + g.getVertices().size());
            pw.println("ARISTAS_REQ : " + mReq);
            pw.println("ARISTAS_NOREQ : " + mNoReq);

            pw.println("LISTA_ARISTAS_REQ :");

            String line;
            for (E edge : g.getEdges()) {
                if (edge.isRequired()) {
                    line = "(" + edge.getFirstEndpointId() + "," + edge.getSecondEndpointId() + ")";
                    line += " coste " + edge.getCost() + " " + ((WindyEdge) edge).getReverseCost();
                    pw.println(line);
                }
            }

            pw.println("LISTA_ARISTAS_NOREQ :");
            for (E edge : g.getEdges()) {
                if (!edge.isRequired()) {
                    line = "(" + edge.getFirstEndpointId() + "," + edge.getSecondEndpointId() + ")";
                    line += " coste " + edge.getCost() + " " + ((WindyEdge) edge).getReverseCost();
                    pw.println(line);
                }
            }

            pw.println("COORDENADAS :");
            for (int i = 1; i <= n; i++) {
                V v = g.getVertex(i);
                line = i + " " + v.getX() + " " + v.getY();
                pw.println(line);
            }


            pw.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> boolean writeMETISInstance(Problem<V, E, G> p, String filename) {
        try {
            G g = p.getGraph();
            Vertex first, second;
            UndirectedGraph g2 = new UndirectedGraph(g.getVidCounter() - 1);
            Collection<V> gVertices = g.getVertices();
            TIntObjectHashMap<UndirectedVertex> g2Vertices = g2.getInternalVertexMap();

            if (g.getClass() != UndirectedGraph.class) {
                for (E l : g.getEdges()) {
                    first = g2Vertices.get(l.getEndpoints().getFirst().getId());
                    second = g2Vertices.get(l.getEndpoints().getSecond().getId());
                    if (first == null)
                        System.out.println("DEBUG");
                    if (!first.getNeighbors().containsKey(second))
                        g2.addEdge(first.getId(), second.getId(), l.getCost());
                }
                for (V v : gVertices) {
                    g2Vertices.get(v.getId()).setCost(v.getCost());
                }
            }

            //front matter
            PrintWriter pw = new PrintWriter(filename, "UTF-8");
            pw.println("%");
            pw.println("% This is a METIS file generated by the Open Source, Arc-Routing Library (OAR Lib).");
            pw.println("% For more information on the METIS Library, or the format please visit: ");
            pw.println("% http://glaros.dtc.umn.edu/gkhome/metis/metis/overview");
            pw.println("%");

            //the header
            int n = g.getVertices().size();
            int m = g2.getEdges().size();
            String header = "";
            header = header + n + " " + m + " " + "011" + " 1";
            pw.println(header);

            TIntObjectHashMap<UndirectedVertex> indexedVertices = g2.getInternalVertexMap();
            UndirectedVertex temp;
            HashMap<UndirectedVertex, ArrayList<Edge>> tempNeighbors;
            boolean shownWarning = false;
            for (int i = 1; i <= n; i++) {
                String line = "";
                temp = indexedVertices.get(i);

                line += temp.getCost() + " ";
                tempNeighbors = temp.getNeighbors();
                for (UndirectedVertex neighbor : tempNeighbors.keySet()) {
                    line += neighbor.getId() + " ";
                    line += tempNeighbors.get(neighbor).get(0).getCost() + " ";
                    if (tempNeighbors.get(neighbor).size() > 1 && !shownWarning) {
                        System.out.println("Multigraphs are not currently supported; we shall only use one of the edges connecting these vertices.");
                        shownWarning = true;
                    }
                }
                pw.println(line);
            }

            pw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> boolean writeOarlibInstance(Problem<V, E, G> p, String filename) {
        try {

            G g = p.getGraph();
            //front matter
            PrintWriter pw = new PrintWriter(filename, "UTF-8");
            pw.println("%");
            pw.println("% This is a file generated by the Open Source, Arc-Routing Library (OAR Lib).");
            pw.println("% For more information on OAR Lib, or the format please visit: ");
            pw.println("% https://github.com/Olibear/ArcRoutingLibrary ");
            pw.println("%");

            ProblemAttributes pa = p.getProblemAttributes();
            boolean isWindy = pa.getmGraphType() == Graph.Type.WINDY;

            //the header
            pw.println();
            pw.println("================================");
            pw.println("Format: OAR Lib");
            pw.println("Graph Type:" + pa.getmGraphType());
            pw.println("Problem Type:" + pa.getmProblemType());
            pw.println("Fleet Size:" + pa.getmNumVehicles());
            pw.println("Number of Depots:" + pa.getmNumDepots());

            String depotIDString = "Depot ID(s):" + g.getDepotId();

            pw.println(depotIDString);
            if (pa.getmProperties() != null) {
                String addedProps = "Additional Properties:";
                for (ProblemAttributes.Properties prop : pa.getmProperties()) {
                    addedProps += prop.toString() + ",";
                }
                pw.println(addedProps);
            }
            pw.println("N:" + g.getVertices().size());
            pw.println("M:" + g.getEdges().size());
            pw.println("================================");


            //Formatting details
            pw.println();
            pw.println("LINKS");

            String lineFormat = "Line Format:V1,V2,COST,HIGHWAY_TYPE,NAME,MAX_SPEED,ZONE";
            if (isWindy)
                lineFormat += ",REVERSE COST";
            lineFormat += ",REQUIRED";

            pw.println(lineFormat);

            //edges
            String line;
            int m = g.getEdges().size();
            for (int i = 1; i <= m; i++) {
                E e = g.getEdge(i);
                line = "";
                line += e.getEndpoints().getFirst().getId() + ","
                        + e.getEndpoints().getSecond().getId() + ","
                        + e.getCost() + ","
                        + e.getType() + ","
                        + e.getLabel() + ","
                        + e.getMaxSpeed() + ","
                        + e.getZone();
                if (isWindy)
                    line += "," + ((WindyEdge) e).getReverseCost();
                line += "," + e.isRequired();
                pw.println(line);
            }

            pw.println("===========END LINKS============");

            boolean hasVertexCoords = false;
            for (V v : g.getVertices())
                if (v.hasCoordinates()) {
                    hasVertexCoords = true;
                    break;
                }

            if (hasVertexCoords) {
                pw.println();
                pw.println("VERTICES");

                lineFormat = "Line Format:x,y";
                pw.println(lineFormat);

                //vertices
                int n = g.getVertices().size();
                for (int i = 1; i <= n; i++) {
                    V v = g.getVertex(i);
                    line = v.getX() + "," + v.getY();
                    pw.println(line);
                }
                pw.println("===========END VERTICES============");
            }

            //TODO: Other properties; we have to have an automated way of fetching them

            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
