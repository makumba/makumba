package org.makumba.devel.relations;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.forms.tags.BasicValueTag;
import org.makumba.forms.tags.FormTagBase;
import org.makumba.forms.tags.InputTag;
import org.makumba.forms.tags.NewTag;
import org.makumba.forms.tags.SearchFieldTag;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.tags.GenericListTag;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;
import org.makumba.providers.query.oql.OQLQueryAnalysisProvider;

import antlr.RecognitionException;

public class JSPRelationMiner extends RelationMiner {

    public JSPRelationMiner(RelationCrawler rc) {
        super(rc);
    }

    public static final String PROJECTION_ORIGIN_CACHE = "org.makumba.projectionOrigin";

    @Override
    public void crawl(String path) {
        JspParseData jpd = JspParseData.getParseData(rc.getWebappRoot(), path, JspRelationsAnalyzer.getInstance());
        PageCache pageCache = null;
        try {
            pageCache = (PageCache) jpd.getAnalysisResult(new RelationParseStatus());
        } catch (Throwable t) {
            // page analysis failed
            logger.warning(
                "Page analysis for page " + path + " failed due to error: " + t.getMessage());
            rc.addJSPAnalysisError(path, t);
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
                    try {
                        computeJSPMDDLabelRelations(path, pageCache, queryKey, cq);
                    } catch (RuntimeException e) {
                        System.out.println("Could not compute JSP<->MDD label relation for " + path + ": " + e.getMessage());
                    }
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
            if (tag instanceof InputTag && !(tag instanceof SearchFieldTag)) { // skip search field tags. FIXME: at least for now
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
                    String ql = MakumbaJspAnalyzer.getQueryLanguage(pageCache);
                    if (ql.equals("oql")) {
                        try {
                            qA = OQLQueryAnalysisProvider.parseQueryFundamental(typeDeterminationQuery);
                        } catch (RecognitionException e) {
                            logger.warning("Could not determine type using query "+typeDeterminationQuery+" in file "+fromFile);
                            return;
                            
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
            toPage = new File(rc.getWebappRoot() + toPage).getCanonicalPath();
            toPage = toPage.substring(rc.getWebappRoot().length());
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
        for (String labelName : labels) {            
            TagData td = (TagData) pageCache.retrieve(TagData.TAG_DATA_CACHE, (MultipleKey) queryKey);

            // if td is null it means that we have a dummy query, not interesting to us
            if (td != null) {

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
                DataDefinition projectionParentType = null;
                try {
                    projectionParentType = cq.getTypeOfExprField(projectionExpr);
                    
                } catch(RuntimeWrappedException e) {
                    rc.addJSPAnalysisError(fromFile, e.getCause());
                    continue;
                }

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
                    logger.warning(
                        "Error while crawling file " + fromFile
                                + ": could not figure out type of the parent of field pointed by expression "
                                + projectionExpr);
                    continue;
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

        rc.addRelation(fileName, relation);
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

        rc.addRelation(toFile, relation);
    }
}
