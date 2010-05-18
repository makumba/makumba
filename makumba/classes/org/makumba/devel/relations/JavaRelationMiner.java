package org.makumba.devel.relations;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.MakumbaError;
import org.makumba.analyser.engine.JavaParseData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.devel.JavaSourceAnalyzer;
import org.makumba.providers.Configuration;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.query.mql.MqlQueryAnalysis;

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

    private Collection<String> webappJavaPackages = new Vector<String>();

    public JavaRelationMiner(RelationCrawler rc) {
        super(rc);
    }

    @Override
    public void crawl(String path) {

        if (!new File(rc.getWebappRoot() + File.separator + path).exists()) {
            logger.warning("Java file " + path + " does not exist in webapp " + rc.getWebappRoot());
            return;
        }

        computeJava2JavaRelations(path);
        computeJava2MDDRelations(path);
    }

    private void computeJava2MDDRelations(String path) {
        JavaMDDParser jqp = new JavaMDDParser(rc.getWebappRoot() + File.separator + path);
        Vector<String> queries = jqp.getQueries();

        // let's try to analyse the queries and extract the relations due to projections and labels

        // FIXME: this should use MQL to do analysis
        // that will work for both HQL and MQL (and thus OQL) queries
        // the query parsers already know well which MDDs and relations are used, they could simply be asked to produce
        // the list
        // that would be just one method in QueryaAnalysis (or in MqlQueryAnalysis directly!) instead of three as it was
        for (String query : queries) {
            // FIXME for the moment, we only have OQL queries in BL. but this won't be necessarily true in the future.
            MqlQueryAnalysis qA = null;
            try {
                qA = (MqlQueryAnalysis) QueryProvider.getQueryAnalzyer(MakumbaJspAnalyzer.QL_OQL).getQueryAnalysis(
                    query);
            } catch (MakumbaError me) {
                String s = "Could not parse query " + query + " from file " + path + ": " + me.getMessage();
                logger.warning(s);
                continue;
            } catch (NullPointerException npe) {
                String s = "Could not parse query " + query + " from file " + path + ": " + npe.getMessage();
                logger.warning(s);
                continue;

            }

            Vector<String> projections = qA.getProjectionType().getFieldNames();
            for (String expr : projections) {
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

                if (dd == null) {
                    logger.warning("Could not parse query " + query + " from file " + path);
                } else {
                    String type = dd.getName();
                    if (type.indexOf("->") > -1) {
                        field = type.substring(type.indexOf("->") + 2) + "." + field;
                        type = type.substring(0, type.indexOf("->"));
                    }

                    addJava2MDDRelation(path, type, expr, field, query);
                }
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
            webappJavaPackages = Configuration.getLogicPackages().values();
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
        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("reason", reason);
        rc.addRelation(fromFile, toFile, "java2java", relationOrigin);
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
        String toFile = "/WEB-INF/classes/dataDefinitions/" + type.replace(".", "/") + ".mdd";
        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("expr", expr);
        relationOrigin.put("field", field);
        relationOrigin.put("reason", query);
        rc.addRelation(fromFile, toFile, "java2MDD", relationOrigin);
    }
}
