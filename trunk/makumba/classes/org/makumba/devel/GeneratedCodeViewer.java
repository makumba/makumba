package org.makumba.devel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.controller.Logic;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.datadefinition.makumba.RecordParser;

/**
 * Implements servlet-based an interface to {@link CodeGenerator}, and displays the generated JSP code.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class GeneratedCodeViewer extends jspViewer {
    private static final String CODE_TYPE_DELIM = "****************************************";

    private static String defaultTemplate;

    private static final String GENERATED_CODE_DIRECTORY = "generatedCode";

    private static Hashtable<String, String> selectableCodeTypes = new Hashtable<String, String>();

    private static ArrayList<String> selectableCodeTypesOrdered = new ArrayList<String>();

    /**
     * Contains all templates, indices are defined by {@link #TEMPLATES_ALL}, {@link #TEMPLATES_BUILTIN},
     * {@link #TEMPLATES_USERDEFINED}
     */
    private static Hashtable<String, Properties>[] TEMPLATES = new Hashtable[3];

    private static final int TEMPLATES_ALL = 0;

    private static final int TEMPLATES_BUILTIN = 1;

    private static final int TEMPLATES_USERDEFINED = 2;
    
    private static final String BUILTIN_TEMPLATES_PATH = "/org/makumba/devel/defaultCodeTemplates/";
    
    static {
        initTemplates();
    }

    /** initialise code templates - read properties from file system */
    private static void initTemplates() {
        for (int i = 0; i < TEMPLATES.length; i++) {
            TEMPLATES[i] = new Hashtable<String, Properties>();
        }
        
        // built-in templates
        Properties builtinProps = new Properties();
        try {
            populateBuiltin(builtinProps, "fieldset");
            populateBuiltin(builtinProps, "simple");
            populateBuiltin(builtinProps, "tabular");
            populateBuiltin(builtinProps, "tabular-fieldset");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        // user defined templates
        URL templatePath = org.makumba.commons.ClassResource.get("codeTemplates");

            if (templatePath != null) {
                File templatePropDirectory = new File(templatePath.getFile());
                if (templatePropDirectory.canRead() && templatePropDirectory.isDirectory()) {
                    File[] files = templatePropDirectory.listFiles(CodeGenerator.getFileFilter());
                    for (int j = 0; j < files.length; j++) {
                        Properties props = new Properties();
                        try {
                            props.load(new FileInputStream(files[j]));
                            String name = files[j].getName().substring(0, files[j].getName().lastIndexOf(".properties"));
                            TEMPLATES[TEMPLATES_ALL].put(name, props);
                            TEMPLATES[TEMPLATES_USERDEFINED].put(name, props);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        defaultTemplate = (String) TEMPLATES[TEMPLATES_BUILTIN].keys().nextElement();
        selectableCodeTypes = new Hashtable<String, String>();
        selectableCodeTypes.put(CodeGenerator.TYPE_NEWFORM, "mak:newForm");
        selectableCodeTypes.put(CodeGenerator.TYPE_EDITFORM, "mak:editForm");
        selectableCodeTypes.put(CodeGenerator.TYPE_LIST, "mak:list");
        selectableCodeTypes.put(CodeGenerator.TYPE_OBJECT, "mak:object");
        selectableCodeTypes.put(CodeGenerator.TYPE_DELETE, "mak:delete");
        selectableCodeTypes.put(CodeGenerator.TYPE_BUSINESS_LOGICS, "Java Business Logics");
        selectableCodeTypes.put("All", "all");
        selectableCodeTypesOrdered = new ArrayList<String>(Arrays.asList(new String[] { "All", CodeGenerator.TYPE_NEWFORM,
                CodeGenerator.TYPE_EDITFORM, CodeGenerator.TYPE_LIST, CodeGenerator.TYPE_OBJECT,
                CodeGenerator.TYPE_DELETE, CodeGenerator.TYPE_BUSINESS_LOGICS }));
    }
    
    private static void populateBuiltin(Properties builtinProps, String templateName) throws IOException {
        builtinProps.load(GeneratedCodeViewer.class.getResourceAsStream(BUILTIN_TEMPLATES_PATH + templateName+ ".properties"));
        TEMPLATES[TEMPLATES_ALL].put(templateName, builtinProps);
        TEMPLATES[TEMPLATES_BUILTIN].put(templateName, builtinProps);
    }

    private URL classesDirectory;

    private DataDefinition dd = null;

    private File fileRoot;

    private String logicDir;

    private String logicFileName;

    private String[] selectedCodeTypes = null;

    private String templateName;

    private String typeParam;

    public GeneratedCodeViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(req, sv, true);
        // initTemplates(); // uncomment this for testing template purposes.

        servletContext = servlet.getServletContext();
        contextPath = request.getContextPath();

        setSearchLevels(false, false, false, true);
        virtualPath = req.getPathInfo();
        contextPath = req.getContextPath();
        if (virtualPath == null) {
            virtualPath = "/";
        }
        //FIXME should not depend directly on RecordParser
        java.net.URL u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null) {
            u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "idd");
        }
        virtualPath = virtualPath.substring(1);

        readFromURL(u);

        generateCode();
    }

    /** Starts the code generation. */
    public void generateCode() throws IOException {
        try {
            dd = (new DataDefinitionProvider()).getDataDefinition(virtualPath);

            // check what code types are selected
            typeParam = request.getParameter("type");
            if (typeParam == null) {
                typeParam = CodeGenerator.TYPE_NEWFORM; // no checkbox selected - default to newForm
            }
            if (typeParam.equalsIgnoreCase("all")) { // all selected
                selectedCodeTypes = CodeGenerator.ALL_PROCESSABLE_TYPES;
            } else { // single checkbox selected
                selectedCodeTypes = new String[] { (String) CodeGenerator.nameToTypeMapping.get(typeParam) };
            }
            if (selectedCodeTypes == null) { // invalid param passes - default to newForm
                selectedCodeTypes = new String[] { CodeGenerator.TYPE_NEWFORM };
                typeParam = CodeGenerator.TYPE_NEWFORM;
            }

            // check template
            templateName = request.getParameter("template");
            if (templateName == null) { // default to empty template
                templateName = defaultTemplate;
            }

            CodeGeneratorTemplate template = new CodeGeneratorTemplate(
                    (Properties) TEMPLATES[TEMPLATES_ALL].get(templateName));
            String action = CodeGenerator.getLabelNameFromDataDefinition(dd) + "View.jsp";

            // puts to gether all pages generated --> used when we have selected more than one code type
            StringBuffer allPages = new StringBuffer();
            JspParseData jspParseData = null;

            // intialise file handlers
            String rootPath = servlet.getServletContext().getRealPath("/");
            fileRoot = new File(rootPath + File.separator + GENERATED_CODE_DIRECTORY);
            fileRoot.mkdirs();
            File allPagesFile = null;
            logicFileName = CodeGenerator.getLogicNameFromDataDefinition(dd) + ".java";
            classesDirectory = org.makumba.commons.ClassResource.get("");

            CodeGenerator codeGenerator = new CodeGenerator();

            // create all selected types
            for (int i = 0; i < selectedCodeTypes.length; i++) {
                String generatingType = (String) CodeGenerator.nameToTypeMapping.get(selectedCodeTypes[i]);

                // create current page
                StringBuffer sb = new StringBuffer();
                File generatedCodeFile;

                if (generatingType == CodeGenerator.TYPE_BUSINESS_LOGICS) { // Java code
                    String packageName = Logic.findPackageName("/");
                    logicDir = File.separator + GENERATED_CODE_DIRECTORY + File.separator
                            + packageName.replace('.', File.separatorChar) + File.separator;
                    String logicPath = classesDirectory.getPath() + logicDir;
                    System.out.println(new File(logicDir).getAbsolutePath());
                    if (new File(logicPath).mkdirs()) {
                        java.util.logging.Logger.getLogger("org.makumba." + "devel.codeGenerator").info(
                            "Created logic directory " + logicPath);
                    }
                    boolean hasSuperLogic = new File(logicPath + "Logic.java").exists();
                    codeGenerator.generateJavaBusinessLogicCode(dd, packageName, hasSuperLogic, selectedCodeTypes, sb);
                    generatedCodeFile = new File(logicPath + logicFileName);
                    allPages.append("\n " + CODE_TYPE_DELIM + "  " + selectableCodeTypes.get(generatingType) + "  "
                            + CODE_TYPE_DELIM + "\n\n");
                    allPages.append(sb); // add to all pages buffer
                } else { // jsp code
                    // create seperator between different pages
                    if (i > 0) {
                        allPages.append("\n\n\n");
                    }
                    allPages.append("\n " + CODE_TYPE_DELIM + "  " + selectableCodeTypes.get(generatingType) + "  "
                            + CODE_TYPE_DELIM + "\n\n");

                    codeGenerator.generateCode(sb, generatingType, dd, action, template);
                    allPages.append(sb); // add to all pages buffer

                    // get and save page & file names
                    String pageName = CodeGenerator.getFileNameFromObject(dd, generatingType);
                    String fileName = GENERATED_CODE_DIRECTORY + File.separator + pageName;

                    generatedCodeFile = new File(fileRoot, pageName);

                    // get jsp page parsers if we have only one page
                    if (selectedCodeTypes.length == 1) {
                        reader = new StringReader(sb.toString());
                        jspParseData = JspParseData.getParseData(rootPath, fileName, JspxJspAnalyzer.getInstance());
                    }
                }
                // write page to file
                FileWriter writer = new FileWriter(generatedCodeFile);
                writer.write(sb.toString());
                writer.flush();
                writer.close();

            }
            // more than one page --> write all pages file & display it's source
            if (selectedCodeTypes.length > 1) {
                String allPagesName = CodeGenerator.getFileNameFromObject(dd, typeParam);
                allPagesFile = new File(fileRoot, allPagesName);
                FileWriter writerAllPage = new FileWriter(allPagesFile);
                writerAllPage.write(allPages.toString());
                writerAllPage.flush();
                writerAllPage.close();

                reader = new StringReader(allPages.toString());
                jspParseData = new JspParseData(rootPath, GENERATED_CODE_DIRECTORY + File.separator
                        + allPagesName, JspxJspAnalyzer.getInstance());
            }

            getParseData(jspParseData);
            if (allPagesFile != null) {
                allPagesFile.delete();
            }

        } catch (DataDefinitionNotFoundError e) {
            caughtError = e;
        } catch (DataDefinitionParseError e) {
            caughtError = e;
        }
    }

    /** writes the page header, with links to the mdd and to browse. */
    public void intro(PrintWriter w) {
        String browsePath = contextPath + "/dataDefinitions/"
                + virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);
        String mddViewerPath = contextPath + "/dataDefinitions/" + virtualPath;
        // link to validation definition, if existing

        w.println("<td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\" nowrap=\"nowrap\">");
        w.println("<a style=\"color: darkblue;\" href=\"" + mddViewerPath + "\">mdd</a>&nbsp;&nbsp;&nbsp;");
        if (dd.getValidationDefinition() != null) {
            w.print("<a style=\"color: darkblue;\" href=\"" + (contextPath + "/validationDefinitions/" + virtualPath) + "\">validation definition</a>&nbsp;&nbsp;&nbsp;");
        }
        w.println("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">code generator</span>&nbsp;&nbsp;&nbsp;");
        w.println("<a style=\"color: darkblue;\"href=\"" + browsePath + "\">browse</a>&nbsp;&nbsp;&nbsp;");
        w.println("</td>");
    }

    /** prints the code generator form. */
    public void printPageBeginAdditional(PrintWriter w) throws IOException {
        if (dd != null) {
            w.println("<form style=\"margin-top:1px; margin-bottom:0px; font-size:smaller;\">");
            w.println("<b>Code type:</b>");

            for (int i = 0; i < selectableCodeTypesOrdered.size(); i++) {
                Object key = selectableCodeTypesOrdered.get(i);
                w.print("<input type=\"radio\" name=\"type\" value=\"" + key + "\"");
                if (key.equals(typeParam)) {
                    w.print("checked=\"checked\" ");
                }
                w.println(" />" + selectableCodeTypes.get(key));
            }

            w.println("<br />");
            w.println("<b>Template:</b>");
            w.println("&nbsp;<i>Built-in</i>:");
            printTemplates(w, TEMPLATES_BUILTIN);
            w.println("&nbsp;&nbsp;<i>User-defined</i>:");
            printTemplates(w, TEMPLATES_USERDEFINED);

            w.println("<input type=\"submit\" value=\"Generate!\" />");
            w.println("</form>");

            printGeneratedCodeLinks(w);
        }
    }

    private void getParseData(JspParseData jspParseData) {
        if (jspParseData != null) {
            sourceSyntaxPoints = jspParseData.getSyntaxPointArray(null);
            syntaxPoints = jspParseData.getSyntaxPoints();
        }
    }

    /** print links to the generated JSP and java files in the page header. */
    private void printGeneratedCodeLinks(PrintWriter w) {
        
        TransactionProvider tp = new TransactionProvider();
        
        String cgiParams = "";

        w.println("<span style=\"font-size: smaller;\">");
        boolean addSeperator = false;
        boolean firstPage = true;
        for (int i = 0; i < CodeGenerator.ALL_PROCESSABLE_TYPES.length; i++) {
            String currentType = CodeGenerator.ALL_PROCESSABLE_TYPES[i];

            if (currentType == CodeGenerator.TYPE_EDITFORM || currentType == CodeGenerator.TYPE_OBJECT
                    || currentType == CodeGenerator.TYPE_DELETE && cgiParams.equals("")) {
                String labelName = CodeGenerator.getLabelNameFromDataDefinition(dd);
                // we need to find an object to edit
                Transaction db = tp.getConnectionTo(tp.getDefaultDataSourceName());
                try {
                    String query = "SELECT " + labelName + " AS " + labelName + " FROM " + dd.getName() + " "
                            + labelName;
                    Vector v = db.executeQuery(query, null, 0, 1);
                    if (v.size() > 0) {
                        cgiParams = "?" + labelName + "="
                                + ((Pointer) ((Dictionary) v.firstElement()).get(labelName)).toExternalForm();
                    }
                }catch (RuntimeWrappedException e) {
                    w.println("<br/> <span style=\"color: red\">" + e.getCause() + "</span>");
                } finally {
                    db.close();
                }
            }

            if (firstPage) {
                w.print("<b>Preview:</b>&nbsp;");
                firstPage = false;
            }
            String pageName = CodeGenerator.getFileNameFromObject(dd, currentType);
            String fileName = fileRoot.getPath() + File.separator + pageName;

            boolean fileExists = new File(fileName).exists();
            boolean selectedType = Arrays.asList(selectedCodeTypes).contains(currentType);
            if (fileExists) {
                if (addSeperator) {
                    w.println("&nbsp;|");
                    addSeperator = false;
                }
                w.print("<a target=\"_blank\" ");
                if (!selectedType) {
                    w.print("style=\"color: grey;\" ");
                }
                w.println("href=\"" + contextPath + "/" + GENERATED_CODE_DIRECTORY + File.separator + pageName
                        + cgiParams + "\"><i>" + pageName + "</i></a>");
                addSeperator = true;
            }
        }
        if (new File(classesDirectory.getPath() + logicDir + logicFileName).exists()) {
            w.println("|&nbsp;<a target=\"_blank\" href=\"" + contextPath + "/classes/" + logicDir + logicFileName
                    + "\"><i>" + logicFileName + "</i></a>");
            w.println("</span>");
        }
    }

    /** prints the list of templates for the code form generator. */
    private void printTemplates(PrintWriter w, int index) {
        ArrayList<String> templates = new ArrayList<String>(TEMPLATES[index].keySet());
        Collections.sort(templates);
        if (templates.size() > 0) {
            for (int i = 0; i < templates.size(); i++) {
                Object key = templates.get(i);
                w.print("<input type=\"radio\" name=\"template\" value=\"" + key + "\"");
                if (key.equals(templateName)) {
                    w.print("checked=\"checked\" ");
                }
                w.println(" />" + key);
            }
        } else {
            w.println("none available");
        }
    }

}
