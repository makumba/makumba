package org.makumba.list.tags;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.apache.commons.collections.map.MultiValueMap;
import org.makumba.LogicException;
import org.makumba.ProgrammerError;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.forms.responder.Responder;
import org.makumba.list.engine.valuecomputer.ValueComputer;

import com.google.gson.Gson;

/**
 * mak:section tag, capable of rendering its content dynamically and reloading it via AJAX callbacks <br>
 * TODO support for multiple events: <mak:section reload="event1, event2"><br>
 * TODO special effects for show/hide/reload <br>
 * TODO detection of "toggle"/"update" situation (i.e. two sections next to one another that hide/show on the same
 * event)?<br>
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: SectionTag.java,v 1.1 Dec 25, 2009 6:47:43 PM manu Exp $
 */
public class SectionTag extends GenericMakumbaTag implements BodyTag {

    private static final String EXPR_SEPARATOR = "---";

    private static final long serialVersionUID = 1L;

    public static final String MAKUMBA_EVENT = "__mak_event__";

    private static final String SECTION_INIT = "__section_init__";

    private String name;

    private String showOn;

    private String hideOn;

    private String reloadOn;

    private String iterationExpr;

    private BodyContent bodyContent;

    Gson gson = new Gson();

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

    @Override
    public void doInitBody() throws JspException {
    }

    @Override
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
        name = reloadOn = showOn = hideOn = iterationExpr = null;
        bodyContent = null;
    }

    private boolean isInvoked = false;

    /**
     * Whether this section matches a specific event (only for 'reloadOn' and 'showOn' events)
     * 
     * @param event
     *            the name of the event that the JSP page is being called with
     * @param exprValue
     *            the expression value used to uniquely identify the section in multiple iterations of the list
     * @return <code>true</code> if this section is concerned by the called event, <code>false</code> otherwise
     */
    private boolean matches(String event, String exprValue) {
        boolean matches = false;

        if (event != null) {
            boolean invokesIteration = event.indexOf(EXPR_SEPARATOR) > -1;
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
        pageCache.cacheNeededResources(new String[] { "makumba.css", "prototype.js", "scriptaculous.js",
                "makumba-ajax.js" });

        // check if we are in a mak:list and if we want to uniquely identify sections of the list via an iterationLabel
        QueryTag parentList = getParentListTag();
        if (parentList != null && iterationExpr != null) {
            // add the iterationLabel to the projections if it's not there
            Object check = pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey);
            if (check == null) {
                ValueComputer vc = ValueComputer.getValueComputerAtAnalysis(false,
                    QueryTag.getParentListKey(this, pageCache), iterationExpr, pageCache);
                pageCache.cache(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey, vc);
            }
        }
    }

    static public String getEvent(PageContext pc) {
        String s = pc.getRequest().getParameter(MAKUMBA_EVENT);
        if (s == null) {
            s = (String) pc.getRequest().getAttribute(MAKUMBA_EVENT);
        }
        return s;
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        try {
            JspWriter out = getPageContext().getOut();

            // if we are inside of a list and an expression value is found that can be used to uniquely identify the
            // section
            // we cache the section resolution maps. otherwise, they're cached at analysis time
            QueryTag parentList = getParentListTag();
            String exprValue = getIterationExpressionValue(pageCache, parentList);

            writeJavascript(out, exprValue);

            // check if we are invoked, i.e. if an event has been "fired" that requires us to do stuff
            isInvoked = matches(getEvent(pageContext), exprValue);

            if (isInvoked && showOn != null) {

                return EVAL_BODY_BUFFERED;
            }

            if (!isInvoked) {

                out.print("<div id=\"" + getSectionID(exprValue) + "\""
                        + (showOn != null ? " style=\"display:none;\"" : "") + ">");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return EVAL_BODY_BUFFERED;
    }

    void writeJavascript(JspWriter out, String exprValue) throws IOException, ProgrammerError {
        String[] events = new String[] { showOn, hideOn, reloadOn };
        String[] eventAction = new String[] { "show", "hide", "reload" };

        out.println("<script type=\"text/javascript\">");
        if (getEvent(pageContext) == null && pageContext.getRequest().getAttribute(SECTION_INIT) == null) {
            pageContext.getRequest().setAttribute(SECTION_INIT, "x");
            out.println("var mak = new Mak();");
            out.println("var _mak_event_to_id_= $H();");
            out.println("var _mak_idevent_to_type_= $H();");

            HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
            String pagePath = req.getContextPath() + req.getServletPath();
            out.println("var _mak_page_url_ = '" + pagePath + "';\n");
            out.println("var _mak_page_params_ = " + getQueryParameters(req) + ";\n");
        }

        MultiValueMap ev2Id = new MultiValueMap();
        MultiValueMap idEvent2Type = new MultiValueMap();
        for (int i = 0; i < events.length; i++) {
            if (events[i] != null) {
                if (events[i].indexOf("___") > -1) {
                    throw new ProgrammerError("Invalid event name '" + events[i]
                            + "', '___' is not allowed in event names");
                }
                if (events[i].indexOf(EXPR_SEPARATOR) > -1) {
                    throw new ProgrammerError("Invalid event name '" + events[i]
                            + "', '---' is not allowed in event names");
                }
                idEvent2Type.put(getSectionID(exprValue) + "___" + events[i], eventAction[i]);
                ev2Id.put(events[i], getSectionID(exprValue));
            }
        }

        out.println("mak.merge(_mak_event_to_id_, $H(" + gson.toJson(ev2Id) + "));");
        out.println("mak.merge(_mak_idevent_to_type_, $H(" + gson.toJson(idEvent2Type) + "));");
        out.println("</script>");
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
                @SuppressWarnings("unchecked")
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
     * Gets the value of the MQL expression used to uniquely identify the section within a mak:list, or an automatically
     * generated one if no expression label is provided. Returns an empty string otherwise.
     */
    private String getIterationExpressionValue(PageCache pageCache, QueryTag parentList) {

        String exprValue = "";
        if (parentList != null && iterationExpr != null) {
            try {
                exprValue = EXPR_SEPARATOR
                        + ((ValueComputer) pageCache.retrieve(MakumbaJspAnalyzer.VALUE_COMPUTERS, tagKey)).getValue(
                            getPageContext()).toString();
            } catch (LogicException le) {
                le.printStackTrace();
            }
        } else if (parentList != null && iterationExpr == null) {
            exprValue = EXPR_SEPARATOR + parentList.getCurrentIterationNumber();
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
     * Serializes the page parameters. If there's a _mak_responder_ parameter indicating the submission of a form, we
     * don't return the parameters.<br>
     * TODO make this smarter, i.e. detect which parameters come from a form
     */
    private String getQueryParameters(HttpServletRequest req) {
        if (req.getParameter(Responder.responderName) != null) {
            return gson.toJson("");
        }
        return gson.toJson(req.getParameterMap());
    }

    private QueryTag getParentListTag() {
        return (QueryTag) findAncestorWithClass(this, QueryTag.class);
    }

}
