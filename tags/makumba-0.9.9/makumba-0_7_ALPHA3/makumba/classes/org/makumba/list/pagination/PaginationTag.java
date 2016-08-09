package org.makumba.list.pagination;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.ClassResource;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.tags.GenericMakumbaTag;
import org.makumba.list.tags.QueryTag;

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
 * @version $Id: PaginationTag.java,v 1.1 23.12.2007 21:41:04 Rudif Exp $
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

    private String itemName = "Items";

    private String limit;

    private String offset;

    private String styleClass = "makumbaPagination";

    private String title;

    private String totalCount;

    public PaginationTag() {
        // TODO: implement reading navigation images from a config (file)
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    @Override
    public void doStartAnalyze(PageCache pageCache) {
        // make sure we are inside a list
        boolean allNotEmpty = org.makumba.commons.StringUtils.allNotEmpty(new String[] { limit, offset, totalCount });
        boolean anyNotEmpty = org.makumba.commons.StringUtils.anyNotEmpty(new String[] { limit, offset, totalCount });
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

    public int doStartTag() {

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
        int pages = (maxResults / limit);
        pages = maxResults % limit == 0 ? pages : pages + 1;
        if (pages >= 1) {
            int currentIndex = (offset / limit); // current index
            boolean hasPreviousPage = currentIndex > 0 ? true : false;
            boolean hasNextPage = currentIndex < pages - 1 ? true : false;
            StringBuffer sb = new StringBuffer();
            String baseUrl = getBaseURL();

            try {
                JspWriter out = pageContext.getOut();
                sb.append("<div class=\"" + styleClass + "\">\n");
                sb.append("  <div style=\"float: left; width: 23%;\" >\n    ");
                if (hasPreviousPage) {
                    sb.append(getAnchor(baseUrl, 0, limit, FIRST)).append("\n    ");
                    sb.append(getAnchor(baseUrl, (currentIndex - 1) * limit, limit, PREVIOUS));
                } else {
                    sb.append(getLink(FIRST, navigationNALinkStyle)).append("\n    ");
                    sb.append(getLink(PREVIOUS, navigationNALinkStyle));
                }
                sb.append("\n  </div>\n");

                sb.append("  <div style=\"float: left; width: 50%; margin: 0 15px;\" align=\"center\">\n    ");

                int itemCountLower = (currentIndex * limit) + 1;
                int itemCountUpper = Math.min(maxResults, (currentIndex + 1 * limit));
                sb.append("Showing ").append(itemName).append(" ").append(itemCountLower).append(" to ").append(
                    itemCountUpper).append("").append(" out of ").append(maxResults).append(" (Page ").append(
                    (currentIndex + 1)).append(" out of ").append(pages).append(")\n");
                sb.append("  </div>\n");

                sb.append("  <div style=\"float: right; width: 23%;\" align=\"right\">\n    ");
                if (hasNextPage) {
                    sb.append(getAnchor(baseUrl, (currentIndex + 1) * limit, limit, NEXT)).append("\n    ");
                    sb.append(getAnchor(baseUrl, (pages - 1) * limit, limit, LAST));
                } else {
                    sb.append(getLink(NEXT, navigationNALinkStyle)).append("\n    ");
                    sb.append(getLink(LAST, navigationNALinkStyle));
                }
                sb.append("  </div>\n");
                sb.append("  <div style=\"clear: both; font-size: 1px;\">&nbsp;</div>\n");
                sb.append("</div>\n");
                out.println(sb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SKIP_BODY;
    }

    /** Initialise the link style, either reading from properties file or using the Makumba provided styles with images */
    private void initLinkStyle() throws IOException {
        String contextPath = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
        navigationLinkStyle.put(FIRST, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_first.gif\" alt=\"" + FIRST + "\">");
        navigationLinkStyle.put(NEXT, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_next.gif\" alt=\"" + NEXT + "\">");
        navigationLinkStyle.put(LAST, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_last.gif\" alt=\"" + LAST + "\">");
        navigationLinkStyle.put(PREVIOUS, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_previous.gif\" alt=\"" + PREVIOUS + "\">");

        navigationNALinkStyle.put(FIRST, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_first_na.gif\"");
        navigationNALinkStyle.put(NEXT, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_next_na.gif\"");
        navigationNALinkStyle.put(LAST, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_last_na.gif\"");
        navigationNALinkStyle.put(PREVIOUS, "<img border=\"0\" src=\"" + contextPath
                + "/makumbaResources/image/resultset_previous_na.gif\"");

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

    private String getAnchor(String baseUrl, int start, int range, String page) {
        StringBuffer link = new StringBuffer("<a href=\"").append(baseUrl).append(OFFSET).append("=").append(start).append(
            "&").append(LIMIT).append("=").append(range).append("\"");
        if (StringUtils.equals(title, "true")) {
            link.append(" title=\"");
            if (page.equals(PREVIOUS)) {
                link.append("Previous page");
            } else if (page.equals(NEXT)) {
                link.append("Next page");
            } else if (page.equals(FIRST)) {
                link.append("First page");
            } else if (page.equals(LAST)) {
                link.append("Last page");
            } else {
                link.append("Go to page " + page + "\">");
            }
            link.append("\"");
        }
        link.append(">").append(getLink(page, navigationLinkStyle)).append("</a>");
        return link.toString();
    }

    private String getLink(String page, Hashtable<String, String> images) {
        if (navigationLinkStyle.get(page) != null) {
            return navigationLinkStyle.get(page);
        } else {
            return page;
        }
    }

    private String getBaseURL() {
        HttpServletRequest r = ((HttpServletRequest) pageContext.getRequest());
        String queryString = this.getQueryString(r.getParameterMap());
        StringBuffer url = new StringBuffer(r.getRequestURL().toString().substring(
            r.getRequestURL().toString().indexOf(r.getContextPath())));
        url.append("?").append(queryString);
        return url.toString();
    }

    private QueryTag getParentListTag() {
        return (QueryTag) findAncestorWithClass(this, QueryTag.class);
    }

    public String getQueryString(Map<Object, Object> map) {
        if (map == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
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
        return sb.toString();
    }

    private AnalysableTag getReferredListTag(PageCache pageCache) {
        if (forList != null) {
            AnalysableTag tag = (AnalysableTag) pageCache.retrieve(MakumbaJspAnalyzer.TAG_CACHE, forList);
            if (tag == null) {
                throw new ProgrammerError("Could not find list with id '" + forList + "'");
            } else {
                return (AnalysableTag) tag;
            }
        } else {
            return null;
        }
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

    public void setTitle(String title) {
        checkValidAttributeValues("title", title, ATTRIBUTE_VALUES_TRUE_FALSE);
        this.title = title.trim();
    }

    public void setTotalCount(String totalCount) throws JspException {
        onlyInt("totalCount", totalCount);
        this.totalCount = totalCount;
    }

}
