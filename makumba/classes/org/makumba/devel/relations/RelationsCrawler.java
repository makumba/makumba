package org.makumba.devel.relations;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.MultipleKey;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.tags.GenericListTag;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;
import org.makumba.providers.query.oql.OQLQueryAnalysisProvider;

import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.forms.tags.BasicValueTag;
import org.makumba.forms.tags.FormTagBase;
import org.makumba.forms.tags.InputTag;
import org.makumba.forms.tags.NewTag;

import antlr.RecognitionException;

/**
 * This crawler looks for relations between Makumba files and stores them in a database table.<br>
 * It is ran with the following arguments: RelationCrawler webappRoot destinationDb [fileList]<br>
 * where destinationDb is the database (e.g. localhost_mysql_karamba) to which the relations table should be written.
 * 
 * FIXME currently this needs to have the webapp's dataDefinitions folder in the classpath in order to run
 * 
 * @author Manuel Gay
 * @version $Id: RelationsCrawler.java,v 1.1 Apr 13, 2008 4:16:16 PM manu Exp $
 */
public class RelationsCrawler {

    private static final String MDD_PATH = "/WEB-INF/classes/dataDefinitions/";

    protected static boolean subProcess = false;

    public static final String PROJECTION_ORIGIN_CACHE = "org.makumba.projectionOrigin";

    public static final String QUERY_ORIGIN_CACHE = "org.makumba.projectionOrigin";

    public static final String INPUT_ORIGIN_CACHE = "org.makumba.inputOrigin";

    private String webappRoot;

    private String targetDatabase;

    private boolean forceDatabase;

    private Map<String, Dictionary<String, Object>> detectedRelations = new HashMap<String, Dictionary<String, Object>>();

    public RelationsCrawler(String webappRoot, String targetDatabase, boolean forcetarget) {
        this.webappRoot = webappRoot;
        this.targetDatabase = targetDatabase;
        this.forceDatabase = forcetarget;
    }

    public Map<String, Dictionary<String, Object>> getDetectedRelations() {
        return this.detectedRelations;
    }

    public String getRelationDatabase() {
        return targetDatabase;
    }

    public void setRelationDatabase(String relationDatabase) {
        this.targetDatabase = relationDatabase;
    }

