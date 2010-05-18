package org.makumba.devel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.DataDefinitionParseError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.analyser.engine.JavaParseData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SourceSyntaxPoints;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.controller.Logic;
import org.makumba.providers.Configuration;
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

    private static String[] selectableQueryLanguages = { "OQL", "HQL" };

    private static Map<String, Map<String, String>> builtIn = Configuration.getInternalCodeGeneratorTemplates();

    private static Map<String, Map<String, String>> userDefined = Configuration.getApplicationSpecificCodeGeneratorTemplates();

    private static Map<String, Map<String, String>> all = new HashMap<String, Map<String, String>>();

    static {
        initTemplates();
    }

    /** initialise code templates - read properties from file system */
    private static void initTemplates() {
        builtIn = Configuration.getInternalCodeGeneratorTemplates();
        userDefined = Configuration.getApplicationSpecificCodeGeneratorTemplates();
        all = new HashMap<String, Map<String, String>>();
        for (String key : userDefined.keySet()) {
            if (builtIn.containsKey(key)) {
                builtIn.remove(key);
            }
        }
        all.putAll(builtIn);
        all.putAll(userDefined);
        defaultTemplate = all.keySet().iterator().next();

        selectableCodeTypes = new Hashtable<String, String>();
        selectableCodeTypes.put(CodeGenerator.TYPE_NEWFORM, "mak:newForm");
        selectableCodeTypes.put(CodeGenerator.TYPE_EDITFORM, "mak:editForm");
        selectableCodeTypes.put(CodeGenerator.TYPE_LIST, "mak:list");
        selectableCodeTypes.put(CodeGenerator.TYPE_OBJECT, "mak:object");
        selectableCodeTypes.put(CodeGenerator.TYPE_DELETE, "mak:delete");
        selectableCodeTypes.put(CodeGenerator.TYPE_BUSINESS_LOGICS, "Java Business Logics");
        selectableCodeTypes.put("All", "all");
        selectableCodeTypesOrdered = new ArrayList<String>(Arrays.asList(new String[] { "All",
                CodeGenerator.TYPE_NEWFORM, CodeGenerator.TYPE_EDITFORM, CodeGenerator.TYPE_LIST,
                CodeGenerator.TYPE_OBJECT, CodeGenerator.TYPE_DELETE, CodeGenerator.TYPE_BUSINESS_LOGICS }));
    }

    private URL classesDirectory;

    private DataDefinition dd = null;

    private File fileRoot, alternateFileRoot;

    private String logicDir;

    private String logicFileName;

    private String[] selectedCodeTypes = null;

    private String templateName;

    private String typeParam;

    private String queryLanguageParam;

    public GeneratedCodeViewer(HttpServletRequest req) throws Exception {
        super(req, true);

        // initTemplates(); // uncomment this for testing template purposes.

        setSearchLevels(false, false, false, true);

        contextPath = request.getContextPath();
        virtualPath = DevelUtils.getVirtualPath(req, Configuration.getCodeGeneratorLocation());
        if (virtualPath == null) {
            virtualPath = "/";
        }
        // FIXME should not depend directly on RecordParser
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
            dd = (DataDefinitionProvider.getInstance()).getDataDefinition(virtualPath);

            // check what code types are selected
            typeParam = request.getParameter("type");
            if (typeParam == null) {
                typeParam = CodeGenerator.TYPE_NEWFORM; // no checkbox selected - default to newForm
            }
            if (typeParam.equalsIgnoreCase("all")) { // all selected
                selectedCodeTypes = CodeGenerator.ALL_PROCESSABLE_TYPES;
            } else { // single checkbox selected
                selectedCodeTypes = new String[] { CodeGenerator.nameToTypeMapping.get(typeParam) };
            }
            if (selectedCodeTypes == null) { // invalid param passes - default to newForm
                selectedCodeTypes = new String[] { CodeGenerator.TYPE_NEWFORM };
                typeParam = CodeGenerator.TYPE_NEWFORM;
            }

            // check which query lanaguage is selected
            queryLanguageParam = request.getParameter("queryLanguage");
            if (queryLanguageParam == null) {
                queryLanguageParam = MakumbaJspAnalyzer.QL_OQL;
            }

            // check template
            templateName = request.getParameter("template");
            if (templateName == null) { // default to empty template
                templateName = defaultTemplate;
            }

            CodeGeneratorTemplate template = new CodeGeneratorTemplate(all.get(templateName), queryLanguageParam);
            String action = CodeGenerator.getLabelNameFromDataDefinition(dd) + "View.jsp";

            // puts to together all pages generated --> used when we have selected more than one code type
            StringBuffer allPages = new StringBuffer();
            SourceSyntaxPoints.PreprocessorClient jspParseData = null;

            // initialise file handlers
            String rootPath = request.getSession().getServletContext().getRealPath("/");

            String alternatePath = System.getProperty("makumba.project.path");
            if (alternatePath != null) {
                alternatePath = new File(alternatePath).getAbsolutePath() + File.separator;
                alternateFileRoot = new File(alternatePath + File.separator + GENERATED_CODE_DIRECTORY);
                alternateFileRoot.mkdirs();
            }

            fileRoot = new File(rootPath + File.separator + GENERATED_CODE_DIRECTORY);
            fileRoot.mkdirs();
            File allPagesFile = null;
            logicFileName = CodeGenerator.getLogicNameFromDataDefinition(dd) + ".java";
            classesDirectory = org.makumba.commons.ClassResource.get("");

            CodeGenerator codeGenerator = new CodeGenerator();

            // create all selected types
            for (int i = 0; i < selectedCodeTypes.length; i++) {
                String generatingType = CodeGenerator.nameToTypeMapping.get(selectedCodeTypes[i]);

                // create current page
                StringBuffer sb = new StringBuffer();
                File generatedCodeFile, alternateCodeFile = null;

                if (generatingType == CodeGenerator.TYPE_BUSINESS_LOGICS) { // Java code
                    String packageName = Logic.findPackageName("/");
                    logicDir = File.separator + GENERATED_CODE_DIRECTORY + File.separator
                            + packageName.replace('.', File.separatorChar) + File.separator;
                    String logicPath = classesDirectory.getPath() + logicDir;
                    if (new File(logicPath).mkdirs()) {
                        java.util.logging.Logger.getLogger("org.makumba.devel.codeGenerator").info(
                            "Created logic directory " + logicPath);
                    }
                    boolean hasSuperLogic = new File(logicPath + "Logic.java").exists();
                    codeGenerator.generateJavaBusinessLogicCode(dd, packageName, hasSuperLogic, selectedCodeTypes, sb);
                    generatedCodeFile = new File(fileRoot, logicFileName);
                    if (alternateFileRoot != null) {
                        alternateCodeFile = new File(alternateFileRoot, logicFileName);
                    }
                    allPages.append("\n " + CODE_TYPE_DELIM + "  " + selectableCodeTypes.get(generatingType) + "  "
                            + CODE_TYPE_DELIM + "\n\n");
                    allPages.append(sb); // add to all pages buffer
                    reader = new StringReader(sb.toString());
                    String fileName = GENERATED_CODE_DIRECTORY + File.separator + logicFileName;
                    jspParseData = JavaParseData.getParseData(rootPath, fileName, JavaSourceAnalyzer.getInstance());
                } else { // jsp code
                    // create seperator between different pages
                    if (i > 0) {
                        allPages.append("\n\n\n");
                    }
                    allPages.append("\n " + CODE_TYPE_DELIM + "  " + selectableCodeTypes.get(generatingType) + "  "
                            + CODE_TYPE_DELIM + "\n\n");

                    codeGenerator.generateCode(sb, generatingType, dd, action, template, queryLanguageParam);
                    allPages.append(sb); // add to all pages buffer

                    // get and save page & file names
                    String pageName = CodeGenerator.getFileNameFromObject(dd, generatingType);
                    String fileName = GENERATED_CODE_DIRECTORY + File.separator + pageName;

                    generatedCodeFile = new File(fileRoot, pageName);
                    if (alternateFileRoot != null) {
                        alternateCodeFile = new File(alternateFileRoot, pageName);
                    }

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

                // if alternate file path is indicated, also write to the other file
                if (alternateFileRoot != null) {
                    System.out.println("Writing to alternate location " + alternateCodeFile.getAbsoluteFile());
                    FileWriter alternateWriter = new FileWriter(alternateCodeFile);
                    alternateWriter.write(sb.toString());
                    alternateWriter.flush();
                    alternateWriter.close();
                }

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
                jspParseData = new JspParseData(rootPath, GENERATED_CODE_DIRECTORY + File.separator + allPagesName,
                        JspxJspAnalyzer.getInstance());
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
    @Override
    public void intro(PrintWriter w) {
        initTemplates();
        String browsePath = contextPath + Configuration.getMddViewerLocation()
                + virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);
        String mddViewerPath = contextPath + Configuration.getMddViewerLocation() + "/" + virtualPath;
        // link to validation definition, if existing

        w.println("<td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\" nowrap=\"nowrap\">");
        w.println("<a style=\"color: darkblue;\" href=\"" + mddViewerPath + "\">mdd</a>&nbsp;&nbsp;&nbsp;");
        w.println("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">code generator</span>&nbsp;&nbsp;&nbsp;");
        w.println("<a style=\"color: darkblue;\"href=\"" + browsePath + "\">browse</a>&nbsp;&nbsp;&nbsp;");

        w.println("&nbsp;&nbsp;&nbsp;");
        DevelUtils.writeDevelUtilLinks(w, Configuration.KEY_MDD_VIEWER, contextPath);

        w.println("</td>");
    }

    /** prints the code generator form. */
    @Override
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
            w.println("<b>Query language:</b>");

            for (String selectableQueryLanguage : selectableQueryLanguages) {
                w.print("<input type=\"radio\" name=\"queryLanguage\" value=\"" + selectableQueryLanguage + "\"");
                if (selectableQueryLanguage.equals(queryLanguageParam)) {
                    w.print("checked=\"checked\" ");
                }
                w.println(" />" + selectableQueryLanguage);

            }
            w.println("<br />");

            w.println("<b>Template:</b>");
            w.println("&nbsp;<i>Built-in</i>:");
            printTemplates(w, builtIn);
            w.println("&nbsp;&nbsp;<i>User-defined</i>:");
            printTemplates(w, userDefined);

            w.println("<input type=\"submit\" value=\"Generate!\" />");
            w.println("</form>");

            printGeneratedCodeLinks(w);
        }
    }

    private void getParseData(SourceSyntaxPoints.PreprocessorClient jspParseData) {
        if (jspParseData != null) {
            sourceSyntaxPoints = jspParseData.getSyntaxPointArray(null);
            syntaxPoints = jspParseData.getSyntaxPoints();
        }
    }

    /** print links to the generated JSP and java files in the page header. */
    private void printGeneratedCodeLinks(PrintWriter w) {

        TransactionProvider tp = TransactionProvider.getInstance();

        String cgiParams = "";

        w.println("<span style=\"font-size: smaller;\">");
        boolean addSeperator = false;
        boolean firstPage = true;
        for (String currentType : CodeGenerator.ALL_PROCESSABLE_TYPES) {
            if (currentType == CodeGenerator.TYPE_EDITFORM || currentType == CodeGenerator.TYPE_OBJECT
                    || currentType == CodeGenerator.TYPE_DELETE && cgiParams.equals("")) {
                String labelName = CodeGenerator.getLabelNameFromDataDefinition(dd);
                // we need to find an object to edit
                Transaction db = tp.getConnectionTo(tp.getDefaultDataSourceName());
                try {
                    String queryOQL = "SELECT " + labelName + " AS " + labelName + " FROM " + dd.getName() + " "
                            + labelName;

                    String queryHQL = "SELECT " + labelName + ".id AS " + labelName + " FROM " + dd.getName() + " "
                            + labelName;

                    Vector<Dictionary<String, Object>> v = db.executeQuery(tp.getQueryLanguage().equals(
                        MakumbaJspAnalyzer.QL_OQL) ? queryOQL : queryHQL, null, 0, 1);
                    if (v.size() > 0) {
                        cgiParams = "?" + labelName + "="
                                + ((Pointer) v.firstElement().get(labelName)).toExternalForm();
                    }
                } catch (RuntimeWrappedException e) {
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
            w.println("|&nbsp;<a target=\"_blank\" href=\"" + contextPath + Configuration.getJavaViewerLocation()
                    + logicDir + logicFileName + "\"><i>" + logicFileName + "</i></a>");
            w.println("</span>");
        }
    }

    /** prints the list of templates for the code form generator. */
    private void printTemplates(PrintWriter w, Map<String, Map<String, String>> t) {
        ArrayList<String> templates = new ArrayList<String>(t.keySet());
        Collections.sort(templates);
        if (templates.size() > 0) {
            for (String key : templates) {
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
