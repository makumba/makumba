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

package org.makumba.view.jsptaglib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.util.JspParseData;
import org.makumba.util.MultipleKey;
import org.makumba.view.ComposedQuery;
import org.makumba.view.ComposedSubquery;

/**
 * This class analyzes a JSP taking into account the specifics of Makumba tags.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class MakumbaJspAnalyzer implements JspParseData.JspAnalyzer {
    static String[] tags = { "value", "org.makumba.view.jsptaglib.ValueTag", "list",
            "org.makumba.view.jsptaglib.QueryTag", "object", "org.makumba.view.jsptaglib.ObjectTag", "form",
            "org.makumba.view.jsptaglib.FormTagBase", "newForm", "org.makumba.view.jsptaglib.NewTag", "addForm",
            "org.makumba.view.jsptaglib.AddTag", "editForm", "org.makumba.view.jsptaglib.EditTag", "deleteLink",
            "org.makumba.view.jsptaglib.DeleteTag", "delete", "org.makumba.view.jsptaglib.DeleteTag", "input",
            "org.makumba.view.jsptaglib.InputTag", "action", "org.makumba.view.jsptaglib.ActionTag", "option",
            "org.makumba.view.jsptaglib.OptionTag", "if", "org.makumba.view.jsptaglib.IfTag" };

    static final Map tagClasses = new HashMap();

    /**
     * Puts the Makumba tags into a Map
     */
    static {
        for (int i = 0; i < tags.length; i += 2)
            try {
                tagClasses.put(tags[i], Class.forName(tags[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }

    /**
     * Class used to store the types included in Makumba tags detected during analysis of a page
     * 
     * @author Cristian Bogdan
     */
    class Types extends HashMap {
        private static final long serialVersionUID = 1L;

        /**
         * Sets the type identified by the key of a tag
         * 
         * @param key
         *            the key of the tag
         * @param value
         *            the field definition containing the type
         * @param t
         *            the MakumbaTag
         */
        public void setType(String key, FieldDefinition value, MakumbaTag t) {
            Object[] val1 = (Object[]) get(key);
            FieldDefinition fd = null;

            if (val1 != null)
                fd = (FieldDefinition) val1[0];
            // if we get nil here, we keep the previous, richer type information
            if (fd != null && value.getType().equals("nil"))
                return;

            MakumbaTag.analyzedTag.set(t.tagData);
            if (fd != null && !value.isAssignableFrom(fd))
                throw new ProgrammerError("Attribute type changing within the page: in tag\n"
                        + ((MakumbaTag) val1[1]).getTagText() + " attribute " + key + " was determined to have type "
                        + fd + " and the from this tag results the incompatible type " + value);
            MakumbaTag.analyzedTag.set(null);

            Object[] val2 = { value, t };
            put(key, val2);
        }
    }

    /**
     * Cache for the page analysis
     * 
     * @author Cristian Bogdan
     * 
     */
    public class PageCache {
        HashMap formatters = new HashMap();

        HashMap valueComputers = new HashMap();

        Types types = new Types();

        HashMap queries = new HashMap();

        HashMap inputTypes = new HashMap();

        HashMap basePointerTypes = new HashMap();

        HashMap tags = new HashMap();

        HashMap tagData = new HashMap();

        boolean usesHQL = false;

        /**
         * Gets the query for a given key
         * 
         * @param key
         *            the key of the tag for which we want to get a query
         * @return The OQL query corresponding to this tag
         */
        public ComposedQuery getQuery(MultipleKey key) {
            ComposedQuery ret = (ComposedQuery) queries.get(key);
            if (ret == null)
                throw new MakumbaError("unknown query for key " + key);
            return ret;
        }

        /**
         * Gets a composed query from the cache, and if none is found, creates one and caches it.
         * 
         * @param key
         *            the key of the tag
         * @param sections
         *            the sections needed to compose a query
         * @param parentKey
         *            the key of the parent tag, if any
         */
        public ComposedQuery cacheQuery(MultipleKey key, String[] sections, MultipleKey parentKey) {
            ComposedQuery ret = (ComposedQuery) queries.get(key);
            if (ret != null)
                return ret;
            ret = parentKey == null ? new ComposedQuery(sections, usesHQL) : new ComposedSubquery(sections,
                    getQuery(parentKey), usesHQL);

            ret.init();
            queries.put(key, ret);
            return ret;
        }

    }

    /**
     * Class used to store the status of the parser
     * 
     * @author Cristian Bogdan
     */
    class ParseStatus {
        String makumbaPrefix;

        String makumbaURI;

        List tags = new ArrayList();

        List parents = new ArrayList();

        PageCache pageCache = new PageCache();

        /**
         * Adds a tag to the current tagData
         * 
         * @param t
         *            the tag to be added
         * @param td
         *            the TagData where to which the tag should be added
         */
        void addTag(MakumbaTag t, JspParseData.TagData td) {
            if (!parents.isEmpty())
                t.setParent((MakumbaTag) parents.get(parents.size() - 1));
            else
                t.setParent(null);

            JspParseData.fill(t, td.attributes);
            t.setTagKey(pageCache);
            if (t.getTagKey() != null && !t.allowsIdenticalKey()) {
                MakumbaTag sameKey = (MakumbaTag) pageCache.tags.get(t.getTagKey());
                if (sameKey != null) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("Due to limitations of the JSP standard, Makumba cannot make\n").append(
                            "a difference between the following two tags: \n");
                    sameKey.addTagText(sb);
                    sb.append("\n");
                    t.addTagText(sb);
                    sb
                            .append("\nTo address this, add an id= attribute to one of the tags, and make sure that id is unique within the page.");
                    throw new ProgrammerError(sb.toString());
                }
                pageCache.tags.put(t.getTagKey(), t);
            }
            pageCache.tagData.put(t.getTagKey(), td);
            t.doStartAnalyze(pageCache);
            tags.add(t);
        }

        /**
         * Handles the start of a tag by adding it to the parent list
         * 
         * @param t
         *            the tag to be added
         */
        public void start(MakumbaTag t) {
            if (t == null)
                return;
            if (!(t instanceof BodyTag) && !(t instanceof QueryTag))
                throw new ProgrammerError("This type of tag cannot have a body:\n " + t.getTagText());
            parents.add(t);
        }

        /**
         * Handles the end of tags
         * 
         * @param td
         *            the TagData containing the information collected for the tag
         */
        public void end(JspParseData.TagData td) {
            String tagName = td.name;
            if (!tagName.startsWith(makumbaPrefix))
                return;

            // checks if the tag was opened
            if (parents.isEmpty()) {
                StringBuffer sb = new StringBuffer();
                sb.append("Error: Closing tag never opened:\ntag \"").append(td.name).append("\" at line ");
                JspParseData.tagDataLine(td, sb);
                throw new org.makumba.ProgrammerError(sb.toString());
            }

            tagName = tagName.substring(makumbaPrefix.length() + 1);
            MakumbaTag t = (MakumbaTag) parents.get(parents.size() - 1);

            // if the end and start of the tag are not the same kind of tag
            if (!t.getClass().equals(tagClasses.get(tagName))) {
                StringBuffer sb = new StringBuffer();
                sb.append("Body tag nesting error:\ntag \"").append(td.name).append("\" at line ");
                JspParseData.tagDataLine(td, sb);

                sb.append("\n\ngot incorrect closing \"").append(td.name).append("\" at line ");
                JspParseData.tagDataLine(td, sb);

                throw new org.makumba.ProgrammerError(sb.toString());
            }

            parents.remove(parents.size() - 1);
        }

        /**
         * Ends the analysis when the end of the page is reached.
         */
        public void endPage() {
            for (Iterator i = tags.iterator(); i.hasNext();) {
                MakumbaTag t = (MakumbaTag) i.next();
                MakumbaTag.analyzedTag.set(t.tagData);
                t.doEndAnalyze(pageCache);
                MakumbaTag.analyzedTag.set(null);
            }
        }
    }

    /**
     * SingletonHolder, to make sure there's only one instance for the MakumbaJspAnalyzer
     * 
     * @author Cristian Bogdan
     */
    private static final class SingletonHolder {
        static final JspParseData.JspAnalyzer singleton = new MakumbaJspAnalyzer();
    }

    private MakumbaJspAnalyzer() {
    }

    public static JspParseData.JspAnalyzer getInstance() {
        return SingletonHolder.singleton;
    }

    /**
     * Performs analysis for a system tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void systemTag(JspParseData.TagData td, Object status) {
        if (td.name.equals("taglib")) {
            // JSP 2.0 introduced taglib directive with no uri: <%@taglib tagdir="/WEB-INF/tags" prefix="tags" %>
            if (td.attributes.get("uri") != null
                    && td.attributes.get("uri").toString().startsWith("http://www.makumba.org/")) {
                ((ParseStatus) status).makumbaPrefix = (String) td.attributes.get("prefix");
                ((ParseStatus) status).makumbaURI = (String) td.attributes.get("uri");
                if (((ParseStatus) status).makumbaURI.equals("http://www.makumba.org/presentation")) {
                    ((ParseStatus) status).pageCache.usesHQL = false;
                } else { // every other makumba.org tag-lib is treated to be hibernate
                    ((ParseStatus) status).pageCache.usesHQL = true;
                }
            }
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
    public void simpleTag(JspParseData.TagData td, Object status) {
        String prefix = ((ParseStatus) status).makumbaPrefix + ":";
        if (!td.name.startsWith(prefix))
            return;
        Class c = (Class) tagClasses.get(td.name.substring(prefix.length()));
        if (c == null)
            return;
        MakumbaTag.analyzedTag.set(td);
        MakumbaTag t = null;
        try {
            t = (MakumbaTag) c.newInstance();
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        td.tagObject = t;
        t.setTagDataAtAnalysis(td);
        ((ParseStatus) status).addTag(t, td);
        MakumbaTag.analyzedTag.set(null);
    }

    /**
     * Performs analysis for the start of a tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void startTag(JspParseData.TagData td, Object status) {
        simpleTag(td, status);
        ((ParseStatus) status).start((MakumbaTag) td.tagObject);
    }

    /**
     * Performs analysis for the end of a tag
     * 
     * @param td
     *            the TagData holding the information
     * @param status
     *            the status of the parsing
     */
    public void endTag(JspParseData.TagData td, Object status) {
        MakumbaTag.analyzedTag.set(td);
        ((ParseStatus) status).end(td);
        MakumbaTag.analyzedTag.set(null);
    }

    public Object makeStatusHolder(Object initialStatus) {
        return new ParseStatus();
    }

    public Object endPage(Object status) {
        ((ParseStatus) status).endPage();
        return ((ParseStatus) status).pageCache;
    }
}