    public static void main(String[] args) {
        
        if(args.length == 0) {
            // composing example start arguments
            String webappPath = "/home/manu/workspace/karamba/public_html/";

            Vector<String> arguments = new Vector<String>();
            arguments.add(webappPath);
            arguments.add("localhost_mysql_makumba");
            arguments.add("forcetarget");
            File dir = new File(webappPath + "archive/");
            String[] files = dir.list();
            for (int i = 0; i < files.length; i++) {
                if (new File(webappPath + "archive/" + files[i]).isFile()) {
                    arguments.add("/archive/" + files[i]);
                }
            }

            File dir2 = new File(webappPath + "WEB-INF/classes/dataDefinitions/best");
            String[] files2 = dir2.list();
            for (int i = 0; i < files2.length; i++) {
                if (new File(webappPath + "WEB-INF/classes/dataDefinitions/best/" + files2[i]).isFile()) {
                    arguments.add("/WEB-INF/classes/dataDefinitions/best/" + files2[i]);
                }
            }

            // String[] args1 = { "/home/manu/workspace/karamba/public_html", "localhost_mysql_makumba", "forcetarget",
            // "/archive/documentEdit.jsp" };
            // args = args1;

            args = (String[]) arguments.toArray(new String[arguments.size()]);
        }

        
        
        System.out.println("RelationsCrawler main");

        String webappRoot = args[0];
        String targetDatabase = args[1];
        String forceDatabase = args[2];

        String[] path = new String[args.length - 3];

        for (int i = 3; i < args.length; i++) {
            path[i - 3] = args[i];
        }

        // this if when we run the guy from the command line directly with the right CP
        boolean rightClassPath = RelationsCrawler.class.getClassLoader().getResource("general/Person.mdd") != null;
        if (!rightClassPath && !subProcess) {
            subProcess = true;
            System.out.println("subprocess is " + subProcess);
            System.out.println("right class path " + rightClassPath);
            Runtime r = Runtime.getRuntime();
            Vector<String> cmd = new Vector<String>();
            cmd.add("java");
            cmd.add("-cp");
            cmd.add(webappRoot + "/WEB-INF/classes/dataDefinitions/:.");
            cmd.add("org.makumba.devel.relations.RelationsCrawler");
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    cmd.add(args[i]);
            }
            String[] cmds = (String[]) cmd.toArray(new String[cmd.size()]);

            File f = new File("./classes");
            System.out.println(f.getAbsolutePath());
            System.out.println(Arrays.toString(cmds));

            try {
                Process p = r.exec(cmds, null, f);
                p.waitFor();
                System.out.println("Executed.");
                int c;
                while ((c = p.getInputStream().read()) != -1) {
                    System.out.write(c);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("We have the right classpath");

            RelationsCrawler rc = new RelationsCrawler(webappRoot, targetDatabase, forceDatabase != null);

            for (int i = 0; i < path.length; i++) {
                rc.crawl(path[i]);
            }

            rc.writeRelationsToDb();
        }
    }

    /**
     * Crawls through a file, if this one is supported
     * 
     * @param path
     *            the path to the file
     */
    public void crawl(String path) {
        if (path.endsWith(".jsp")) {
            crawlThroughJSP(path);
        } else if (path.endsWith(".mdd")) {
            crawlThroughMDD(path);

        }
    }

    /**
     * Crawls through the fields of an MDD and creates a relation whenever an external pointer or set is found
     * 
     * @param path
     *            the path to the MDD
     */
    public void crawlThroughMDD(String path) {
        DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

        String type = path.substring(MDD_PATH.length(), path.length() - 4).replace('/', '.');

        DataDefinition dd = ddp.getDataDefinition(type);

        Vector<String> fields = dd.getFieldNames();
        for (Iterator<String> iterator = fields.iterator(); iterator.hasNext();) {
            String fieldName = iterator.next();
            FieldDefinition fd = dd.getFieldDefinition(fieldName);

            if (fd.getType().equals("ptr") || fd.getType().equals("set")) {
                addMDD2MDDRelation(path, typeToPath(fd.getPointedType().getName()), fd.getName());
            }
        }
    }

    /**
     * Transforms a MDD name into the relative path to the MDD
     * 
     * @param typeName
     *            the name of the type
     * @return the path relative to the webapp root
     */
    private String typeToPath(String typeName) {
        return MDD_PATH + typeName.replace('.', '/') + ".mdd";
    }

    /**
     * Crawls through a page, computes relations and stores them in the database.
     */
    public void crawlThroughJSP(String path) {
        JspParseData jpd = JspParseData.getParseData(webappRoot, path, JspRelationsAnalyzer.getInstance());
        PageCache pageCache = null;
        try {
            pageCache = (PageCache) jpd.getAnalysisResult(new RelationsParseStatus());
        } catch (Throwable t) {
            // page analysis failed
            MakumbaSystem.getLogger().warning(
                "Page analysis for page " + path + " failed due to error: " + t.getMessage());
            return;
        }

        Map<Object, Object> queryCache = pageCache.retrieveCache(GenericListTag.QUERY);

        if (queryCache != null) {
            Iterator<Object> it = queryCache.keySet().iterator();
            while (it.hasNext()) {
                Object queryKey = it.next();
                Object query = queryCache.get(queryKey);
                if (query instanceof ComposedQuery) {
                    ComposedQuery cq = (ComposedQuery) query;

                    // System.out.println("Projections for query "+cq.getTypeAnalyzerQuery());

                    computeJSPMDDProjectionRelations(path, pageCache, queryKey, cq);
                    computeJSPMDDLabelRelations(path, pageCache, queryKey, cq);
                }
            }
        }

        Map<Object, Object> tagDataCache = pageCache.retrieveCache(TagData.TAG_DATA_CACHE);

        if (tagDataCache != null) {
            Iterator<Object> it2 = tagDataCache.keySet().iterator();
            while (it2.hasNext()) {
                Object tagKey = (Object) it2.next(); // tagData key = tag key
                TagData tagData = (TagData) tagDataCache.get(tagKey);

                computeJSPMDDFormRelations(path, pageCache, tagDataCache, tagKey, tagData);
                computeJSPJSPRelations(tagData, path);

            }
        }
    }

    /**
     * Computes the relations between two JSP pages, based on a tag (jsp:include or
     * 
     * @include)
     * @param tagData
     *            the data of the tag that contains a potential relation
     */
    private void computeJSPJSPRelations(TagData tagData, String currentPage) {

        if (tagData.name.equals("jsp:include")) {
            String toPage = tagData.attributes.get("page");
            toPage = transformIncludePath(toPage, currentPage);
            addJSP2JSPRelation(currentPage, toPage, tagData);

        }

        if (tagData.name.equals("include")) {
            String toPage = tagData.attributes.get("file");
            toPage = transformIncludePath(toPage, currentPage);
            addJSP2JSPRelation(currentPage, toPage, tagData);

        }
    }

    /**
     * Computes relations for makumba forms. The only tags that are interesting are the newTag-s and the input tag-s,
     * since the others (edit, add, delete) need anyway a query tag to run (they won't be a source of a direct relation
     * to a type, but only use an existing one)
     * 
     * @param fromFile
     *            the file from which the relation originates
     * @param pageCache
     *            the analysis status
     * @param tagDataCache
     *            the cache containing the tagData
     * @param tagKey
     *            the key of the current tag
     * @param tagData
     *            the tagData containing information about the current tag
     */
    private void computeJSPMDDFormRelations(String fromFile, PageCache pageCache, Map<Object, Object> tagDataCache,
            Object tagKey, TagData tagData) {

        Object tag = tagData.getTagObject();
        if (tag != null) {

            // let's first look at the NewTag-s
            if (tag instanceof NewTag) {
                String baseObjectType = tagData.attributes.get("type");
                String field = baseObjectType.substring(baseObjectType.lastIndexOf(".") + 1);

                addJSP2MDDRelation(fromFile, baseObjectType, tagData, baseObjectType, field);

            }

            // we only look at the input tags
            if (tag instanceof InputTag) {
                MultipleKey formTagKey = ((InputTag) tag).getForm().getTagKey();

                String baseObjectType = (String) pageCache.retrieve(FormTagBase.BASE_POINTER_TYPES, formTagKey);

                // for some strange reason this happens for newTag-s types
                if (baseObjectType == null) {
                    baseObjectType = ((TagData) tagDataCache.get(formTagKey)).attributes.get("type");
                }

                FieldDefinition tagFieldType = (FieldDefinition) pageCache.retrieve(BasicValueTag.INPUT_TYPES, tagKey);
                String expr = tagData.attributes.get("field") == null ? tagData.attributes.get("name")
                        : tagData.attributes.get("field");

                // if the expression is not a field of the current type, we need to re-compute the type
                if (expr.indexOf(".") > -1) {

                    String typeDeterminationQuery = "SELECT typeLabel." + expr.substring(0, expr.lastIndexOf("."))
                            + " AS type FROM " + baseObjectType + " typeLabel";
                    // we create the query analysis, based on the QL of the page
                    QueryAnalysis qA = null;
                    String ql = (String) pageCache.retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE,
                        MakumbaJspAnalyzer.QUERY_LANGUAGE);
                    if (ql.equals("oql")) {
                        try {
                            qA = OQLQueryAnalysisProvider.parseQueryFundamental(typeDeterminationQuery);
                        } catch (RecognitionException e) {
                            e.printStackTrace();
                        }
                    } else if (ql.equals("hql")) {
                        qA = HQLQueryAnalysisProvider.getHqlAnalyzer(typeDeterminationQuery);
                    }

                    baseObjectType = qA.getProjectionType().getFieldDefinition("type").getPointedType().getName();

                    // FIXME in some rare cases, e.g. <mak:input name="some" value="expr" /> this doesn't work
                    // so we need to compute the type for "expr", by retrieving the CQ corresponding to the input's
                    // parent list

                    if (baseObjectType != null) {
                        addJSP2MDDRelation(fromFile, baseObjectType, tagData, expr, tagFieldType.getName());
                    }
                }
            }
        }
    }

