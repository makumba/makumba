///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: FormsOQLTest.java 2628 2008-06-17 21:03:46Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.SquarifiedTreeMapLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * A (not-ready) class to visualise the relation of data definitions.
 * 
 * @author Rudolf Mayer
 * @version $Id: MDDVisualiser.java,v 1.1 Jul 21, 2008 12:26:37 AM rudi Exp $
 */
public class MDDRelationVisualiser {
    static final String tree = "graph";

    static final String treeNodes = "graph.nodes";

    static final String treeEdges = "graph.edges";

    public static void main(String[] args) throws IOException {
        Vector<String> mdds = DataDefinitionProvider.getInstance().getDataDefinitionsInDefaultLocations();
        File tmp = new File("/tmp/graph.ml"); // File.createTempFile("graph_", ".ml");

        Document d = DocumentHelper.createDocument();
        Element graphml_tag = d.addElement("graphml");
        
        
        Element root = graphml_tag.addElement(tree);
        root.addAttribute("edgedefault", "directed");
        
        Namespace ns = Namespace.get("", "http://graphml.graphdrawing.org/xmlns");
        d.getRootElement().add(ns);
        
        Element incoming = root.addElement("key");
        incoming.addAttribute("id", "name");
        incoming.addAttribute("for", "node");
        incoming.addAttribute("attr.name", "name");
        incoming.addAttribute("attr.type", "string");
        
        for (String mdd : mdds) {
            try {
                DataDefinition dd = DataDefinitionProvider.getInstance().getDataDefinition(mdd);
                System.out.println(dd);
                addNode(root, dd.getName());
                final Vector<String> fieldNames = dd.getFieldNames();
                for (String name : fieldNames) {
                    FieldDefinition fd = dd.getFieldDefinition(name);
                    if (fd.isPointer()) {
                        final String name2 = fd.getPointedType().getName();
                        addEdge(root, dd.getName(), name2, 9);
                    } else if (fd.getIntegerType() == FieldDefinition._set) {
                        final String name2 = fd.getPointedType().getName();
                        addEdge(root, dd.getName(), name2, 9);
                    }
                }
            } catch (DataDefinitionParseError e) {
                System.out.println("Skipping broken MDD " + mdd);
            }
        }

        XMLWriter serializer = new XMLWriter(new FileWriter(tmp), new OutputFormat("", false));
        serializer.write(d);
        serializer.close();

        Graph graph = null;
        try {
            graph = new GraphMLReader().readGraph(tmp.getAbsoluteFile());
        } catch (DataIOException e) {
            e.printStackTrace();
            System.err.println("Error loading graph. Exiting...");
            System.exit(1);
        }

        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization m_vis = new Visualization();
        m_vis.add(tree, graph);
        m_vis.setInteractive(treeNodes, null, false);

        // draw the "name" label for NodeItems
        LabelRenderer m_nodeRenderer = new LabelRenderer("name");
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        m_nodeRenderer.setRoundedCorner(8, 8); // round the corners

        EdgeRenderer m_edgeRenderer = new EdgeRenderer();
        m_edgeRenderer.setDefaultLineWidth(1.0);

        // create a new default renderer factory
        // return our name label renderer as the default for all non-EdgeItems
        // includes straight line edges for EdgeItems by default
        final DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer);
        rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
        m_vis.setRendererFactory(rf);

        // use black for node text
        ColorAction text = new ColorAction(treeNodes, VisualItem.TEXTCOLOR, ColorLib.gray(0));
        // use light grey for edges
        ColorAction edges = new ColorAction(treeEdges, VisualItem.STROKECOLOR, ColorLib.gray(200));

        // create an action list containing all color assignments
        ActionList color = new ActionList();
        // color.add(fill);
        color.add(text);
        color.add(edges);

        // create the tree layout action
        Layout graphLayout = new SquarifiedTreeMapLayout(tree);
        m_vis.putAction("circleLayout", graphLayout);

        // create an action list with an animated layout
        // the INFINITY parameter tells the action list to run indefinitely
        ActionList layout = new ActionList(Activity.DEFAULT_STEP_TIME * 500);
        Layout l = new ForceDirectedLayout(tree);
        layout.add(l);
        layout.add(new RepaintAction());

        // add the actions to the visualization
        m_vis.putAction("color", color);
        m_vis.putAction("layout", layout);

        // create a new Display that pull from our Visualization
        Display display = new Display(m_vis);
        display.setSize(1200, 800); // set display size
        display.addControlListener(new DragControl()); // drag items around
        display.addControlListener(new PanControl()); // pan with background left-drag
        display.addControlListener(new ZoomControl()); // zoom with vertical right-drag

        // create a new window to hold the visualization
        JFrame frame = new JFrame("prefuse example");
        // ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.pack(); // layout components in window
        frame.setVisible(true); // show the window

        m_vis.run("color"); // assign the colors
        m_vis.run("layout"); // start up the animated layout

    }

    private static void addNode(Element root, String name) {
        Element elem = root.addElement("node").addAttribute("id", "" + name);
        elem.addElement("data").addAttribute("key", "name").setText(name);
    }

    private static Element addEdge(Element root, String from, String to, Integer count) {
        Element elem = root.addElement("edge");

        elem.addAttribute("source", "" + (from));
        elem.addAttribute("target", "" + (to));
        elem.addAttribute("directed", "true");

        return elem;
    }

}
