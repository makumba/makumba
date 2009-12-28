package org.makumba.list.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.apache.commons.collections.map.MultiValueMap;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.json.JSONObject;
import org.makumba.commons.tags.GenericMakumbaTag;

/**
 * mak:section tag, capable of rendering its content dynamically and reloading it via AJAX callbacks
 * 
 * TODO require needed resources
 * TODO enhance makumba-sections.js to use class instead of icon for wheel, and make a makumba.css containing the icon
 * TODO handle forms inside of section (submit via partial post-back?)
 * TODO support for multiple events: <mak:section reload="event1, event2">
 * TODO work inside of a list, multiple iterations
 * TODO special effects for show/hide/reload
 * TODO detection of "toggle"/"update" situation (i.e. two sections next to one another that hide/show on the same event)?
 * 
 * @author Manuel Gay
 * @version $Id: SectionTag.java,v 1.1 Dec 25, 2009 6:47:43 PM manu Exp $
 */
public class SectionTag extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    public static final String MAKUMBA_EVENT = "__mak_event__";

    private String name;

    private String show;

    private String hide;

    private String reload;

    private String[] actions;

    private String[] actionsType = new String[] { "show", "hide", "reload" };
    
    private BodyContent bodyContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public String getHide() {
        return hide;
    }

    public void setHide(String hide) {
        this.hide = hide;
    }

    public String getReload() {
        return reload;
    }

    public void setReload(String reload) {
        this.reload = reload;
    }

    @Override
    public boolean canHaveBody() {
        return true;
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        tagKey = getTagKey(pageCache);
    }

    private MultipleKey getTagKey(PageCache pageCache) {
        return new MultipleKey(new Object[] { getSectionID(pageCache) });
    }

    @Override
    public boolean allowsIdenticalKey() {
        // section names must be unique
        return false;
    }
    
    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        isFirstSection = isLastSection = false;
        name = reload = show = hide = null;
        actions = null;
        bodyContent = null;
    }


    @Override
    public void doStartAnalyze(PageCache pageCache) {

        // check if this section handles at least one event
        if (show == null && hide == null && reload == null) {
            throw new ProgrammerError(
                    "A mak:section must have at least a 'show', 'hide' or 'reload' attribute specified");
        }
        
        // check if this section does not handle the same event in two different ways
        if(show != null && hide != null && show.equals(hide)) {
            throw new ProgrammerError("Cannot show and hide a section for the same event");
        }
        if(reload != null && hide != null && reload.equals(hide)) {
            throw new ProgrammerError("Cannot reload and hide a section for the same event");
        }
        if(show != null && reload != null && show.equals(reload)) {
            throw new ProgrammerError("Cannot show and reload a section for the same event");
        }
        
        actions = new String[] { show, hide, reload };

        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null) {

                if (actions[i].indexOf("___") > -1) {
                    throw new ProgrammerError("Invalid event name '" + actions[i] + "', '___' is not allowed in event names");
                }

                // cache our invocation in the map event -> section id
                pageCache.cacheMultiple(MakumbaJspAnalyzer.SECTION_EVENT_TO_ID, actions[i], getSectionID(pageCache));

                // we can have several types of event per section depending on the event
                // we need a mapping (section id, event) -> type
                pageCache.cache(MakumbaJspAnalyzer.SECTION_IDEVENT_TO_TYPE, getSectionID(pageCache) + "___"
                        + actions[i], actionsType[i]);
            }
        }
    }

    @Override
    public void doEndAnalyze(PageCache pageCache) {

    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        initialize(pageCache);

        JspWriter out = getPageContext().getOut();
        try {

            // tell the controller handler to start recording the stream
            // with the ID of the section

            if(!isInvoked) {

                if (isFirstSection) {
                    // print the javascript that contains the data about sections to event mappings
                    out.print(getSectionDataScript(pageCache));
                }

                out.print("<div id=\"" + getSectionID(pageCache) + "\">");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws LogicException, JspException {
        JspWriter out = getPageContext().getOut();

        try {

            if (isInvoked) {
                
                String eventName = "";
                if(reload != null) {
                    eventName = reload;
                } else if(show != null) {
                    eventName = show;
                }

                // store all the stuff in a map in the request context
                Map<String, String> sectionData = (Map<String, String>) pageContext.getRequest().getAttribute(MAKUMBA_EVENT + "###" + eventName);
                if(sectionData == null) {
                    sectionData = new LinkedHashMap<String, String>();
                    pageContext.getRequest().setAttribute(MAKUMBA_EVENT + "###" + eventName, sectionData);
                }
                
                StringWriter sw = new StringWriter();
                bodyContent.writeOut(sw);
                sectionData.put(getSectionID(pageCache), sw.toString());

            } else {
                bodyContent.writeOut(bodyContent.getEnclosingWriter());
                out.print("</div>");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.doAnalyzedEndTag(pageCache);
    }

    /**
     * Generates the unique section ID based on the position of the section tag in the page TODO extend this to handle
     * list iterations
     */
    private String getSectionID(PageCache pageCache) {
        return name;
    }

    private boolean isInvoked = false;

    private boolean isFirstSection = false;

    private boolean isLastSection = false;

    private void initialize(PageCache pageCache) {

        setInvoked(pageCache);

        // iterate over all the tags to see where we are in the page
        // TODO think of implementing a convenience method in the page analysis of the sort "firstOfMyKind",
        // "lastOfMyKind"
        Map<Object, Object> tags = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
        Iterator<Object> tagDataIterator = tags.keySet().iterator();
        boolean firstTag = true;
        while (tagDataIterator.hasNext()) {
            MultipleKey key = (MultipleKey) tagDataIterator.next();

            if (key.equals(getTagKey(pageCache)) && firstTag) {
                isFirstSection = true;
            }
            if (key.equals(getTagKey(pageCache)) && !tagDataIterator.hasNext()) {
                isLastSection = true;
            }
            firstTag = false;
        }
    }
    
    private void setInvoked(PageCache pageCache) {

        // check if we are invoked, i.e. if an event has been "fired" that requires us to do stuff
        if (matches(getPageContext().getRequest().getParameter(MAKUMBA_EVENT))) {
            isInvoked = true;
        }
    }

    private boolean matches(String event) {
        boolean matches = false;
        if(reload != null) {
            matches = event != null && reload.equals(event);
        } else if(show != null) {
            matches = event != null && show.equals(event);
        }
        return matches;
    }

    private String getSectionDataScript(PageCache pageCache) {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        String pagePath = req.getContextPath() + "/" + req.getServletPath();

        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">\n");

        // we cache the section data for the page
        // if the JSP is modified, pageCache is discarded by the analyzer engine
        String eventToId = (String) pageCache.retrieve(MakumbaJspAnalyzer.SECTION_DATA, "event_to_id#" + pagePath);
        if (eventToId == null) {
            eventToId = getEventToId(pageCache);
            pageCache.cache(MakumbaJspAnalyzer.SECTION_DATA, "event_to_id#" + pagePath, eventToId);
        }
        String idEventToType = (String) pageCache.retrieve(MakumbaJspAnalyzer.SECTION_DATA, "idevent_to_type#"
                + pagePath);
        if (idEventToType == null) {
            idEventToType = getIdEventToType(pageCache);
            pageCache.cache(MakumbaJspAnalyzer.SECTION_DATA, "idevent_to_type#" + pagePath, idEventToType);
        }

        sb.append("var _mak_event_to_id_ = " + eventToId + ";\n");
        sb.append("var _mak_idevent_to_type_ = " + idEventToType + ";\n");
        sb.append("var _mak_page_url_ = '" + pagePath + "';\n");

        sb.append("var _mak_page_params_ = " + getQueryParameters(req) + ";\n");

        sb.append("</script>\n");

        return sb.toString();

    }

    private String getQueryParameters(HttpServletRequest req) {
        /*String query = "";
        for (Iterator<String> i = req.getParameterMap().keySet().iterator(); i.hasNext();) {
            String el = i.next();
            String[] vals = (String[]) req.getParameterMap().get(el);
            for (int k = 0; k < vals.length; k++) {
                query += el + ": '" + vals[k] + "'";
                if (k < vals.length - 1) {
                    query += ",";
                }
            }
            if (i.hasNext()) {
                query += ",";
            } else {
                query = "{" + query + "}";
            }
        }
        if (query.length() == 0)
            query = "new Hash()";

        return query;*/
        return new JSONObject(req.getParameterMap()).toString();
    }

    private String getIdEventToType(PageCache pageCache) {
        // {div1: 'reload', div2: 'show', div3: 'reload', div4: 'hide' }
        Map<Object, Object> idevent_to_type = pageCache.retrieveCache(MakumbaJspAnalyzer.SECTION_IDEVENT_TO_TYPE);
        return new JSONObject(idevent_to_type).toString();
    }

    private String getEventToId(PageCache pageCache) {
        MultiValueMap eventToId = pageCache.retrieveMultiCache(MakumbaJspAnalyzer.SECTION_EVENT_TO_ID);
        return new JSONObject(eventToId).toString();
    }

    public void doInitBody() throws JspException {
    }

    public void setBodyContent(BodyContent b) {
        this.bodyContent = b;
        
    }

}
