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
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.Expression;

import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.AnalysableExpression;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.ELData;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.interfaces.JspAnalyzer;

/**
 * This class analyzes a JSP taking into account the specifics of Makumba tags. When implementing a new Makumba tag, be
 * sure to add it to on of {@link #listTags}, {@link #oldFormTags} or {@link #formTags}. TODO maybe extract some
 * commonly used methods and add them to a generic analyser
 * <p>
 * TODO maybe extract some commonly used methods and add them to a generic analyser
 * </p>
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class MakumbaJspAnalyzer implements JspAnalyzer {

    // cache keys, centralised in one place to have an overview of what is cached

    public static final String TAG_CACHE = "org.makumba.tags";
    
    public static final String EL_CACHE = "org.makumba.el";

    public static final String EL_DATA_CACHE = "org.makumba.elData";
    
    public static final String TAG_DATA_CACHE = "org.makumba.tagData";

    public static final String FORM_TAGS_DEPENDENCY_CACHE = "org.makumba.dependency";

    public static final String NESTED_FORM_NAMES = "org.makumba.nestedFormNames";

    public static final String INPUT_TYPES = "org.makumba.inputtypes";

    public static final String LAZY_EVALUATED_INPUTS = "org.makumba.unresolvedInputs";

    public static final String BASE_POINTER_TYPES = "org.makumba.basePointerTypes";

    public static final String VALUE_COMPUTERS = "org.makumba.valueComputers";

    public static final String QUERY = "org.makumba.query";

    public static final String QUERY_LANGUAGE = "org.makumba.queryLanguage";

    public static final String DS_ATTR = "org.makumba.database";

    public static final String FORMATTERS = "org.makumba.formatters";

    public static final String PROJECTION_ORIGIN_CACHE = "org.makumba.projectionOrigin";

    public static final String ADD_FORM_DATA_TYPE = "org.makumba.addFormDataType";
    
    public static final String SECTION_EVENT_TO_ID = "org.makumba.sectionsEventToId";

    public static final String SECTION_IDEVENT_TO_TYPE = "org.makumba.sectionsEventToType";
    
    public static final String SECTION_DATA = "org.makumba.sectionData";

    
    static String[] listTags = { "value", "org.makumba.list.tags.ValueTag", "list", "org.makumba.list.tags.QueryTag",
            "object", "org.makumba.list.tags.ObjectTag", "if", "org.makumba.list.tags.IfTag", "resultList",
            "org.makumba.list.tags.ResultListTag", "pagination", "org.makumba.list.pagination.PaginationTag",
            "section", "org.makumba.list.tags.SectionTag" };

    static String[] oldFormTags = { "form", "org.makumba.forms.tags.FormTagBase", "newForm",
            "org.makumba.forms.tags.NewTag", "addForm", "org.makumba.forms.tags.AddTag", "editForm",
            "org.makumba.forms.tags.EditTag", "deleteLink", "org.makumba.forms.tags.DeleteTag", "delete",
            "org.makumba.forms.tags.DeleteTag", "input", "org.makumba.forms.tags.InputTag", "action",
            "org.makumba.forms.tags.ActionTag", "option", "org.makumba.forms.tags.OptionTag", "searchForm",
            "org.makumba.forms.tags.SearchTag", "criterion", "org.makumba.forms.tags.CriterionTag", "searchField",
            "org.makumba.forms.tags.SearchFieldTag", "matchMode", "org.makumba.forms.tags.MatchModeTag", "submit",
            "org.makumba.forms.tags.SubmitTag" };

    static String[] formTags = { "form", "org.makumba.forms.tags.FormTagBase", "new", "org.makumba.forms.tags.NewTag",
            "add", "org.makumba.forms.tags.AddTag", "edit", "org.makumba.forms.tags.EditTag", "deleteLink",
            "org.makumba.forms.tags.DeleteTag", "delete", "org.makumba.forms.tags.DeleteTag", "input",
            "org.makumba.forms.tags.InputTag", "action", "org.makumba.forms.tags.ActionTag", "option",
            "org.makumba.forms.tags.OptionTag", "submit", "org.makumba.forms.tags.SubmitTag", "searchForm",
            "org.makumba.forms.tags.SearchTag", "criterion", "org.makumba.forms.tags.CriterionTag", "searchField",
            "org.makumba.forms.tags.SearchFieldTag", "matchMode", "org.makumba.forms.tags.MatchModeTag" };

    static String[] formTagNames = { "form", "newForm", "addForm", "editForm", "deleteLink", "delete", "searchForm",
            "new", "add", "edit", "submit" };
    
    static String[] elExpressions = { "expr", "org.makumba.list.tags.ValueExpression" };
    
    static String[] elExpressionNames = { "expr"};

    static final Map<String, Class<?>> tagClasses = new HashMap<String, Class<?>>();

    static final List<String> formTagNamesList = Arrays.asList(formTagNames);

    /**
     * Puts the Makumba tags into a Map
     */
    static {
        for (int i = 0; i < listTags.length; i += 2) {
            try {
                tagClasses.put(listTags[i], Class.forName(listTags[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (int i = 0; i < oldFormTags.length; i += 2) {
            try {
                tagClasses.put(oldFormTags[i], Class.forName(oldFormTags[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (int i = 0; i < formTags.length; i += 2) {
            try {
                tagClasses.put(formTags[i], Class.forName(formTags[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        for (int i = 0; i < elExpressions.length; i += 2) {
            try {
                tagClasses.put(elExpressions[i], Class.forName(elExpressions[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * SingletonHolder, to make sure there's only one instance for the MakumbaJspAnalyzer
     * 
     * @author Cristian Bogdan
     */
    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder{
        static JspAnalyzer singleton = new MakumbaJspAnalyzer();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    public static final String QL_OQL = "OQL";

    public static final String QL_HQL = "HQL";

    protected MakumbaJspAnalyzer() {
    }

    public static JspAnalyzer getInstance() {
        return SingletonHolder.singleton;
    }

    /**
     * Performs analysis for a system tag FIXME this should be thought of much more
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void systemTag(TagData td, Object status) {

        // JSP 2.0 introduced taglib directive with no uri: <%@taglib tagdir="/WEB-INF/tags" prefix="tags" %>
        if (td.name.equals("taglib")) {

            // we check if this is a Makumba taglib declaration
            if (td.attributes.get("uri") != null
                    && td.attributes.get("uri").toString().startsWith("http://www.makumba.org/")) {
                String prefix = td.attributes.get("prefix");
                String URI = td.attributes.get("uri");

                // if this is an old makumba system-tag or a OQL list
                if (URI.equals("http://www.makumba.org/presentation") || URI.equals("http://www.makumba.org/list")) {
                    ((ParseStatus) status).makumbaPrefix = prefix;
                    ((ParseStatus) status).makumbaURI = URI;
                    ((ParseStatus) status).pageCache.cache(QUERY_LANGUAGE, QUERY_LANGUAGE, "oql");

                    // if this is a hibernate tag or HQL list
                } else if (URI.equals("http://www.makumba.org/view-hql")
                        || URI.equals("http://www.makumba.org/hibernate")
                        || URI.equals("http://www.makumba.org/list-hql")) {
                    ((ParseStatus) status).makumbaPrefix = prefix;
                    ((ParseStatus) status).makumbaURI = URI;
                    ((ParseStatus) status).pageCache.cache(QUERY_LANGUAGE, QUERY_LANGUAGE, "hql");

                    // if this is a forms declaration
                } else if (URI.equals("http://www.makumba.org/forms")) {
                    ((ParseStatus) status).formPrefix = prefix;
                    ((ParseStatus) status).formMakumbaURI = URI;

                    // FIXME: here we actually shouldn't store a query language because that's the list's business
                    // however if there's a page that doesn't use lists but only forms, we have to do it because
                    // for now we use a ListFormDataProvider running dummy queries and hence needing a query language

                    if (((ParseStatus) status).pageCache.retrieve(QUERY_LANGUAGE, QUERY_LANGUAGE) == null) {
                        ((ParseStatus) status).pageCache.cache(QUERY_LANGUAGE, QUERY_LANGUAGE, "oql");
                    }

                }
            }
        } else {
            handleNonMakumbaSystemTags(td, status);
        }
    }

    /**
     * Performs analysis for a simple tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void simpleTag(TagData td, Object status) {
        String makumbaPrefix = ((ParseStatus) status).makumbaPrefix + ":";
        String formsPrefix = ((ParseStatus) status).formPrefix + ":";

        // we handle only Makumba tags
        if (!td.name.startsWith(makumbaPrefix) && !td.name.startsWith(formsPrefix)) {
            handleNonMakumbaTags(td, status);
        } else {
            // we retrieve the name of the tag to fetch its class
            String tagName = "";
            if (td.name.startsWith(makumbaPrefix)) {
                tagName = td.name.substring(makumbaPrefix.length());
            } else if (td.name.startsWith(formsPrefix)) {
                tagName = td.name.substring(formsPrefix.length());
            }

            Class<?> c = tagClasses.get(tagName);
            if (c == null) {
                return;
            }
            AnalysableElement.setAnalyzedElementData(td);
            AnalysableTag t = null;
            try {
                t = (AnalysableTag) c.newInstance();
            } catch (Throwable thr) {
                thr.printStackTrace();
            }
            // we set the tagObject of the tagData with the new tag object
            td.tagObject = t;
            t.setTagDataAtAnalysis(td);
            ((ParseStatus) status).addTag(t, td);
            AnalysableElement.setAnalyzedElementData(null);
        }
    }
    
    /**
     * Performs analysis for a EL expression (see {@link Expression})
     * 
     *  @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void elExpression(ELData ed, Object status) {
        // we accept only makumba EL stuff
        
        if(!StringUtils.startsWith(ed.getExpression(), elExpressionNames)) {
            return;
        }
        
        // find the type of the expression / method
        String type = StringUtils.getStartsWith(ed.getExpression(), elExpressionNames);
        
        Class<?> c = tagClasses.get(type);
        if (c == null) {
            return;
        }
        AnalysableElement.setAnalyzedElementData(ed);
        AnalysableExpression e = null;
        try {
            e = (AnalysableExpression) c.newInstance();
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        
        e.setELDataAtAnalysis(ed);
        e.treatELExpressionAtAnalysis(ed.getExpression());
        
        ((ParseStatus) status).addExpression(e, ed);
        AnalysableElement.setAnalyzedElementData(null);
    }

    /**
     * Handles non-mak tags. To be implemented by classes that extend this one
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    protected void handleNonMakumbaTags(TagData td, Object status) {
    }

    /**
     * Handles non-mak system tags. To be implemented by classes that extend this one
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */

    protected void handleNonMakumbaSystemTags(TagData td, Object status) {
    }

    /**
     * Performs analysis for the start of a tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void startTag(TagData td, Object status) {
        simpleTag(td, status);
        ((ParseStatus) status).start((AnalysableTag) td.tagObject);
    }

    /**
     * Performs analysis for the end of a tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void endTag(TagData td, Object status) {
        AnalysableElement.setAnalyzedElementData(td);
        ((ParseStatus) status).end(td);
        AnalysableElement.setAnalyzedElementData(null);
    }

    public Object makeStatusHolder(Object initialStatus) {
        return new ParseStatus();
    }

    public Object endPage(Object status) {
        ((ParseStatus) status).endPage();
        return ((ParseStatus) status).pageCache;
    }

    public static String getQueryLanguage(PageCache pageCache) {
        return (String) pageCache.retrieve(QUERY_LANGUAGE, QUERY_LANGUAGE);
    }

    public static boolean isOQLPage(PageCache pageCache) {
        return getQueryLanguage(pageCache).equalsIgnoreCase(QL_OQL);
    }

    public static boolean isHQLPage(PageCache pageCache) {
        return getQueryLanguage(pageCache).equalsIgnoreCase(QL_HQL);
    }

}