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
import org.apache.commons.lang.StringUtils;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.json.JSONObject;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.list.engine.valuecomputer.ValueComputer;

/**
 * mak:section tag, capable of rendering its content dynamically and reloading it via AJAX callbacks <br>
 * TODO enhance makumba-ajax.js to use class instead of icon for wheel, and make a makumba.css containing the icon<br>
 * TODO handle forms inside of section (submit via partial post-back?)<br>
 * TODO support for multiple events: <mak:section reload="event1, event2"><br>
 * TODO special effects for show/hide/reload <br>
 * TODO detection of "toggle"/"update" situation (i.e. two sections next to one another that hide/show on the same
 * event)?<br>
 * 
 * @author Manuel Gay
 * @version $Id: SectionTag.java,v 1.1 Dec 25, 2009 6:47:43 PM manu Exp $
 */
public class SectionTag extends GenericMakumbaTag implements BodyTag {

    private static final long serialVersionUID = 1L;

    public static final String MAKUMBA_EVENT = "__mak_event__";

    private String name;

    private String showOn;

    private String hideOn;

    private String reloadOn;

    private String iterationExpr;

    private String[] events;

    private String[] eventAction = new String[] { "show", "hide", "reload" };

    private BodyContent bodyContent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowOn() {
        return showOn;
    }

    public void setShowOn(String show) {
        this.showOn = show;
    }

    public String getHideOn() {
        return hideOn;
    }

    public void setHideOn(String hide) {
        this.hideOn = hide;
    }

    public String getReloadOn() {
        return reloadOn;
    }

    public void setReloadOn(String reload) {
        this.reloadOn = reload;
    }

    public String getIterationExpr() {
        return iterationExpr;
    }

    public void setIterationExpr(String iterationExpression) {
        this.iterationExpr = iterationExpression;
    }

    public void doInitBody() throws JspException {
    }

    public void setBodyContent(BodyContent b) {
        this.bodyContent = b;
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
        return new MultipleKey(new Object[] { getSectionID(null), iterationExpr });
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
        name = reloadOn = showOn = hideOn = iterationExpr = null;
        events = null;
        bodyContent = null;
    }

    private boolean isInvoked = false;

    private boolean isFirstSection = false;

    private boolean isLastSection = false;

    /**
     * Initializes various runtime variables necessary for the tag to work
     */
    private void initialize(PageCache pageCache) {

        // iterate over all the tags to see where we are in the page
        // TODO think of implementing a convenience method in the page analysis of the sort "firstOfMyKind",
        // "lastOfMyKind"
        Map<Object, Object> tags = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
        Iterator<Object> tagDataIterator = tags.keySet().iterator();
        boolean firstTag = true;
        MultipleKey lastSection = null;
        while (tagDataIterator.hasNext()) {
            MultipleKey key = (MultipleKey) tagDataIterator.next();
            Object o = ((TagData) tags.get(key)).getTagObject();
            if (!(o instanceof SectionTag)) {
                continue;
            }

            if (key.equals(getTagKey(pageCache)) && firstTag) {
                isFirstSection = true;
            }
            lastSection = key;

            firstTag = false;
        }
        if (lastSection.equals(getTagKey(pageCache))) {
            isLastSection = true;
        }
    }
    private boolean matches(String event, String exprValue) {
        boolean matches = false;

        if (event != null) {
            boolean invokesIteration = event.indexOf("---") > -1;
            if (reloadOn != null) {

                if (invokesIteration) {
                    matches = (reloadOn + exprValue).equals(event);
                } else {
                    matches = (reloadOn + exprValue).startsWith(event);
                }
            } else if (showOn != null) {
                if (invokesIteration) {
                    matches = (showOn + exprValue).equals(event);
                } else {
                    matches = (showOn + exprValue).startsWith(event);
                }
            }
        }

        return matches;
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {

        events = new String[] { showOn, hideOn, reloadOn };

        // check if this section handles at least one event
        if (showOn == null && hideOn == null && reloadOn == null) {
            throw new ProgrammerError(
                    "A mak:section must have at least a 'show', 'hide' or 'reload' attribute specified");
        }

        // check if this section does not handle the same event in two different ways
        if (showOn != null && hideOn != null && showOn.equals(hideOn)) {
            throw new ProgrammerError("Cannot show and hide a section for the same event");
        }
        if (reloadOn != null && hideOn != null && reloadOn.equals(hideOn)) {
            throw new ProgrammerError("Cannot reload and hide a section for the same event");
        }
        if (showOn != null && reloadOn != null && showOn.equals(reloadOn)) {
            throw new ProgrammerError("Cannot show and reload a section for the same event");
        }

        // request needed resources
        pageCache.cacheSetValues(NEEDED_RESOURCES, new String[] { "makumba.css", "prototype.js", "scriptaculous.js",
                "makumba-ajax.js" });

        // check if we are in a mak:list and if we want to uniquely identify sections of the list via an iterationLabel
        QueryTag parentList = getParentListTag();
        if (parentList != null && iterationExpr != null) {
            // add the iterationLabel to the projections if it's not there
            Object check = pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey);
            if (check == null) {
                ValueComputer vc = ValueComputer.getValueComputerAtAnalysis(this, QueryTag.getParentListKey(this,
                    pageCache), iterationExpr, pageCache);
                pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey, vc);
            }
        } else if (parentList == null) {
            cacheSectionResolution(pageCache, "", false);
        }
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        initialize(pageCache);

