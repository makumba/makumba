package org.makumba.devel.relations;

import java.io.File;
import java.io.FileInputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.MakumbaError;
import org.makumba.analyser.engine.JavaParseData;
import org.makumba.devel.JavaSourceAnalyzer;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.query.oql.OQLQueryAnalysisProvider;

import antlr.RecognitionException;

/**
 * Extracts relations from a Java source file <br>
 * FIXME currently only handles OQL queries in Java -> MDD relations<br>
 * TODO add more origin information for Java -> MDD relations (now we only give the query string, we miss line number
 * etc.)
 * 
 * @author Manuel Gay
 * @version $Id: JavaRelationMiner.java,v 1.1 Apr 19, 2008 9:00:56 PM manu Exp $
 */
public class JavaRelationMiner extends RelationMiner {

    private Vector<String> webappJavaPackages = new Vector<String>();

    public JavaRelationMiner(RelationCrawler rc) {
        super(rc);
    }

    @Override
    public void crawl(String path) {
        
        if(!new File(rc.getWebappRoot() + File.separator + path).exists()) {
            logger.warning("MDD "+path + " does not exist in webapp "+rc.getWebappRoot());
            return;
        }

        computeJava2JavaRelations(path);
        computeJava2MDDRelations(path);
    }

    private void computeJava2MDDRelations(String path) {
        JavaMDDParser jqp = new JavaMDDParser(rc.getWebappRoot() + File.separator + path);
        Vector<String> queries = jqp.getQueries();

        // let's try to analyse the queries and extract the relations due to projections and labels
        for (String query : queries) {
            // FIXME for the moment, we only have OQL queries in BL. but this won't be necessarily true in the future.
            QueryAnalysis qA = null;
            try {
                qA = OQLQueryAnalysisProvider.parseQueryFundamental(query);
            } catch (RecognitionException e) {
                String s = "Could not parse query "+query+" from file "+path+": "+e.getMessage();
                logger.warning(s);
                rc.addJavaAnalysisError(s);
                continue;
            } catch(MakumbaError me) {
                String s = "Could not parse query "+query+" from file "+path+": "+me.getMessage();
                logger.warning(s);
                continue;
            }
            

            Dictionary<String, String> projections = qA.getProjections();
            for (Enumeration<String> e = projections.elements(); e.hasMoreElements();) {
                String expr = e.nextElement();
                String field = qA.getFieldOfExpr(expr);
                DataDefinition dd = qA.getTypeOfExprField(expr);
                String realExpr;

                // this is due to a count(something) or sum(something) etc.
                // let's see if we can get the guy inside
                if (dd == null) {
                    int n = -1;
                    if ((n = expr.indexOf("(")) > -1) {
                        realExpr = expr.substring(n + 1, expr.length() - 1);
                        if (realExpr.equals("*")) {
                            // count(*)
                            continue;
                        }
                        field = qA.getFieldOfExpr(realExpr);
                        dd = qA.getTypeOfExprField(realExpr);
                    }
                }
                
                String type = dd.getName();
                if (type.indexOf("->") > -1) {
                    field = type.substring(type.indexOf("->") + 2) + "." + field;
                    type = type.substring(0, type.indexOf("->"));
                }
                
                addJava2MDDRelation(path, type, expr, field, query);
            }

            Map<String, DataDefinition> labelTypes = qA.getLabelTypes();
            Iterator<String> labelIterator = labelTypes.keySet().iterator();
            while (labelIterator.hasNext()) {
                String labelName = labelIterator.next();
                String type = labelTypes.get(labelName).getName();
                String field = type.indexOf("->") > -1 ? type.substring(type.indexOf("->") + 2)
                        : type.substring(type.lastIndexOf(".") + 1);
                if (type.indexOf("->") > -1) {
                    type = type.substring(0, type.indexOf("->"));
                }

                addJava2MDDRelation(path, type, labelName, field, query);
            }
        }
    }

    private void computeJava2JavaRelations(String path) {

        JavaParseData jpd = JavaParseData.getParseData(rc.getWebappRoot(), path, JavaSourceAnalyzer.getInstance());
        jpd.getAnalysisResult(null);

        if (webappJavaPackages == null) {
            readMakumbaControllerPackages();
        }

        Hashtable<String, String> importedClasses = jpd.getImportedClasses();
        for (String key : importedClasses.keySet()) {
            String className = importedClasses.get(key);

            // let's see if this class is in one package of the webapp
            boolean isWebappJava = false;
            Iterator<String> packages = webappJavaPackages.iterator();
            while (packages.hasNext() && !isWebappJava) {
                isWebappJava = className.startsWith(packages.next());
            }

            if (isWebappJava) {
                addJava2JavaRelation(path, className, "import");
            }
        }
    }

    /**
     * Reads MakumbaController.properties and extracts the packages
     */
    private void readMakumbaControllerPackages() {
        String makumbaControllerPath = rc.getWebappRoot() + File.separator
                + "/WEB-INF/classes/MakumbaController.properties";
        Properties p = null;
        try {
            p.load(new FileInputStream(makumbaControllerPath));
        } catch (Exception e) {
            logger.severe("Could not read " + makumbaControllerPath
                    + ", hence relations in Java files won't be correctly analysed.");
            return;
        }

        Iterator<Object> it = p.keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            String packageName = p.getProperty(key);
            if (packageName != null && packageName.length() > 0) {
                webappJavaPackages.add(packageName);
            }
        }

        // TODO remove sub-packages to increase performance

    }

    /**
     * Adds a Java -> Java relation
     * 
     * @param fromFile
     *            the file this relation originates from
     * @param className
     *            the name of the class this relations points to
     * @param reason
     *            the reason of the dependency
     */
    private void addJava2JavaRelation(String fromFile, String className, String reason) {
        String toFile = "/WEB-INF/classes/" + className.replace(".", File.separator);

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("reason", reason);

        relation.put("origin", relationOrigin);

        rc.addRelation(toFile, relation);
    }

    /**
     * Adds a Java -> MDD relation
     * 
     * @param fromFile
     *            the file this relation originates from
     * @param type
     *            the type of the MDD this relation points to
     * @param expr
     *            the expression that leads to the dependency
     * @param field
     *            the field of the MDD pointed by the relation
     * @param query
     *            the query this relation originates from
     */
    private void addJava2MDDRelation(String fromFile, String type, String expr, String field, String query) {
        String fileName = "/WEB-INF/classes/dataDefinitions/" + type.replace(".", "/") + ".mdd";

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("expr", expr);
        relationOrigin.put("field", field);
        relationOrigin.put("reason", query);

        relation.put("origin", relationOrigin);

        rc.addRelation(fileName, relation);

    }

}
