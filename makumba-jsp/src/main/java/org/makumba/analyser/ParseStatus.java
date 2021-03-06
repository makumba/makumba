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

package org.makumba.analyser;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.ProgrammerError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.GraphTS;
import org.makumba.commons.MultipleKey;
import org.makumba.list.tags.QueryTag;

/**
 * Class used to store the status of the parser
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ParseStatus {

    public ParseStatus() {

    }

    String makumbaPrefix = new String("dummy_prefix"); // for old makumba and list

    String formPrefix = new String("dummy_prefix"); // for makumba forms

    String makumbaURI; // for old makumba and list

    String formMakumbaURI; // for makumba forms

    List<AnalysableElement> elements = new ArrayList<AnalysableElement>();

    List<AnalysableTag> parents = new ArrayList<AnalysableTag>();

    protected PageCache pageCache = new PageCache();

    GraphTS<MultipleKey> formGraph = new GraphTS<MultipleKey>();

    /**
     * Caches useful information for a tag in its TagData object and caches it in the pageCache.
     * 
     * @param t
     *            the tag to be added
     * @param td
     *            the TagData where to which the tag should be added
     */
    void addTag(AnalysableTag t, TagData td) {
        if (!parents.isEmpty()) {
            t.setParent(parents.get(parents.size() - 1));
        } else {
            t.setParent(null);
        }

        JspParseData.fill(t, td.attributes);
        t.setTagKey(pageCache);
        if (t.getTagKey() != null && !t.allowsIdenticalKey()) {
            AnalysableTag sameKey = (AnalysableTag) pageCache.retrieve(MakumbaJspAnalyzer.TAG_CACHE, t.getTagKey());
            if (sameKey != null) {
                StringBuffer sb = new StringBuffer();
                sb.append("Due to limitations of the JSP standard, Makumba cannot make\n").append(
                    "a difference between the following two tags: \n");
                sameKey.addTagText(sb);
                sb.append("\n");
                t.addTagText(sb);
                sb.append("\nTo address this, add an id= attribute to one of the tags, and make sure that id is unique within the page.");
                throw new ProgrammerError(sb.toString());
            }
            pageCache.cache(MakumbaJspAnalyzer.TAG_CACHE, t.getTagKey(), t);
            // added by rudi: to be able to find a list tag by the ID, we need to cache it twice
            // FIXME: this seems to be a dirty hack
            if (t instanceof QueryTag) {
                pageCache.cache(MakumbaJspAnalyzer.TAG_CACHE, t.getId(), t);
            }
        }

        // we also want to cache the dependencies between form tags
        if (MakumbaJspAnalyzer.formTagNamesList.contains(getTagName(t.tagData.name))) {

            // fetch the parent
            if (t.getParent() instanceof AnalysableTag) {

                AnalysableTag parent = (AnalysableTag) t.getParent();

                // if the parent is a form tag
                // maybe not needed, but who knows
                if (MakumbaJspAnalyzer.formTagNamesList.contains(getTagName(parent.tagData.name))) {

                    // we add this tag to the form graph
                    td.nodeNumber = formGraph.addVertex(t.getTagKey());

                    // we also add the dependency
                    formGraph.addEdge(td.nodeNumber, parent.tagData.nodeNumber);

                } else {
                    // we are a root form
                    // we simply add it
                    td.nodeNumber = formGraph.addVertex(t.getTagKey());
                }

            } else if (t.getParent() == null) {
                // this form tag has no parent
                // we simply add it
                td.nodeNumber = formGraph.addVertex(t.getTagKey());
            }
        }

        pageCache.cache(MakumbaJspAnalyzer.TAG_DATA_CACHE, t.getTagKey(), td);

        t.doStartAnalyze(pageCache);

        // check if the registered attribute values are correct
        t.checkAttributeValues();

        elements.add(t);
    }

    /**
     * Caches useful information for an EL expression in the page cache, for later re-use
     * 
     * @param e
     *            the {@link AnalysableExpression}
     * @param ed
     *            the {@link ELData}
     */
    void addExpression(AnalysableExpression e, ELData ed) {

        // what's important for a makumba EL expression is to know its parent tag
        if (!parents.isEmpty()) {
            e.setParent(parents.get(parents.size() - 1));
        } else {
            e.setParent(null);
        }

        e.setKey(pageCache);

        pageCache.cache(MakumbaJspAnalyzer.EL_CACHE, e.getKey(), e);
        pageCache.cache(MakumbaJspAnalyzer.EL_DATA_CACHE, e.getKey(), ed);

        e.analyze(pageCache);

        elements.add(e);

    }

    /**
     * Handles the start of a tag by adding it to the parent list
     * 
     * @param t
     *            the tag to be added
     */
    public void start(AnalysableTag t) {
        if (t == null) {
            return;
        }
        if (!(t instanceof BodyTag) && !t.canHaveBody()) {
            throw new ProgrammerError("This type of tag cannot have a body:\n " + t.getTagText());
        }
        parents.add(t);
    }

    /**
     * Handles the end of tags
     * 
     * @param td
     *            the TagData containing the information collected for the tag
     */
    public void end(TagData td) {
        String tagName = td.name;
        if (!tagName.startsWith(makumbaPrefix) && !tagName.startsWith(formPrefix)) {
            return;
        }

        // checks if the tag was opened
        // FIXME there is a bug with the login tag
        if (parents.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error: Closing tag never opened:\ntag \"").append(td.name).append("\" at line ");
            JspParseData.tagDataLine(td, sb);
            throw new org.makumba.ProgrammerError(sb.toString());
        }

        tagName = getTagName(tagName);

        AnalysableTag t = parents.get(parents.size() - 1);

        // if the end and start of the tag are not the same kind of tag
        if (!t.getClass().equals(MakumbaJspAnalyzer.tagClasses.get(tagName))) {
            StringBuffer sb = new StringBuffer();
            sb.append("Body tag nesting error:\ntag \"").append(td.name).append("\" at line ");
            JspParseData.tagDataLine(td, sb);

            sb.append("\n\ngot incorrect closing \"").append(td.name).append("\" at line ");
            JspParseData.tagDataLine(td, sb);

            throw new org.makumba.ProgrammerError(sb.toString());
        }

        TagData beginTagData = (TagData) pageCache.retrieve(MakumbaJspAnalyzer.TAG_DATA_CACHE, t.getTagKey());
        beginTagData.setClosingTagData(td);

        parents.remove(parents.size() - 1);
    }

    /**
     * Gets the short name of a tag, without the prefix
     * 
     * @param tagName
     *            the inital name of the tak, e.g. mak:newForm
     * @return the short version of the name, e.g. newForm
     */
    private String getTagName(String tagName) {
        if (tagName.startsWith(makumbaPrefix)) {
            tagName = tagName.substring(makumbaPrefix.length() + 1);
        } else if (tagName.startsWith(formPrefix)) {
            tagName = tagName.substring(formPrefix.length() + 1);
        }
        return tagName;
    }

    /**
     * Ends the analysis when the end of the page is reached.
     */
    public void endPage() {
        for (AnalysableElement analysableElement : elements) {
            if (analysableElement instanceof AnalysableTag) {
                AnalysableTag t = (AnalysableTag) analysableElement;
                AnalysableElement.setAnalyzedElementData(t.tagData);
                t.doEndAnalyze(pageCache);
                AnalysableElement.setAnalyzedElementData(null);
            } else if (analysableElement instanceof AnalysableExpression) {
                AnalysableExpression expr = (AnalysableExpression) analysableElement;
                AnalysableElement.setAnalyzedElementData(expr.getElementData());
                expr.doEndAnalyze(pageCache);
                AnalysableElement.setAnalyzedElementData(null);
            }
        }
        // additionally to the tags, we also store the dependency graph in the pageCache after sorting it
        formGraph.topo();
        pageCache.cache(MakumbaJspAnalyzer.FORM_TAGS_DEPENDENCY_CACHE, MakumbaJspAnalyzer.FORM_TAGS_DEPENDENCY_CACHE,
            formGraph.getSortedKeys());
    }
}