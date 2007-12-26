package org.makumba.list.pagination;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.commons.lang.StringUtils;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
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

    private static final String FIRST = "<<";

    private static final String NEXT = ">";

    private static final String LAST = ">>";

    private static final String LIMIT = "limit";

    private static final String PREVIOUS = "<";

    private static final String OFFSET = "offset";

    private static final long serialVersionUID = 1L;

    private static final String SPACE = "&nbsp;";

    private String forList;

    private String itemName = "Items";

    private String limit;

    private String offset;

    private String styleClass = "makumbaPagination";

    private String title;

    private String totalCount;

    private StringBuffer addTitle(StringBuffer sb, String page) {
        if (StringUtils.isEmpty(page)) {
            return sb;
        }
        if (StringUtils.equals(title, "true")) {
            StringBuffer tt = new StringBuffer("<span title=\"");
            if (page.equals(PREVIOUS)) {
                tt.append("Previous page\">");
            } else if (page.equals(NEXT)) {
                tt.append("Next page\">");
            } else if (page.equals(FIRST)) {
                tt.append("First page\">");
            } else if (page.equals(LAST)) {
                tt.append("Last page\">");
            } else {
                tt.append("Go to page " + page + "\">");
            }
            tt.append(sb);
            tt.append("</span>");
            return tt;
        }
        return sb;
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
        if (getParentListTag() == null && getReferredListTag(pageCache) == null && allNotEmpty) {
            throw new ProgrammerError(
                    "\'pagination\' tag must be enclosed in a 'list' tag, or the list specified with the 'forList' attribute, or "
                            + ALL_ATTRIBUTES + " must be specified!");
        }
        super.doStartAnalyze(pageCache);
    }

    public int doStartTag() {
        int offset = this.offset != null ? Integer.parseInt(this.offset) : getParentListTag().getOffsetInt();
        int limit = this.limit != null ? Integer.parseInt(this.limit) : getParentListTag().getLimitInt();
        int maxResults = this.totalCount != null ? Integer.parseInt(this.totalCount) : QueryTag.maxResults();
        int pages = (maxResults / limit);
        pages = maxResults % limit == 0 ? pages : pages + 1;
        if (pages >= 1) {
            int currentIndex = (offset / limit); // current index
            boolean firstPage = currentIndex < 2 ? true : false;
            boolean hasPreviousPage = currentIndex > 0 ? true : false;
            boolean hasNextPage = currentIndex < pages - 1 ? true : false;
            boolean lastPage = currentIndex > pages - 2 ? true : false;
            StringBuffer sb = new StringBuffer();
            String baseUrl = getBaseURL();

            try {
                JspWriter out = pageContext.getOut();
                sb.append("<div class=\"" + styleClass + "\">\n");
                sb.append("  <div style=\"float: left; width: 23%; height: 100%;\" >\n    ");
                if (hasPreviousPage) {
                    sb.append(getAnchor(baseUrl, 0, limit, FIRST)).append("|").append(SPACE).append("\n    ");
                    sb.append(getAnchor(baseUrl, (currentIndex - 1) * limit, limit, PREVIOUS)).append("|");
                } else {
                    sb.append(FIRST).append("|").append(SPACE).append("\n    ");;
                    sb.append(PREVIOUS).append("|");
                }
                sb.append("  </div>\n");

                sb.append("  <div style=\"float: left; width: 50%; height: 100%; margin: 0 15px;\" align=\"center\">\n    ");

                int itemCountLower = (currentIndex * limit) + 1;
                int itemCountUpper = Math.min(maxResults, (currentIndex + 1 * limit));
                sb.append("Showing ").append(itemName).append(" ").append(itemCountLower).append(" to ").append(
                    itemCountUpper).append("").append(" out of ").append(maxResults).append(" (Page ").append(
                    (currentIndex + 1)).append(" out of ").append(pages).append(")\n");
                sb.append("  </div>\n");

                sb.append("  <div style=\"float: right; width: 23%; height: 100%;\" align=\"right\">\n    ");
                if (hasNextPage) {
                    sb.append("|").append(SPACE).append(getAnchor(baseUrl, (currentIndex + 1) * limit, limit, NEXT)).append("\n    ");;
                    sb.append("|").append(SPACE).append(getAnchor(baseUrl, (pages - 1) * limit, limit, LAST));
                } else {
                    sb.append("|").append(SPACE).append(NEXT).append("\n    ");;
                    sb.append("|").append(SPACE).append(LAST);
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

    private String getAnchor(String baseUrl, int start, int range, String page) {
        return addTitle(
            new StringBuffer("<a href=\"").append(baseUrl).append(OFFSET).append("=").append(start).append("&").append(
                LIMIT).append("=").append(range).append("\"").append(NEXT).append(page).append("</a>").append(SPACE),
            page).toString();
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

    public String getQueryString(Map map) {
        if (map == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Iterator iterator = map.keySet().iterator();
        String obj = null;
        while (iterator.hasNext()) {
            obj = (String) iterator.next();
            if (!org.makumba.commons.StringUtils.equalsAny(obj, new String[] { LIMIT, OFFSET })) {
                String[] strings = (String[]) map.get(obj);
                for (int i = 0; i < strings.length; i++) {
                    sb.append(obj).append("=").append((strings)[i]).append("&");
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
