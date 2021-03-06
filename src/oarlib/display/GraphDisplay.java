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
package oarlib.display;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.display.util.Colors;
import oarlib.exceptions.UnsupportedFormatException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.labelAdjust.LabelAdjust;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Gephi interface
 * Display class meant to visualize the structure of the graph
 *
 * @author oliverlum
 */
public class GraphDisplay {

    private Layout mLayout;
    private ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
    private GraphModel mGraphModel;
    private ExportController mExportController;
    private PreviewModel mPreviewModel;

    private Graph<? extends Vertex, ? extends Link<? extends Vertex>> mGraph;

    private String mInstanceName;
    private float mXScaleFactor;
    private float mYScaleFactor;
    private double mMinX;
    private double mMinY;

    public GraphDisplay(Layout layout, Graph<? extends Vertex, ? extends Link<? extends Vertex>> graph, String instanceName) {
        mLayout = layout;
        mGraph = graph;
        mInstanceName = instanceName;
        pc.newProject();
        mGraphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        mExportController = Lookup.getDefault().lookup(ExportController.class);
        mPreviewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
        mMinX = -100;
        mMinY = -100;
        mXScaleFactor = -100;
        mYScaleFactor = -100;
    }

    public void setGraph(Graph<? extends Vertex, ? extends Link<? extends Vertex>> newGraph) {
        mGraph = newGraph;
    }

    public void setLayout(Layout newLayout) {
        mLayout = newLayout;
    }

    public void setInstanceName(String newName) {
        mInstanceName = newName;
    }

    public void setScaling(float newXFactor, float newYFactor, double newMinX, double newMinY) {
        mXScaleFactor = newXFactor;
        mYScaleFactor = newYFactor;
        mMinX = newMinX;
        mMinY = newMinY;
    }

