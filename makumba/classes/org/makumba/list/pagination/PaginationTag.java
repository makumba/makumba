package org.makumba.list.pagination;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.ClassResource;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.Configuration;

/**
 * This class provides pagination navigation links, i.e. links to the first, previous, next and last page of a paginated
 * list. This tag can be used in the following three ways:
 * <ul>
 * <li>Inside a mak:list: no attribute needs to be specified</li>
 * <li>Outside a mak:list: specify the list using the 'forList' attribute</li>
 * <li>Not connected to a specific mak:list: specify limit, offset and totalCount attributes</li>
 * </ul>
 * 
 * @author Rudolf Mayer
 * @version $Id: PaginationTag.java,v 1.1 23.12.2007 21:41:04 Rudi Exp $
 */
public class PaginationTag extends GenericMakumbaTag {

    private static final String ALL_ATTRIBUTES = "all of the attributes 'limit', 'offset' and 'totalCount'";

    private static final String FIRST = "&lt;&lt;";

    private static final String NEXT = "&gt;";

    private static final String LAST = "&gt;&gt;";

    private static final String LIMIT = "limit";

    private static final String PREVIOUS = "&lt;";

    private static Hashtable<String, String> navigationLinkStyle = new Hashtable<String, String>(4);

    private static Hashtable<String, String> navigationNALinkStyle = new Hashtable<String, String>(4);

    private static boolean navigationStylesInitialised = false;

    private static final String OFFSET = "offset";

    private static final long serialVersionUID = 1L;

    private static final String PROPERTIES_FILE_NAME = "paginationProperties.properties";

    private String forList;

    private String itemName;

    private String limit;

    private String offset;

    private String styleClass;

    private String paginationLinkTitle;

    private String totalCount;

    private String action;

    private String paginationLinkTitleText;

    private boolean showPageTitle = true;

    public PaginationTag() {
        // TODO: get images from Makumba Config
    }

    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws LogicException, JspException {
        return EVAL_PAGE;
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        Logger.getLogger("org.makumba.list.pagination").fine(
            "Start analysing pagination tag, attributes: offset: " + offset + ", limit: " + limit + ", totalCount: "
                    + totalCount + ", action: " + action);
        // make sure we are inside a list
        final String[] values = new String[] { limit, offset, totalCount };
        boolean allNotEmpty = org.makumba.commons.StringUtils.allNotEmpty(values);
        boolean anyNotEmpty = org.makumba.commons.StringUtils.anyNotEmpty(values);
        if (anyNotEmpty && allNotEmpty) {
            throw new ProgrammerError("You must provide values for either " + ALL_ATTRIBUTES + ", or for none.");
        }
        if (getParentListTag() == null && getReferredListTag(pageCache) == null && !allNotEmpty) {
            throw new ProgrammerError(
                    "\'pagination\' tag must be enclosed in a 'list' tag, or the list specified with the 'forList' attribute, or "
                            + ALL_ATTRIBUTES + " must be specified!");
        }
        super.doStartAnalyze(pageCache);
    }

    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
    
        if (!navigationStylesInitialised) {
            try {
                initLinkStyle();
            } catch (IOException e) {
                throw new MakumbaError("Error reading properties for pagination: " + e.getMessage());
            }
        }

        int offset = this.offset != null ? Integer.parseInt(this.offset) : getParentListTag().getOffsetInt();
        int limit = this.limit != null ? Integer.parseInt(this.limit) : getParentListTag().getLimitInt();
        int maxResults = this.totalCount != null ? Integer.parseInt(this.totalCount) : QueryTag.maxResults();

        int pages = (int) Math.ceil(maxResults / (double) limit);

        if (maxResults < limit) { // no pagination needed
            pages = 0;
        }

