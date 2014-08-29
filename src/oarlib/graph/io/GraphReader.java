package oarlib.graph.io;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.MixedEdge;
import oarlib.exceptions.FormatMismatchException;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Reader to accept various file formats, and store them as a graph object.
 *
 * @author Oliver
 */
public class GraphReader {
    private GraphFormat.Name mFormat;

    public GraphReader(GraphFormat.Name format) {
        mFormat = format;
    }

    public void setFormat(GraphFormat.Name newFormat) {
        mFormat = newFormat;
    }

    public GraphFormat.Name getFormat() {
        return mFormat;
    }

    public Graph<?, ?> readGraph(String fileName) throws UnsupportedFormatException, FormatMismatchException {
        switch (mFormat) {
            case Simple:
                return readSimpleGraph(fileName);
            case Corberan:
                return readCorberanGraph(fileName);
            case Yaoyuenyong:
                return readYaoyuenyongGraph(fileName);
            case Campos:
                return readCamposGraph(fileName);
            case DIMACS_Modified:
                break;
            case METIS:
                return readMETISGraph(fileName);
            case OARLib:
                //TODO: I should probably write a reader for my own format
                break;
        }
        throw new UnsupportedFormatException("While the format seems to have been added to the Format.Name type list,"
                + " there doesn't seem to be an appropriate read method assigned to it.  Support is planned in the future," +
                "but not currently available");
    }