    /**
     * Exports the graph associated with this display to the specified export type, (e.g. PDF), using the display's layout.
     *
     * @param et - the preferred export format.
     * @return - true if the export is successful; false oth.
     */
    public boolean export(ExportType et) throws UnsupportedFormatException {

        try {
            switch (et) {
                case PDF:
                    exportToPDF(null, null);
                    break;
                case GEXF:
                    exportToGEXF(null, null);
                    break;
                case CSV:
                    exportToCSV(null, null);
                    break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exports the graph associated with this display to the specified export type, (e.g. PDF), using the display's layout.
     *
     * @param et            - the preferred export format.
     * @param edgePartition - key = edge id; value = partition #
     * @return - true if the export is successful; false oth.
     */
    public boolean exportWithPartition(ExportType et, HashMap<Integer, Integer> edgePartition) throws UnsupportedFormatException, IllegalArgumentException {
        try {
            switch (et) {
                case PDF:
                    exportToPDF(edgePartition, null);
                    break;
                case GEXF:
                    exportToGEXF(edgePartition, null);
                    break;
                case CSV:
                    exportToCSV(edgePartition, null);
                    break;
                case JSON:
                    exportToJSON(edgePartition, null);
                    break;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportRoute(ExportType et, Route r) {
        try {
            switch (et) {
                case PDF:
                    exportToPDF(null, r);
                    break;
                case GEXF:
                    exportToGEXF(null, r);
                    break;
                case CSV:
                    exportToCSV(null, r);
                    break;
                case JSON:
                    exportToJSON(null, r);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void exportToGEXF(HashMap<Integer, Integer> edgePartition, Route r) throws UnsupportedFormatException {
        //TODO
    }

    private void exportToJSON(HashMap<Integer, Integer> edgePartition, Route r) throws UnsupportedFormatException {
        //TODO

        //if you don't have coordinates, it wouldn't really work well
        if(mGraph.getVertices().size() == 0) {
            throw new UnsupportedFormatException("The graph you are attempting to export appears to be empty.");
        }

        if(!mGraph.getVertices().iterator().next().hasCoordinates()) {
            throw new UnsupportedFormatException("The graph you are attempting to export appears not to have any cordinates.");
        }


        //null null case is just show the graph
        try {

            //for consumption by OARLibViz
            PrintWriter pw = new PrintWriter(new File(mInstanceName + ".json"), "UTF-8");
            pw.println("{");

            //nodes
            pw.println("\t\"nodes\"");
            pw.println("\t[");
            Iterator<? extends Vertex> vIter = mGraph.getVertices().iterator();
            while(vIter.hasNext()) {
                Vertex v = vIter.next();
                String toPrint = "\t\t{\"id\":" + v.getId() + ", \"name\":" + v.getId() + ", \"x\":" + v.getX() + ", \"y\":" + v.getY() + "}";
                if(vIter.hasNext()) {
                    toPrint += ",";
                }
                pw.println(toPrint);
            }
            pw.println("\t]");

            pw.println("\t\"links\"");
            pw.println("\t[");
            Iterator<? extends Link> lIter = mGraph.getEdges().iterator();
            while(lIter.hasNext()) {
                Link l = lIter.next();
                String toPrint = "\t\t{\"id\":" + l.getId() + ", \"label\":" + l.getId() + ", \"source\":" + l.getFirstEndpointId() + ", \"sink\":" + l.getSecondEndpointId() + ", \"directed\":" + l.isDirected() + "}";
                if(vIter.hasNext()) {
                    toPrint += ",";
                }
                pw.println(toPrint);
            }
            pw.println("\t]");

            pw.println("}");

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //with partitions

        //with route
    }

    private void exportToCSV(HashMap<Integer, Integer> edgePartition, Route r) throws UnsupportedFormatException {
        //TODO
    }

    private <V extends Vertex, E extends Link<V>> void exportToPDF(HashMap<Integer, Integer> edgePartition, Route<V, E> r) throws UnsupportedFormatException {

        boolean withPartitions = edgePartition != null;
        boolean useAutoLayout = true;
        int numPartitions = 0;

        //ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //pc.newProject();

        //if (withPartitions && edgePartition.keySet().size() != mGraph.getEdges().size())
        //throw new IllegalArgumentException("The specified partition does not seem to have the appropriate number of entries.");


        //figure out max cost
        int maxCost = 0;
        for (Link l : mGraph.getEdges())
            if (l.getCost() > maxCost)
                maxCost = l.getCost();

        //convert the graph from our format into one digestible by Gephi
        org.gephi.graph.api.Graph graph;

        if (mGraph.getClass() == UndirectedGraph.class) {
            graph = mGraphModel.getUndirectedGraph();
        } else if (mGraph.getClass() == DirectedGraph.class) {
            graph = mGraphModel.getDirectedGraph();
        } else if (mGraph.getClass() == MixedGraph.class) {
            graph = mGraphModel.getMixedGraph();
        } else if (mGraph.getClass() == WindyGraph.class) {
            graph = mGraphModel.getUndirectedGraph();
        } else
            throw new UnsupportedFormatException("The graph passed in is not currently supported for visualization");

        //add nodes
        int n = mGraph.getVertices().size();
        Vertex tempV;
        TIntObjectHashMap<? extends Vertex> mVertices = mGraph.getInternalVertexMap();

        Node[] nodeSet = new Node[n + 1];
        Node temp;

        //figure out anchors for scaling
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 1; i <= n; i++) {
            tempV = mVertices.get(i);

            if (tempV.getX() > maxX)
                maxX = tempV.getX();
            if (tempV.getX() < minX)
                minX = tempV.getX();


            if (tempV.getY() > maxY)
                maxY = tempV.getY();
            if (tempV.getY() < minY)
                minY = tempV.getY();
        }

        float xRange = (float) (maxX - minX);
        float yRange = (float) (maxY - minY);
        float xScaleFactor = 100f / xRange * 100;
        float yScaleFactor = 100f / yRange * 100;

        for (int i = 1; i <= n; i++) {
            tempV = mVertices.get(i);
            temp = mGraphModel.factory().newNode();

            if (mGraph.getDepotId() == i) {
                temp.getNodeData().setLabel("Depot");
                temp.getNodeData().setSize(100f);
                temp.getNodeData().setColor(1f, 0f, 0f);
            } else {
                temp.getNodeData().setLabel("");
                temp.getNodeData().setSize(3f);
                temp.getNodeData().setColor(1f, 1f, 1f);
            }

            if (tempV.hasCoordinates()) {
                useAutoLayout = false;
                if (mMinX != -100) {
                    temp.getNodeData().setX((float) (tempV.getX() - mMinX) * mXScaleFactor);
                    temp.getNodeData().setY((float) (tempV.getY() - mMinY) * mYScaleFactor);
                }
                temp.getNodeData().setX((float) (tempV.getX() - minX) * xScaleFactor);
                temp.getNodeData().setY((float) (tempV.getY() - minY) * yScaleFactor);
            }

            nodeSet[i] = temp;
            graph.addNode(temp);
        }

        //add edges
        int m = mGraph.getEdges().size();
        Edge tempEdge;
        TIntObjectHashMap<? extends Link<? extends Vertex>> edgeMap = mGraph.getInternalEdgeMap();
        //Link<? extends Vertex> tempLink;
        float cost = 0f;
        float revCost = 0f;

        HashSet<Link<? extends Vertex>> links = new HashSet<Link<? extends Vertex>>();
        if (r == null)
            for (Link<? extends Vertex> tempLink : edgeMap.getValues(new Link[1]))
                links.add(tempLink);
        else
            for (Link<? extends Vertex> tempLink : r.getPath())
                links.add(tempLink);

        for (Link<? extends Vertex> tempLink : links) {
            if (tempLink.getCost() == 0) {
                cost = .5f;
                revCost = .5f;
            } else {
                cost = ((float) maxCost - tempLink.getCost()) / maxCost;
                if (tempLink.getClass() == WindyEdge.class)
                    revCost = ((float) maxCost - ((WindyEdge) tempLink).getReverseCost()) / maxCost;
            }

            if (tempLink.getClass() == WindyEdge.class)
                tempEdge = mGraphModel.factory().newEdge(nodeSet[tempLink.getEndpoints().getFirst().getId()], nodeSet[tempLink.getEndpoints().getSecond().getId()], (cost + revCost) / 2f, tempLink.isDirected());
            else
                tempEdge = mGraphModel.factory().newEdge(nodeSet[tempLink.getEndpoints().getFirst().getId()], nodeSet[tempLink.getEndpoints().getSecond().getId()], cost, tempLink.isDirected());
            if (tempLink.getClass() == WindyEdge.class)
                tempEdge.getEdgeData().setLabel("");//tempLink.getCost() + "," + ((WindyEdge) tempLink).getReverseCost());
            else
                tempEdge.getEdgeData().setLabel(String.valueOf(tempLink.getCost()));
            if (withPartitions) {
                if (edgePartition.containsKey(tempLink.getId())) {
                    tempEdge.getEdgeData().getAttributes().setValue("Partition", edgePartition.get(tempLink.getId()));
                    if (edgePartition.get(tempLink.getId()) > numPartitions)
                        numPartitions = edgePartition.get(tempLink.getId());
                }
            } else {
                if (tempLink.isRequired())
                    tempEdge.getEdgeData().getAttributes().setValue("Partition", 2);
                else
                    tempEdge.getEdgeData().getAttributes().setValue("Partition", 1);
            }
            if (withPartitions && !edgePartition.containsKey(tempLink.getId()))
                tempEdge.setWeight(20f);
            else
                tempEdge.setWeight(40f);
            graph.addEdge(tempEdge);
        }

        numPartitions++;

        if (useAutoLayout) {
            //Layout for 1 minute
            AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
            autoLayout.setGraphModel(mGraphModel);
            YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
            ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
            LabelAdjust thirdLayout = new LabelAdjust(null);
            LayoutProperty[] stuff = thirdLayout.getProperties();

            AutoLayout.DynamicProperty speed2 = AutoLayout.createDynamicProperty("LabelAdjust.speed.name", new Double[]{0.5, 0.2}, new float[]{0f, 1f}, AutoLayout.Interpolation.LINEAR);
            AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
            AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(1000.), 0f);//500 for the complete period
            autoLayout.addLayout(firstLayout, 0.5f);
            autoLayout.addLayout(secondLayout, 0.3f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
            autoLayout.addLayout(thirdLayout, 0.2f, new AutoLayout.DynamicProperty[]{speed2});
            autoLayout.execute();
        }

        //testing partitioning
        RankingController rc = Lookup.getDefault().lookup(RankingController.class);
        Ranking partitionRanking = rc.getModel().getRanking(Ranking.EDGE_ELEMENT, "Partition");
        AbstractColorTransformer ct = (AbstractColorTransformer) rc.getModel().getTransformer(Ranking.EDGE_ELEMENT, Transformer.RENDERABLE_COLOR);

        float increment = 1f / (Colors.RYGCBGB.length - 1);
        float[] positions = new float[Colors.RYGCBGB.length];
        for (int i = 0; i < Colors.RYGCBGB.length; i++) {
            positions[i] = i * increment;
        }
        ct.setColorPositions(positions);
        ct.setColors(Colors.RYGCBGB);

        rc.transform(partitionRanking, ct);

        //Change some coloring / size attributes
        mPreviewModel.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH, .5f);
        EdgeColor edgeColor = new EdgeColor(EdgeColor.Mode.ORIGINAL);
        mPreviewModel.getProperties().putValue(PreviewProperty.EDGE_COLOR, edgeColor);
        mPreviewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, true);
        mPreviewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, true);
        mPreviewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, false);
        mPreviewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, true);
        mPreviewModel.getProperties().putValue(PreviewProperty.EDGE_LABEL_FONT, new Font("Helvetica", Font.ITALIC, 3));
        mPreviewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, new Font("Helvetica", Font.ITALIC, 3));
        mPreviewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR, new DependantOriginalColor(Color.WHITE));

        try {
            //ExportController mExportController = Lookup.getDefault().lookup(ExportController.class);
            mExportController.exportFile(new File("/Users/oliverlum/Downloads/Plots/" + mInstanceName + "autolayout.pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //now do the subgraphs
        /*if (withPartitions) {
            PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            Partition p = partitionController.buildPartition(attributeModel.getEdgeTable().getColumn("Partition"), graph);

            PartitionBuilder.EdgePartitionFilter epf = new PartitionBuilder.EdgePartitionFilter(p);
            FilterController fc = Lookup.getDefault().lookup(FilterController.class);
            Query q;
            GraphView view;
            for (int i = 0; i < numPartitions; i++) {
                epf.unselectAll();
                epf.addPart(p.getPartFromValue(i));

                q = fc.createQuery(epf);
                view = fc.filter(q);
                graphModel.setVisibleView(view);

                try {
                    ec.exportFile(new File("/Users/oliverlum/Downloads/Plots/" + mInstanceName + "_PART_" + i + "_autolayout.pdf"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }*/

        pc.cleanWorkspace(pc.getCurrentWorkspace());
    }

    /**
     * The types of supported display formats.
     */
    public enum Layout {
        YifanHu,
        ForceAtlas,
        ForceAtlas2,
        LabelAdjust,
        FruchtermanReingold;
    }

    /**
     * The types of supported export formats.
     */
    public enum ExportType {
        PDF,
        GEXF,
        JSON,
        CSV;
    }
}
