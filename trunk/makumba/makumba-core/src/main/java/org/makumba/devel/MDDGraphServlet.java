package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.QueryFragmentFunction;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.DeveloperTool;

/**
 * @author Filip Kis
 */
public class MDDGraphServlet extends DataServlet {

    private static final long serialVersionUID = 1L;

    public MDDGraphServlet() {
        super(DeveloperTool.MDD_GRAPH_VIEWER);
        additionalScripts = new String[1];
        additionalScripts[0] = "joint.all.min.js";
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        PrintWriter writer = response.getWriter();

        writePageContentHeader(null, writer, null, DeveloperTool.MDD_GRAPH_VIEWER);

        Vector<String> mdds = DataDefinitionProvider.getInstance().getDataDefinitionsInDefaultLocations();

        writer.println("<div id=\"mddDiagram\"></div>");
        writer.println("<script type=\"text/javascript\">");

        writer.println("Joint.paper(\"mddDiagram\", 1000, 1000);");

        writer.println("var uml = Joint.dia.uml");

        int x = 20;
        for (String mddName : mdds) {
            if (!mddName.startsWith("org.makumba.")) {
                DataDefinition dd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);
                List<FieldDefinition> fields = dd.getFieldDefinitions();
                Collection<QueryFragmentFunction> functions = dd.getFunctions().getFunctions();
                int width = mddName.length() * 5 + 45;
                long height = Math.round(40 + fields.size() * 15 + functions.size() * 15);
                writer.println("var mdd_" + mddName.replace(".", "_") + " = uml.Class.create({\n" + "  rect: {x: " + x
                        + ", y: 20, width: " + width + ", height: " + height + "},\n" + "  label: \"" + mddName
                        + "\",\n" + "  shadow: true,\n" + "  attrs: {\n" + "    fill: \"#fff\"\n" + "  },\n"
                        + "  labelAttrs: {\n" + "    'font-weight': 'bold'\n" + "  },\n" + " attributes: [");
                for (FieldDefinition fd : fields) {
                    if (fields.indexOf(fd) > 0) {
                        writer.print(",");
                    }
                    writer.print("\"" + fd.getName() + "\"");
                }
                writer.println("]," + " methods: [");
                int i = 0;
                for (QueryFragmentFunction qff : functions) {
                    if (i > 0) {
                        writer.print(",");
                    }
                    writer.print("\"" + qff.getName() + "\"");
                    i++;
                }
                writer.println("]});");
                x += width + 20;
            }
        }

        for (String mddName : mdds) {
            if (!mddName.startsWith("org.makumba.")) {
                DataDefinition dd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);
                List<FieldDefinition> fields = dd.getFieldDefinitions();
                for (FieldDefinition fd : fields) {
                    if (fd.isPointer()) {
                        writer.println("mdd_" + mddName.replace(".", "_") + ".joint(mdd_"
                                + fd.getPointedType().getName().replace(".", "_") + ",uml.dependencyArrow).label(\""
                                + fd.getName() + "\");");
                    }
                }
            }
        }

        writer.println("</script>");

        DevelUtils.writePageEnd(writer);
    }
}