        if (pages >= 1) {
            int currentIndex = (int) Math.ceil(offset / (double) limit); // current index
            boolean hasPreviousPage = currentIndex > 0 ? true : false;
            boolean hasNextPage = currentIndex < pages - 1 ? true : false;
            StringBuffer sb = new StringBuffer();
            String baseUrl = action != null ? action : getBaseURL();
            int itemCountLower = offset + 1;
            int itemCountUpper = Math.min(maxResults, (currentIndex + 1) * limit);

            // handle anchors in actions (bla.jsp?person=hg34bw#employment)
            String actionAnchor = "";
            final int actionHashPos = baseUrl.indexOf('#');
            if (actionHashPos > -1) {
                actionAnchor = baseUrl.substring(actionHashPos);
                baseUrl = baseUrl.substring(0, actionHashPos);
            }

            try {
                JspWriter out = pageContext.getOut();
                sb.append("<div class=\"" + styleClass + "\">\n");
                sb.append("  <div style=\"float: left; width: 50px\" >\n    ");
                if (hasPreviousPage) {
                    sb.append(getAnchor(baseUrl, actionAnchor, 0, limit, FIRST)).append("\n    ");
                    sb.append(getAnchor(baseUrl, actionAnchor, (currentIndex - 1) * limit, limit, PREVIOUS));
                } else {
                    sb.append(getLink(FIRST, navigationNALinkStyle)).append("\n    ");
                    sb.append(getLink(PREVIOUS, navigationNALinkStyle));
                }
                sb.append("\n  </div>\n");

                sb.append("  <div style=\"float: right; width: 50px\" align=\"right\">\n    ");
                if (hasNextPage) {
                    sb.append(getAnchor(baseUrl, actionAnchor, (currentIndex + 1) * limit, limit, NEXT)).append(
                        "\n    ");
                    sb.append(getAnchor(baseUrl, actionAnchor, (pages - 1) * limit, limit, LAST));
                } else {
                    sb.append(getLink(NEXT, navigationNALinkStyle)).append("\n    ");
                    sb.append(getLink(LAST, navigationNALinkStyle));
                }
                sb.append("  </div>\n");

                sb.append("  <div style=\"text-align: center; margin: 0 15px;\" align=\"center\">\n    ");
                if (showPageTitle) { // not show the message if it was deactivated by the user
                    sb.append("Showing ").append(itemName).append(" ").append(itemCountLower).append(" to ").append(
                        itemCountUpper).append("").append(" out of ").append(maxResults).append(" (Page ").append(
                        (currentIndex + 1)).append(" out of ").append(pages).append(")\n");
                }
                sb.append("  </div>\n");

                sb.append("  <div style=\"clear: both; font-size: 1px;\">&nbsp;</div>\n");
                sb.append("</div>\n");
                out.println(sb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Logger.getLogger("org.makumba.list.pagination").fine(
                "Pagination resulted in only one page, attributes are: offset: " + offset + ", limit: " + limit
                        + ", maxResults: " + maxResults + ", pages: " + pages);
        }
        return SKIP_BODY;
    }

    /** Initialise the link style, either reading from properties file or using the Makumba provided styles with images */
    private void initLinkStyle() throws IOException {
        String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
        navigationLinkStyle.put(FIRST, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_first.gif\" alt=\"" + FIRST + "\">");
        navigationLinkStyle.put(NEXT, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_next.gif\" alt=\"" + NEXT + "\">");
        navigationLinkStyle.put(LAST, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_last.gif\" alt=\"" + LAST + "\">");
        navigationLinkStyle.put(PREVIOUS, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_previous.gif\" alt=\"" + PREVIOUS
                + "\">");

        navigationNALinkStyle.put(FIRST, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_first_na.gif\">");
        navigationNALinkStyle.put(NEXT, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_next_na.gif\">");
        navigationNALinkStyle.put(LAST, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_last_na.gif\">");
        navigationNALinkStyle.put(PREVIOUS, "<img border=\"0\" src=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/image/resultset_previous_na.gif\">");

        Properties linkStyleProperties = new Properties();
        URL alternateLinkPropertiesURL = ClassResource.get(PROPERTIES_FILE_NAME);
        if (alternateLinkPropertiesURL != null) {
            Logger.getLogger("org.makumba.list.pagination").info(
                "Loading alternative properties for pagination links from "
                        + alternateLinkPropertiesURL.toExternalForm());
            linkStyleProperties.load((alternateLinkPropertiesURL).openConnection().getInputStream());
        }
        String[] s = { FIRST, NEXT, LAST, PREVIOUS };
        for (int i = 0; i < s.length; i++) {
            navigationLinkStyle.put(s[i], linkStyleProperties.getProperty(s[i], navigationLinkStyle.get(s[i])));
            navigationNALinkStyle.put(s[i], linkStyleProperties.getProperty(s[i] + "_NA",
                navigationNALinkStyle.get(s[i])));
        }

        navigationStylesInitialised = true;
    }

    private String getAnchor(String baseUrl, String actionAnchor, int start, int range, String page) {
        if (baseUrl.endsWith("?") || baseUrl.endsWith("&")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String sep = baseUrl.indexOf('?') >= 0 ? "&" : "?";
        StringBuffer link = new StringBuffer("<a href=\"").append(baseUrl).append(sep).append(OFFSET).append("=").append(
            start).append("&").append(LIMIT).append("=").append(range).append(actionAnchor).append("\"");
        if (StringUtils.equals(paginationLinkTitle, "true")) {
            link.append(" title=\"");
            if (page.equals(PREVIOUS)) {
                link.append("Previous " + paginationLinkTitleText);
            } else if (page.equals(NEXT)) {
                link.append("Next " + paginationLinkTitleText);
            } else if (page.equals(FIRST)) {
                link.append("First " + paginationLinkTitleText);
            } else if (page.equals(LAST)) {
                link.append("Last " + paginationLinkTitleText);
            } else {
                link.append("Go to " + paginationLinkTitleText + " " + page + "\">");
            }
            link.append("\"");
        }
        link.append(">").append(getLink(page, navigationLinkStyle)).append("</a>");
        return link.toString();
    }

    private String getLink(String page, Hashtable<String, String> images) {
        if (images.get(page) != null) {
            return images.get(page);
        } else {
            return page;
        }
    }

    private String getBaseURL() {
        HttpServletRequest r = ((HttpServletRequest) pageContext.getRequest());
        StringBuilder queryString = getQueryString(r.getParameterMap());
        StringBuilder url = new StringBuilder(r.getRequestURL().toString().substring(
            r.getRequestURL().toString().indexOf(r.getContextPath())));
        if (queryString.length() > 0) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    private QueryTag getParentListTag() {
        return (QueryTag) findAncestorWithClass(this, QueryTag.class);
    }

    public StringBuilder getQueryString(Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Object obj : map.keySet()) {
            if (!org.makumba.commons.StringUtils.equalsAny(obj, new String[] { LIMIT, OFFSET })) {
                String[] strings = (String[]) map.get(obj);
                for (int i = 0; i < strings.length; i++) {
                    if (StringUtils.isNotBlank(strings[i])) {
                        sb.append(obj).append("=").append(strings[i]).append("&");
                    }
                }
            }
        }
        return sb;
    }

    private AnalysableTag getReferredListTag(PageCache pageCache) {
        return getTagById(pageCache, forList, PaginationTag.class);
    }

    public void setForList(String forList) {
        this.forList = forList;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setLimit(String limit) throws JspException {
        onlyInt("limit", limit);
        this.limit = limit;
    }

    public void setOffset(String offset) throws JspException {
        onlyInt("offset", offset);
        this.offset = offset;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setShowPageTitle(String showPageTitle) {
        this.showPageTitle = StringUtils.equals(showPageTitle, "true");
    }

    public void setPaginationLinkTitle(String paginationLinkTitle) {
        this.paginationLinkTitle = paginationLinkTitle.trim();
    }

    public void setPaginationLinkTitleText(String paginationLinkTitleText) {
        this.paginationLinkTitleText = paginationLinkTitleText;
    }

    public void setTotalCount(String totalCount) throws JspException {
        onlyInt("totalCount", totalCount);
        this.totalCount = totalCount;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(new Object[] { action, getParentListTag() != null ? getParentListTag().tagKey : null,
                itemName, limit, offset, paginationLinkTitle, paginationLinkTitleText });
    }
    
    @Override
    public void initialiseState() {
        super.initialiseState();
        itemName = "Items";
        styleClass = "makumbaPagination";
        paginationLinkTitle = "true";
        paginationLinkTitleText = "page";
    }
    
    @Override
    protected void registerPossibleAttributeValues() {
        registerAttributeValues("showPageTitle", ATTRIBUTE_VALUES_TRUE_FALSE);
        registerAttributeValues("paginationLinkTitle", ATTRIBUTE_VALUES_TRUE_FALSE);
    }
    
    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        action = forList = itemName = limit = offset = paginationLinkTitle = paginationLinkTitleText = null;
    }

}
