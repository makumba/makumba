package org.makumba.commons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.list.tags.QueryTag;

/**
 * Class used to store the status of the parser
 * 
 * @author Cristian Bogdan
 */
public class ParseStatus {


    public ParseStatus() {

    }

    String makumbaPrefix = new String("dummy_prefix"); // for old makumba and list

    String formPrefix = new String("dummy_prefix"); // for makumba forms

    String makumbaURI; // for old makumba and list

    String formMakumbaURI; // for makumba forms

    List<AnalysableTag> tags = new ArrayList<AnalysableTag>();

    List<AnalysableTag> parents = new ArrayList<AnalysableTag>();

    protected PageCache pageCache = new PageCache();
    
    GraphTS formGraph = new GraphTS();
    
    

    /**
     * Caches useful information for a tag in its TagData object and caches it in the pageCache. 
     * 
     * @param t
     *            the tag to be added
     * @param td
     *            the TagData where to which the tag should be added
     */
    void addTag(AnalysableTag t, TagData td) {
        if (!parents.isEmpty())
            t.setParent((AnalysableTag) parents.get(parents.size() - 1));
        else
            t.setParent(null);

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
        if(MakumbaJspAnalyzer.formTagNamesList.contains(getTagName(t.tagData.name))) {
            
            // fetch the parent
            if(t.getParent() instanceof AnalysableTag) {
                
                AnalysableTag parent = (AnalysableTag) t.getParent();
                
                // if the parent is a form tag
                // maybe not needed, but who knows
                if(MakumbaJspAnalyzer.formTagNamesList.contains(getTagName(parent.tagData.name))) {
                    
                    // we add this tag to the form graph
                    td.nodeNumber = formGraph.addVertex(t.getTagKey());
                    
                    // we also add the dependency
                    formGraph.addEdge(td.nodeNumber, parent.tagData.nodeNumber);
                    
                } else {
                    // we are a root form
                    // we simply add it
                    td.nodeNumber = formGraph.addVertex(t.getTagKey());
                }
                
            } else if(t.getParent() == null) {
                // this form tag has no parent
                // we simply add it
                td.nodeNumber = formGraph.addVertex(t.getTagKey());
            }
        }
        
        pageCache.cache(TagData.TAG_DATA_CACHE, t.getTagKey(), td);

        t.doStartAnalyze(pageCache);
        tags.add(t);
        
        
        
    }

    /**
     * Handles the start of a tag by adding it to the parent list
     * 
     * @param t
     *            the tag to be added
     */
    public void start(AnalysableTag t) {
        if (t == null)
            return;
        if (!(t instanceof BodyTag) && !t.canHaveBody())
            throw new ProgrammerError("This type of tag cannot have a body:\n " + t.getTagText());
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
        if (!tagName.startsWith(makumbaPrefix) && !tagName.startsWith(formPrefix))
            return;

        // checks if the tag was opened
        if (parents.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error: Closing tag never opened:\ntag \"").append(td.name).append("\" at line ");
            JspParseData.tagDataLine(td, sb);
            throw new org.makumba.ProgrammerError(sb.toString());
        }

        tagName = getTagName(tagName);

        AnalysableTag t = (AnalysableTag) parents.get(parents.size() - 1);

        // if the end and start of the tag are not the same kind of tag
        if (!t.getClass().equals(MakumbaJspAnalyzer.tagClasses.get(tagName))) {
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
     * Gets the short name of a tag, without the prefix
     * @param tagName the inital name of the tak, e.g. mak:newForm
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
        for (Iterator i = tags.iterator(); i.hasNext();) {
            AnalysableTag t = (AnalysableTag) i.next();
            AnalysableTag.analyzedTag.set(t.tagData);
            t.doEndAnalyze(pageCache);
            AnalysableTag.analyzedTag.set(null);
        }
        // additionally to the tags, we also store the dependency graph in the pageCache after sorting it
        formGraph.topo();            
        pageCache.cache(MakumbaJspAnalyzer.DEPENDENCY_CACHE, MakumbaJspAnalyzer.DEPENDENCY_CACHE, formGraph.getSortedKeys());
    }
}