    private Graph<?, ?> readMETISGraph(String fileName) throws FormatMismatchException {
        try {
            //ans, so far I only know of undirected graphs for this type
            UndirectedGraph ans = new UndirectedGraph();

            //file reading vars
            String line;
            String[] temp;
            File graphFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(graphFile));

            //header info
            int n = 0;
            int numWeightsPerVertex = 1;

            boolean hasEdgeWeights = false;
            boolean hasVertexWeights = false;
            boolean hasVertexSizes = false;

            line = br.readLine();

            //skip any header
            while (line.startsWith("%"))
                line = br.readLine();
            temp = line.split(",\\s+|:");

            if (temp.length == 2) {
                //n, and m
                n = Integer.parseInt(temp[0]);
            }
            if (temp.length > 2) {
                //fmt
                if (temp[2].charAt(2) == '0') {
                    hasEdgeWeights = false;
                    System.out.println("This file does not contain information about edge weights.  The graph will attempt to be read, but this seems strange, as this is an Arc-Routing Library");
                }
                if (temp[2].charAt(1) == '0')
                    hasVertexWeights = false;
                if (temp[2].charAt(0) == '0')
                    hasVertexSizes = false;
            }
            if (temp.length > 3) {
                //nconn
                numWeightsPerVertex = Integer.parseInt(temp[3]);
                if (numWeightsPerVertex > 1)
                    System.out.println("The graph specified in this file has multiple weights per vertex.  Currently, there is no support for multiple vertex weights, so only the first will be used.");
            }
            if (temp.length > 4) {
                br.close();
                throw new FormatMismatchException("This does not appear to be a METIS graph file.  The header is malformed.");
            }

            //vertices
            for (int i = 0; i < n; i++) {
                ans.addVertex(new UndirectedVertex(""));
            }

            HashMap<Integer, UndirectedVertex> indexedVertices = ans.getInternalVertexMap();

            //now read the rest
            for (int i = 1; i <= n; i++) {
                if ((line = br.readLine()) == null) {
                    br.close();
                    throw new FormatMismatchException("This does not appear to be a valid METIS graph file.  There are not as many lines as vertices specified in the header.");
                }

                temp = line.split(",\\s+|:");
                //if there's a vertex size, read it
                if (hasVertexSizes) {
                    indexedVertices.get(i).setSize(Integer.parseInt(temp[0]));
                }
                if (hasVertexWeights) {
                    if (hasVertexSizes)
                        indexedVertices.get(i).setCost(Integer.parseInt(temp[1]));
                    else
                        indexedVertices.get(i).setCost(Integer.parseInt(temp[0]));
                }
                if (hasEdgeWeights) {
                    int start = 0;
                    if (hasVertexSizes)
                        start++;
                    if (hasVertexWeights)
                        start += numWeightsPerVertex;
                    int end = temp.length;
                    for (int j = start; j < end; j += 2) {
                        //to avoid redundancy in the file
                        if (i < Integer.parseInt(temp[j]))
                            ans.addEdge(i, Integer.parseInt(temp[j]), Integer.parseInt(temp[j + 1]));
                    }
                }
            }

            br.close();
            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Graph<?, ?> readCamposGraph(String fileName) throws FormatMismatchException {
        try {
            //ans, so far I only know of directed graphs for this type
            DirectedGraph ans = new DirectedGraph();

            //file reading vars
            String line;
            String[] temp;
            File graphFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(graphFile));

            //header info
            int n = 0;
            int m = 0;
            line = br.readLine();
            temp = line.split(",\\s+|:");
            boolean isReq = false;

            n = Integer.parseInt(temp[0].trim()); //first line is number of vertices

            line = br.readLine(); //second line is number of vertices in the simplified graph

            line = br.readLine(); // third line is the number of arcs

            temp = line.split(",");
            m = Integer.parseInt(temp[0].trim());

            line = br.readLine(); //fourth line is # of connected components of the simplified graph

            line = br.readLine(); //fifth line is number of vertices belonging to each of those connected components

            //construct the ans graph
            for (int i = 0; i < n; i++) {
                ans.addVertex(new DirectedVertex("orig"));
            }
            for (int i = 0; i < m; i++) {
                line = br.readLine();
                if (line == null) {
                    br.close();
                    throw new FormatMismatchException("Not enough lines to match the claimed number of arcs");
                }

                temp = line.split(",");
                if (temp.length < 4) {
                    br.close();
                    throw new FormatMismatchException("This line doesn't have the required components.");
                }

                isReq = Integer.parseInt(temp[3].trim()) == 1;
                ans.addEdge(Integer.parseInt(temp[0].trim()), Integer.parseInt(temp[1].trim()), "orig", Integer.parseInt(temp[2].trim()), isReq);
            }
            br.close();
            return ans;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Graph<?, ?> readYaoyuenyongGraph(String fileName) throws FormatMismatchException {
        try {    //file reading vars
            String line;
            String type = "";
            String[] temp;
            File graphFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(graphFile));

            //header info
            int n = 0;
            int m = 0;
            line = br.readLine();
            temp = line.split(",");

            //graph type
            type = temp[0];
            n = Integer.parseInt(temp[2]);
            m = Integer.parseInt(temp[3]);

            //split on graph type

            //undirected
            if (type.equals("1")) {
                UndirectedGraph ans = new UndirectedGraph();
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new UndirectedVertex("original"));
                }
                for (int i = 0; i < m; i++) {
                    line = br.readLine();
                    temp = line.split(",");

                    ans.addEdge(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), "original", Integer.parseInt(temp[2]));
                }

                br.close();
                return ans;
            }
            //directed
            else if (type.equals("2")) {
                DirectedGraph ans = new DirectedGraph();
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new DirectedVertex("original"));
                }
                for (int i = 0; i < m; i++) {
                    line = br.readLine();
                    temp = line.split(",");

                    ans.addEdge(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), "original", Integer.parseInt(temp[2]));
                }

                br.close();
                return ans;
            }
            //mixed
            else if (type.equals("3")) {
                MixedGraph ans = new MixedGraph();
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new MixedVertex("original"));
                }
                for (int i = 0; i < m; i++) {
                    line = br.readLine();
                    temp = line.split(",");

                    boolean directed = (temp[4].equals("1"));
                    ans.addEdge(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), "original", Integer.parseInt(temp[2]), directed);
                }

                br.close();
                return ans;
            } else {
                br.close();
                throw new FormatMismatchException("Unrecognized Type.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Graph<?, ?> readCorberanGraph(String fileName) throws FormatMismatchException {
        try {
            String line;
            String type = "";
            String[] temp;
            File graphFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(graphFile));
            //header info
            int n = 0;
            int m = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("NOMBRE")) {
                    temp = line.split("\\s+|:");
                    if (temp[3].startsWith("MA") || temp[3].startsWith("MB"))
                        type = "Mixed";
                    else if (temp[3].startsWith("WA") || temp[3].startsWith("WB"))
                        type = "Windy";
                    else if (temp[3].startsWith("A") || temp[3].startsWith("M") || temp[3].startsWith("m"))
                        type = "WindyRural";
                    else {
                        br.close();
                        throw new FormatMismatchException("We could not figure out what type of graph this is.");
                    }
                } else if (line.contains("VERTICES")) {
                    temp = line.split("\\s+|:");
                    n = Integer.parseInt(temp[temp.length - 1]);
                } else if (line.contains("ARISTAS")) {
                    temp = line.split("\\s+|:");
                    m = Integer.parseInt(temp[temp.length - 1]);
                    break;
                } else if (line.contains("RISTAS_REQ")) {
                    temp = line.split("\\s+|:");
                    m += Integer.parseInt(temp[temp.length - 1]);
                } else if (line.contains("RISTAS_NOREQ")) {
                    temp = line.split("\\s+|:");
                    m += Integer.parseInt(temp[temp.length - 1]);
                    break;
                }
            }

            if (n == 0 || m == 0) {
                br.close();
                throw new FormatMismatchException("We could not detect any vertices (edges) in the file.");
            }
            //now split off into types
            if (type.equals("Mixed")) {
                MixedGraph ans = new MixedGraph();
                int tailId;
                int headId;
                int cost1;
                int cost2;
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new MixedVertex("original"));
                }
                HashMap<Integer, MixedVertex> ansVertices = ans.getInternalVertexMap();
                br.readLine();
                br.readLine();
                int index;
                while ((line = br.readLine()) != null) {
                    if (line.contains("ARISTAS"))
                        break;
                    temp = line.split("\\s+|:|\\)|,|\\(");
                    index = 1;
                    if (temp[index].isEmpty())
                        index++;
                    tailId = Integer.parseInt(temp[index++]);
                    if (temp[index].isEmpty())
                        index++;
                    if (temp[index].isEmpty())
                        index++;
                    headId = Integer.parseInt(temp[index++]);
                    index += 2;
                    cost1 = Integer.parseInt(temp[index++]);
                    cost2 = Integer.parseInt(temp[index]);
                    if (cost1 == 99999999) //backwards arc
                    {
                        ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(headId), ansVertices.get(tailId)), cost2, true));
                    } else if (cost2 == 99999999) //forwards arc
                    {
                        ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(tailId), ansVertices.get(headId)), cost1, true));
                    } else // edge
                    {
                        ans.addEdge(new MixedEdge("original", new Pair<MixedVertex>(ansVertices.get(tailId), ansVertices.get(headId)), cost1, false));
                    }
                }
                br.close();
                return ans;
            } else if (type.equals("Windy")) {
                WindyGraph ans = new WindyGraph();
                int tailId;
                int headId;
                int cost1;
                int cost2;
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new WindyVertex("original"));
                }
                br.readLine();
                br.readLine();
                int index;
                while ((line = br.readLine()) != null) {
                    if (line.contains("ARISTAS"))
                        break;
                    temp = line.split("\\s+|:|\\)|,|\\(");
                    index = 1;
                    if (temp[index].isEmpty())
                        index++;
                    tailId = Integer.parseInt(temp[index++]);
                    if (temp[index].isEmpty())
                        index++;
                    if (temp[index].isEmpty())
                        index++;
                    headId = Integer.parseInt(temp[index++]);
                    index += 2;
                    cost1 = Integer.parseInt(temp[index++]);
                    cost2 = Integer.parseInt(temp[index]);

                    ans.addEdge(tailId, headId, "original", cost1, cost2);

                }
                br.close();
                return ans;
            } else if (type.equals("WindyRural")) {
                WindyGraph ans = new WindyGraph();
                int tailId;
                int headId;
                int cost1;
                int cost2;
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new WindyVertex("original"));
                }
                //	br.readLine();
                //	br.readLine();
                int index;
                while ((line = br.readLine()) != null) {
                    if (line.contains("LISTA_ARISTAS_REQ")) //in-process the required guys
                    {
                        while ((line = br.readLine()) != null) {
                            if (line.contains("LISTA_ARISTAS_NOREQ")) {
                                break;
                            }

                            temp = line.split("\\s+|:|\\)|,|\\(");
                            index = 1;
                            if (temp[index].isEmpty())
                                index++;
                            tailId = Integer.parseInt(temp[index++]);
                            if (temp[index].isEmpty())
                                index++;
                            if (temp[index].isEmpty())
                                index++;
                            headId = Integer.parseInt(temp[index++]);
                            index += 2;
                            cost1 = Integer.parseInt(temp[index++]);
                            cost2 = Integer.parseInt(temp[index]);

                            ans.addEdge(tailId, headId, "original", cost1, cost2, true);
                        }
                        while ((line = br.readLine()) != null) {
                            temp = line.split("\\s+|:|\\)|,|\\(");
                            if (temp.length == 1)
                                break;
                            index = 1;
                            if (temp[index].isEmpty())
                                index++;
                            tailId = Integer.parseInt(temp[index++]);
                            if (temp[index].isEmpty())
                                index++;
                            if (temp[index].isEmpty())
                                index++;
                            headId = Integer.parseInt(temp[index++]);
                            index += 2;
                            cost1 = Integer.parseInt(temp[index++]);
                            cost2 = Integer.parseInt(temp[index]);

                            ans.addEdge(tailId, headId, "original", cost1, cost2, false);
                        }
                    }

                }
                br.close();
                return ans;
            } else {
                br.close();
                throw new FormatMismatchException("We don't currently support the type of graph right now.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Graph<?, ?> readSimpleGraph(String fileName) throws FormatMismatchException {
        try {
            String type; //first line of DIMACS_Modified
            String header; //second line of DIMACS_Modified
            File graphFile = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(graphFile));
            //header info
            type = br.readLine();
            if (type == null) {
                br.close();
                throw new FormatMismatchException("There were no readable lines in the file.");
            }
            header = br.readLine();
            if (header == null) {
                br.close();
                throw new FormatMismatchException("There was only one readable line in the file.");
            }
            String[] nm = header.split("\\s+");
            int n = Integer.parseInt(nm[0]);
            int m = Integer.parseInt(nm[1]);

            String line;
            String[] splitLine;

            //branch on types, (more elegant way?)
            if (type.equals("Directed")) {
                DirectedGraph ans = new DirectedGraph();
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new DirectedVertex("Original"));
                }
                HashMap<Integer, DirectedVertex> indexedVertices = ans.getInternalVertexMap();
                for (int i = 0; i < m - 2; i++) {
                    line = br.readLine();
                    if (line == null) {
                        br.close();
                        throw new FormatMismatchException("There were not enough lines in the file to account for the number "
                                + "of edges claimed in the header.");
                    }
                    splitLine = line.split("\\s+");
                    if (splitLine.length != 3) {
                        br.close();
                        throw new FormatMismatchException("One of the edge lines had too many entries in it.");
                    }
                    ans.addEdge(new Arc("Original", new Pair<DirectedVertex>(indexedVertices.get(Integer.parseInt(splitLine[0])), indexedVertices.get(Integer.parseInt(splitLine[1]))), Integer.parseInt(splitLine[2])));
                }
                if ((line = br.readLine()) != null) {
                    System.out.println("Ignoring excess lines in file.  This could just be whitespace, but there are more lines than "
                            + "are claimed in the header");
                }
                br.close();
                return ans;
            } else if (type.equals("Undirected")) {
                UndirectedGraph ans = new UndirectedGraph();
                for (int i = 0; i < n; i++) {
                    ans.addVertex(new UndirectedVertex("Original"));
                }
                HashMap<Integer, UndirectedVertex> indexedVertices = ans.getInternalVertexMap();
                for (int i = 0; i < m - 2; i++) {
                    line = br.readLine();
                    if (line == null) {
                        br.close();
                        throw new FormatMismatchException("There were not enough lines in the file to account for the number "
                                + "of edges claimed in the header.");
                    }
                    splitLine = line.split("\\s+");
                    if (splitLine.length != 3) {
                        br.close();
                        throw new FormatMismatchException("One of the edge lines had too many entries in it.");
                    }
                    ans.addEdge(new Edge("Original", new Pair<UndirectedVertex>(indexedVertices.get(Integer.parseInt(splitLine[0])), indexedVertices.get(Integer.parseInt(splitLine[1]))), Integer.parseInt(splitLine[2])));

                }
                if ((line = br.readLine()) != null) {
                    System.out.println("Ignoring excess lines in file.  This could just be whitespace, but there are more lines than "
                            + "are claimed in the header");
                }
                br.close();
                return ans;
            } else if (type.equals("Mixed")) {
                //TODO
            } else if (type.equals("Windy")) {
                //TODO
            }
            //Something is wrong
            else {
                br.close();
                throw new FormatMismatchException("The type specified in the first line of the DIMACS_Modified file was not recognized."
                        + "  It should read either \"Directed\" \"Undirected\" \"Mixed\" or \"Windy\"");
            }
            br.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