    /**
     * Transforms the path of a file contained in a include directive to be "nice".
     * 
     * @param toPage
     *            the path to the page
     * @return the transformed path
     */
    private String transformIncludePath(String toPage, String currentPage) {
        // compute the right path from webappRoot
        if (!toPage.startsWith("/")) {
            toPage = new File(currentPage).getParent() + File.separator + toPage;
        }

        // filter out attributes
        if (toPage.indexOf("?") > -1) {
            toPage = toPage.substring(0, toPage.indexOf("?"));
        }

        // filter out anchors
        if (toPage.indexOf("#") > -1) {
            toPage = toPage.substring(0, toPage.indexOf("#"));
        }

        // let's make the path canonical
        try {
            toPage = new File(webappRoot + toPage).getCanonicalPath();
            toPage = toPage.substring(webappRoot.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toPage;
    }

    /**
     * Computes relations between the labels of a {@link ComposedQuery} and their originating tag in a makumba page.
     * 
     * @param fromFile
     *            the file from which the relation originates
     * @param pageCache
     *            the pageCache containing the makumba page analysis
     * @param queryKey
     *            the key of the ComposedQuery, used to retrieve the tag responsible for a projection
     * @param cq
     *            the ComposedQuery to analyse
     */
    private void computeJSPMDDLabelRelations(String fromFile, PageCache pageCache, Object queryKey, ComposedQuery cq) {

        Map<String, DataDefinition> labelTypes = cq.getFromLabelTypes();
        Set<String> labels = labelTypes.keySet();
        for (Iterator<String> iterator = labels.iterator(); iterator.hasNext();) {
            String labelName = iterator.next();
            TagData td = (TagData) pageCache.retrieve(TagData.TAG_DATA_CACHE, (MultipleKey) queryKey);
            if (td != null) {
                // if td is null it means that we have a dummy query

                String type = cq.getFromLabelTypes().get(labelName).getName();
                String field = type.indexOf("->") > -1 ? type.substring(type.indexOf("->") + 2)
                        : type.substring(type.lastIndexOf(".") + 1);
                if (type.indexOf("->") > -1) {
                    type = type.substring(0, type.indexOf("->"));
                }

                addJSP2MDDRelation(fromFile, type, td, labelName, field);
            }
        }
    }

    /**
     * Computes relations between the projections in a makumba page (mak:values) and the fields of an MDD. It determines
     * which tag is responsible for which projection expression in a {@link ComposedQuery} of a page, as well as the
     * field the expression is pointing to.
     * 
     * @param fromFil
     *            the file from which the relation originates
     * @param pageCache
     *            the pageCache containing the makumba page analysis
     * @param queryKey
     *            the key of the ComposedQuery, used to retrieve the tag responsible for a projection
     * @param cq
     *            the ComposedQuery to analyse
     */
    private void computeJSPMDDProjectionRelations(String fromFile, PageCache pageCache, Object queryKey,
            ComposedQuery cq) {
        // let's see where the projections of the queries come from
        Vector<Object> projections = cq.getProjections();

        for (Iterator<Object> iterator = projections.iterator(); iterator.hasNext();) {
            String projectionExpr = (String) iterator.next();
            MultipleKey valueTagKey = (MultipleKey) pageCache.retrieve(PROJECTION_ORIGIN_CACHE, new MultipleKey(
                    (MultipleKey) queryKey, projectionExpr));

            // if we don't get anything here it means that this CQ is not interesting for us
            if (valueTagKey != null) {
                TagData td = (TagData) pageCache.retrieve(TagData.TAG_DATA_CACHE, valueTagKey);

                String field = cq.getFieldOfExpr(projectionExpr);
                String realExpr = null;
                DataDefinition projectionParentType = cq.getTypeOfExprField(projectionExpr);

                // this is due to a count(something) or sum(something) etc.
                // let's see if we can get the guy inside
                if (projectionParentType == null) {
                    int n = -1;
                    if ((n = projectionExpr.indexOf("(")) > -1) {
                        realExpr = projectionExpr.substring(n + 1, projectionExpr.length() - 1);
                        if (realExpr.equals("*")) {
                            // count(*)
                            continue;
                        }
                        field = cq.getFieldOfExpr(realExpr);
                        projectionParentType = cq.getTypeOfExprField(realExpr);
                    }
                }

                if (projectionParentType == null) {
                    MakumbaSystem.getLogger().warning(
                        "Error while crawling file " + fromFile
                                + ": could not figure out type of the parent of field pointed by expression "
                                + projectionExpr);
                }

                String type = projectionParentType.getName();
                // we select a label
                if (field.equals(realExpr == null ? projectionExpr : realExpr) && type.indexOf("->") == -1) {
                    field = type.substring(type.lastIndexOf(".") + 1);
                }

                // if we have a setComplex or ptrOne, we modify the type so as to have a pure mdd type, and append the
                // setComplex to the field
                if (type.indexOf("->") > -1) {
                    field = type.substring(type.indexOf("->") + 2) + "." + field;
                    type = type.substring(0, type.indexOf("->"));
                }

                // System.out.println(td.name + " at line " + td.getStartLine()
                // + " is responsible for projection " + projectionExpr + " on field "+field+" of type
                // "+projectionParentType);

                addJSP2MDDRelation(fromFile, type, td, projectionExpr, field);

            }
        }
    }

    /**
     * Adds a JSP -> MDD relation
     * 
     * @param type
     *            the MDD type
     * @param originTagData
     *            the tagData of the tag from which the relation originates
     * @param expr
     *            the expression that leads to the relation
     * @param field
     *            the field of the MDD pointed by the relation
     */
    private void addJSP2MDDRelation(String fromFile, String type, TagData originTagData, String expr, String field) {

        // System.out.println(originTagData.name + " at line " + originTagData.getStartLine()
        // + " is responsible for expression " + expr + " affecting field "+field+" of type "+type);

        String fileName = "/WEB-INF/classes/dataDefinitions/" + type.replace(".", "/") + ".mdd";

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("startcol", originTagData.getStartColumn());
        relationOrigin.put("endcol", originTagData.getEndColumn());
        relationOrigin.put("startline", originTagData.getStartLine());
        relationOrigin.put("endline", originTagData.getEndLine());
        relationOrigin.put("tagname", originTagData.name);
        relationOrigin.put("expr", expr);
        relationOrigin.put("field", field);

        relation.put("origin", relationOrigin);

        addRelation(fileName, relation);
    }

    /**
     * Adds a JSP -> JSP relation
     * 
     * @param fromFile
     *            the file this relation originates from
     * @param toFile
     *            the JSP of the relation
     * @param originTagData
     *            the tagData of the tag responsible for the relation
     */
    private void addJSP2JSPRelation(String fromFile, String toFile, TagData originTagData) {

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("startcol", originTagData.getStartColumn());
        relationOrigin.put("endcol", originTagData.getEndColumn());
        relationOrigin.put("startline", originTagData.getStartLine());
        relationOrigin.put("endline", originTagData.getEndLine());
        relationOrigin.put("tagname", originTagData.name);

        relation.put("origin", relationOrigin);

        addRelation(toFile, relation);
    }

    /**
     * Adds a MDD -> MDD relation
     * 
     * @param fromFile
     *            the file this relation originates from
     * @param toFile
     *            the MDD of the relation
     * @param fromField
     *            the field responsible for the relation
     */
    private void addMDD2MDDRelation(String fromFile, String toFile, String fromField) {

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("field", fromField);

        relation.put("origin", relationOrigin);

        addRelation(toFile, relation);
    }

    /**
     * Adds a relation which will later on be written to the database
     * 
     * @param toFile
     *            the path to the file there is a relation with
     * @param relationData
     *            the relation data
     */
    public void addRelation(String toFile, Dictionary<String, Object> relationData) {

        Dictionary<String, Object> rel;
        if ((rel = detectedRelations.get(toFile)) != null) {

            // we just add the detail
            Vector<Dictionary<String, Object>> origin = (Vector<Dictionary<String, Object>>) rel.get("origin");
            origin.add((Dictionary<String, Object>) relationData.get("origin"));
            rel.put("origin", origin);

            detectedRelations.put(toFile, rel);

        } else {
            // we extract the relation detail, and add it to a detail Vector of Dictionaries
            Dictionary<String, Object> originData = (Dictionary<String, Object>) relationData.get("origin");
            Vector<Dictionary<String, Object>> origin = new Vector<Dictionary<String, Object>>();
            origin.add(originData);

            relationData.put("origin", origin);

            detectedRelations.put(toFile, relationData);
        }

    }

    /**
     * Writes the relations to the database TODO make sure the previous data is removed, before writing the new
     * relations
     */
    public void writeRelationsToDb() {
        // here we save all the computed relations to the relations database

        Map<String, Dictionary<String, Object>> relations = getDetectedRelations();

        TransactionProvider tp = TransactionProvider.getInstance();

        Pointer webappPointer = determineRelationsDatabase(tp, forceDatabase);

        Iterator<String> it = relations.keySet().iterator();
        while (it.hasNext()) {
            String toFile = it.next();
            Dictionary<String, Object> relationInfo = relations.get(toFile);
            String relationType = (String) relationInfo.get("type");
            String fromFile = (String) relationInfo.get("fromFile");

            System.out.println(fromFile + " -(" + relationType + ")-> " + toFile);

            // now we insert the records into the relations table, in the right database
            Transaction tr2 = null;

            try {
                tr2 = tp.getConnectionTo(targetDatabase);

                // we check if there's already such a relation in the database

                Object[] args = { toFile, fromFile };
                Vector<Dictionary<String, Object>> previousRelation = tr2.executeQuery(
                    "SELECT relation AS relation FROM org.makumba.devel.relations.Relation relation WHERE relation.toFile = $1 AND relation.fromFile = $2",
                    args);

                if (previousRelation.size() > 0) {
                    // we delete the previous relation origin

                    Pointer previousRelationPtr = (Pointer) previousRelation.get(0).get("relation");

                    Vector<Dictionary<String, Object>> previousRelationOrigin = tr2.executeQuery(
                        "SELECT origin AS origin FROM org.makumba.devel.relations.Relation relation, relation.origin origin WHERE relation = $1",
                        new Object[] { previousRelationPtr });

                    for (Iterator iterator = previousRelationOrigin.iterator(); iterator.hasNext();) {
                        Dictionary<String, Object> dictionary = (Dictionary<String, Object>) iterator.next();
                        tr2.delete((Pointer) dictionary.get("origin"));
                    }

                    // we now delete the relation itself
                    tr2.delete(previousRelationPtr);

                }

                relationInfo.put("toFile", toFile);
                relationInfo.put("webapp", webappPointer);

                Vector<Pointer> originSet = new Vector<Pointer>();

                Vector<Dictionary<String, Object>> relationOrigins = (Vector<Dictionary<String, Object>>) relationInfo.get("origin");

                for (Iterator iterator = relationOrigins.iterator(); iterator.hasNext();) {
                    Dictionary<String, Object> dictionary = (Dictionary<String, Object>) iterator.next();
                    originSet.add(tr2.insert("org.makumba.devel.relations.RelationOrigin", dictionary));
                }

                relationInfo.put("origin", originSet);

                tr2.insert("org.makumba.devel.relations.Relation", relationInfo);

            } finally {
                tr2.close();
            }
        }

    }

    /**
     * Figures out the relations database, i.e. to which database relations should be written to, and if there's none,
     * creates an entry in the default database (per webappRoot).
     * 
     * @param tp
     *            the TransactionProvider that makes it possible to run the determination query
     * @param forceDestination
     *            whether or not to force the database to write to. If enabled, will also update the relations database
     *            record in the default database.
     * @return a Pointer to the record in the default database that points to the relations database
     */
    private Pointer determineRelationsDatabase(TransactionProvider tp, boolean forceDestination) {

        Pointer webappPointer = null;

        // first we are going to check in which db the relations are, if there are any
        Transaction tr = null;
        try {
            tr = tp.getConnectionTo(tp.getDefaultDataSourceName());
            Vector<Dictionary<String, Object>> databaseLocation = tr.executeQuery(
                "SELECT wdb AS webappPointer, wdb.relationDatabase AS relationDatabase from org.makumba.devel.relations.WebappDatabase wdb WHERE wdb.webappRoot = $1",
                new String[] { webappRoot });
            if (databaseLocation.size() > 1) {
                // that's too much
                throw new RuntimeException("Too many possible locations for the relations database of webapp "
                        + webappRoot);
            } else if (databaseLocation.size() == 1) {

                if (forceDestination) {
                    // we set the location to the one provided at execution
                    Dictionary<String, String> data = new Hashtable<String, String>();
                    data.put("relationDatabase", targetDatabase);
                    Pointer oldPointer = webappPointer = (Pointer) databaseLocation.get(0).get("webappPointer");
                    tr.update(oldPointer, data);
                } else {
                    // we re-use the previous location
                    targetDatabase = (String) databaseLocation.get(0).get("relationDatabase");
                    webappPointer = (Pointer) databaseLocation.get(0).get("webappPointer");
                }
            } else if (databaseLocation.size() == 0) {
                // we set the location to the one provided at execution
                Dictionary<String, String> data = new Hashtable<String, String>();
                data.put("webappRoot", webappRoot);
                data.put("relationDatabase", targetDatabase);
                webappPointer = tr.insert("org.makumba.devel.relations.WebappDatabase", data);
            }
        } finally {
            tr.close();
        }

        return webappPointer;

    }

}