        // if we are inside of a list and an expression value is found that can be used to uniquely identify the section
        // we cache the section resolution maps. otherwise, they're cached at analysis time
        QueryTag parentList = getParentListTag();
        String exprValue = getIterationExpressionValue(pageCache, parentList);

        // check if we are invoked, i.e. if an event has been "fired" that requires us to do stuff
        if (matches(getPageContext().getRequest().getParameter(MAKUMBA_EVENT), exprValue)) {
            isInvoked = true;
        } else {
            isInvoked = false;
        }

        events = new String[] { showOn, hideOn, reloadOn };

        if (exprValue.length() > 0) {
            cacheSectionResolution(pageCache, exprValue, parentList != null && iterationExpr != null);
        }

        JspWriter out = getPageContext().getOut();
        try {

            if (!isInvoked) {

                // we print the section data only in the end, after all the iterations have been done
                if (isLastSection) {

                    if (parentList != null
                            && parentList.getCurrentIterationNumber() == parentList.getNumberOfIterations()) {
                        // print the javascript that contains the data about sections to event mappings
                        out.print(getSectionDataScript(pageCache));
                    } else if (parentList == null) {
                        out.print(getSectionDataScript(pageCache));
                    }
                }

                out.print("<div id=\"" + getSectionID(exprValue) + "\""
                        + (showOn != null ? " style=\"display:none;\"" : "") + ">");
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

                QueryTag parentList = getParentListTag();
                String exprValue = getIterationExpressionValue(pageCache, parentList);

                String eventName = "";
                if (reloadOn != null) {
                    eventName = reloadOn;
                } else if (showOn != null) {
                    eventName = showOn;
                }

                // store all the stuff in a map in the request context
                Map<String, String> sectionData = (Map<String, String>) pageContext.getRequest().getAttribute(
                    MAKUMBA_EVENT + "###" + eventName + exprValue);
                if (sectionData == null) {
                    sectionData = new LinkedHashMap<String, String>();
                    pageContext.getRequest().setAttribute(MAKUMBA_EVENT + "###" + eventName + exprValue, sectionData);
                }

                StringWriter sw = new StringWriter();
                bodyContent.writeOut(sw);
                sectionData.put(getSectionID(exprValue), sw.toString());

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
     * Caches the section resolution:
     * <ul>
     * <li>event -> section ID</li>
     * <li>(event, section ID) -> action type (hide, show, reload)</li>
     * </ul>
     */
    private void cacheSectionResolution(PageCache pageCache, String exprValue, boolean iterationIdentifiable)
            throws ProgrammerError {

        for (int i = 0; i < events.length; i++) {
            if (events[i] != null) {

                if (events[i].indexOf("___") > -1) {
                    throw new ProgrammerError("Invalid event name '" + events[i]
                            + "', '___' is not allowed in event names");
                }
                if (events[i].indexOf("---") > -1) {
                    throw new ProgrammerError("Invalid event name '" + events[i]
                            + "', '---' is not allowed in event names");
                }

                // cache our invocation in the map event -> section id
                pageCache.cacheMultiple(MakumbaJspAnalyzer.SECTION_EVENT_TO_ID, events[i], getSectionID(exprValue));

                // we can have several types of event per section depending on the event
                // we need a mapping (section id, event) -> type
                pageCache.cache(MakumbaJspAnalyzer.SECTION_IDEVENT_TO_TYPE,
                    getSectionID(exprValue) + "___" + events[i], eventAction[i]);

                if (iterationIdentifiable && !StringUtils.isEmpty(exprValue)) {
                    pageCache.cacheMultiple(MakumbaJspAnalyzer.SECTION_EVENT_TO_ID, events[i] + exprValue,
                        getSectionID(exprValue));
                    pageCache.cache(MakumbaJspAnalyzer.SECTION_IDEVENT_TO_TYPE, getSectionID(exprValue) + "___"
                            + events[i] + exprValue, eventAction[i]);
                }
            }
        }
    }

    /**
     * Gets the value of the MQL expression used to uniquely identify the section within a mak:list, or an automatically
     * generated one if no expression label is provided. Returns an empty string otherwise.
     */
    private String getIterationExpressionValue(PageCache pageCache, QueryTag parentList) {

        String exprValue = "";
        if (parentList != null && iterationExpr != null) {
            try {
                exprValue = "---"
                        + ((ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey)).getValue(
                            getPageContext()).toString();
            } catch (LogicException le) {
                le.printStackTrace();
            }
        } else if (parentList != null && iterationExpr == null) {
            exprValue = "---" + parentList.getCurrentIterationNumber();
        }
        return exprValue;

    }

    /**
     * Gets the unique section ID
     */
    private String getSectionID(String iterationIdentifier) {
        return name + iterationIdentifier;
    }

    /**
     * Generates the javascript containing the data (arrays) with section information
     */
    private String getSectionDataScript(PageCache pageCache) {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        String pagePath = req.getContextPath() + req.getServletPath();

        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">\n");

        // FIXME we cannot cache the section data because if the data changes, then we would have to discard the cache
        // which is not done yet because there is no mechanism for that
        String eventToId = getEventToId(pageCache);
        /*
         * String eventToId = (String) pageCache.retrieve(MakumbaJspAnalyzer.SECTION_DATA, "event_to_id#" + pagePath);
         * if (eventToId == null) { eventToId = getEventToId(pageCache);
         * pageCache.cache(MakumbaJspAnalyzer.SECTION_DATA, "event_to_id#" + pagePath, eventToId); }
         */
        String idEventToType = getIdEventToType(pageCache);
        /*
         * String idEventToType = (String) pageCache.retrieve(MakumbaJspAnalyzer.SECTION_DATA, "idevent_to_type#" +
         * pagePath); if (idEventToType == null) { idEventToType = getIdEventToType(pageCache);
         * pageCache.cache(MakumbaJspAnalyzer.SECTION_DATA, "idevent_to_type#" + pagePath, idEventToType); }
         */
        sb.append("var mak = new Mak();\n");
        sb.append("var _mak_event_to_id_ = " + eventToId + ";\n");
        sb.append("var _mak_idevent_to_type_ = " + idEventToType + ";\n");
        sb.append("var _mak_page_url_ = '" + pagePath + "';\n");
        sb.append("var _mak_page_params_ = " + getQueryParameters(req) + ";\n");
        sb.append("</script>\n");

        return sb.toString();

    }

    private String getQueryParameters(HttpServletRequest req) {
        /*
         * String query = ""; for (Iterator<String> i = req.getParameterMap().keySet().iterator(); i.hasNext();) {
         * String el = i.next(); String[] vals = (String[]) req.getParameterMap().get(el); for (int k = 0; k <
         * vals.length; k++) { query += el + ": '" + vals[k] + "'"; if (k < vals.length - 1) { query += ","; } } if
         * (i.hasNext()) { query += ","; } else { query = "{" + query + "}"; } } if (query.length() == 0) query =
         * "new Hash()"; return query;
         */
        return new JSONObject(req.getParameterMap()).toString();
    }

    // {div1: 'reload', div2: 'show', div3: 'reload', div4: 'hide' }
    private String getIdEventToType(PageCache pageCache) {
        Map<Object, Object> idevent_to_type = pageCache.retrieveCache(MakumbaJspAnalyzer.SECTION_IDEVENT_TO_TYPE);
        return new JSONObject(idevent_to_type).toString();
    }

    private String getEventToId(PageCache pageCache) {
        MultiValueMap eventToId = pageCache.retrieveMultiCache(MakumbaJspAnalyzer.SECTION_EVENT_TO_ID);
        return new JSONObject(eventToId).toString();
    }

    private QueryTag getParentListTag() {
        return (QueryTag) findAncestorWithClass(this, QueryTag.class);
    }

}
