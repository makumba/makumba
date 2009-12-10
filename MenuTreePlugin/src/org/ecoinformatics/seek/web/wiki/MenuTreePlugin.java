/*
 * Created on Mar 9, 2004
 *
 * CVS: $Header: /cvs/seek/web/SeekWikiWebApp/src/org/ecoinformatics/seek/web/wiki/MenuTreePlugin.java,v 1.3 2004/03/30 15:40:06 tekell Exp $
 */
package org.ecoinformatics.seek.web.wiki;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.plugin.PluginException;
import com.ecyrd.jspwiki.plugin.WikiPlugin;
import com.ecyrd.jspwiki.providers.WikiPageProvider;

/**
 * JspWikiPlugin to generate a tree base navigation A WikiPage specified by the
 * "menuPage" parameter (by default "MenuTree") is used as the data source for
 * the menu. This page consists of a hierarchy of links as bullets, for example, <br>
 * *[Optional Display Text|WikiPageName]<br>
 * **[ChildPage1]<br>
 * ***[GrandChildPage1]<br>
 * **[ChildPage2]<br>
 * **[ChildPage3]<br>
 * *[AnotherPage]<br>
 * <br>
 * The menu functions as a set of expanding/collapsing lists/sublists. When a
 * page is displayed that is contained in the menu, all the necessary menu nodes
 * are expanded to reveal (and highlight) the menu item and it's children. CSS
 * Styles can be used to customize the look to use images such as open/closed
 * folders .ul.MenuTree, ul.MenuTreeRoot, li.MenuTree, li.MenuTreeSelected,
 * li.MenuTreeExpanded, li. MenuTreeLeaf, li.MenuTreeLeafSelected
 * 
 * @author stekell
 * @author Manuel Gay
 * 
 */
public class MenuTreePlugin implements WikiPlugin {

    private static final String PARAM_MENUPAGE = "menuPage";

    private static final String DEFAULT_MENUPAGE = "MenuTree";

    public String execute(WikiContext context, Map params) throws PluginException {

        WikiEngine engine = context.getEngine();

        String ret = "";

        String menuPage = (String) params.get(PARAM_MENUPAGE);
        if (menuPage == null) {
            menuPage = DEFAULT_MENUPAGE;
        }
        if (!engine.pageExists(menuPage)) {
            return engine.textToHTML(context, "%%error Page " + menuPage
                    + " not found, no tree menu can be generated%%");
        }

        try {

            String menuText = engine.getPureText(menuPage, WikiPageProvider.LATEST_VERSION);
            MenuTree.compute(menuText);
            
            LinkedHashMap<Integer, String> headers = getMenuHeaders(menuText);
            
            Iterator<Integer> it = headers.keySet().iterator();
            Integer i = it.next();
            while(i < MenuTree.startMenu && it.hasNext()) {
                ret += "<h1>" + engine.textToHTML(context, headers.get(i)) + "</h1>\n";
                i = it.next();
            }
            
            ret += getMenuHtml(context.getPage().getName(), engine);
            
            boolean done = false;
            while(i > MenuTree.endMenu && !done) {
                ret += "<h1>" + engine.textToHTML(context, headers.get(i)) + "</h1>\n";
                if(it.hasNext()) {
                    i = it.next();
                } else {
                    done = true;
                }
            }
            
        } catch (Exception e) {
            ret = e.toString();
            e.printStackTrace(System.err);
        }

        return ret;
    }

    /**
     * Scans the menu page and fetches the first header element in order to generate a h1 header
     */
    private LinkedHashMap<Integer, String> getMenuHeaders(String menuText) {
        
        LinkedHashMap<Integer, String> headers = new LinkedHashMap<Integer, String>();
        
        Matcher m = Pattern.compile("^!!! *(.*)$", Pattern.MULTILINE).matcher(menuText);
        
        // fetch the first one
        while(m.find()) {
            headers.put(m.start(), m.group(1));
        }

        return headers;
    }
    
    /**
     * Generates the HTML for the menu
     * 
     * @param currentPageName
     *            the name of the page that is currently being displayed
     * @param engine
     *            the {@link WikiEngine}
     * @return the HTML String for the current page
     */
    private String getMenuHtml(String currentPageName, WikiEngine engine) {
        StringBuffer sb = new StringBuffer();

        HashSet<MenuTreeNode> nodesInPath = new HashSet<MenuTreeNode>();
        MenuTreeNode currentNode = MenuTree.get(currentPageName);
        MenuTreeNode selectedNode = currentNode;
        if (currentNode != null) {
            nodesInPath.add(currentNode);

            while (currentNode.getParent() != null) {
                currentNode = currentNode.getParent();
                nodesInPath.add(currentNode);
            }
        }

        MenuTreeNode root = MenuTree.root;

        buildChildMenuHtml(root, selectedNode, nodesInPath, sb, engine);

        return sb.toString();
    }

    /**
     * Builds the HTML for a given node
     */
    private void buildChildMenuHtml(MenuTreeNode node, MenuTreeNode selectedNode, HashSet<MenuTreeNode> nodesInPath,
            StringBuffer sb, WikiEngine engine) {

        MenuTreeNode childNode;

        if (!node.children.isEmpty()) {
            if (node.getLinkText().equals("ROOT")) {
                sb.append("<ul class=\"MenuTreeRoot\">\n");
            } else {
                sb.append("<ul class=\"MenuTree\">\n");
            }

            Iterator<MenuTreeNode> iter = node.children.iterator();

            while (iter.hasNext()) {
                childNode = (MenuTreeNode) iter.next();

                String linkText = childNode.getLinkText();
                String url = engine.getURL(WikiContext.VIEW, linkText, null, true);
                if(linkText.startsWith("http://")) {
                    url = linkText;
                }
                
                if (childNode == selectedNode && !childNode.children.isEmpty()) {
                    sb.append("<a class=\"MenuTreeSelected\" href=\""
                            + url + "\">"
                            + "<li class=\"MenuTreeSelected\">"
                            + childNode.getDisplayText() + "</li></a>\n");
                } else if (childNode == selectedNode) {
                    sb.append("<a class=\"MenuTreeSelected\" href=\""
                            + url + "\">"
                            + "<li class=\"MenuTreeLeafSelected\">"
                            + childNode.getDisplayText() + "</li></a>\n");
                } else if (!nodesInPath.contains(childNode) && childNode.children.isEmpty()) {
                    sb.append("<a href=\""
                            + url + "\">"
                            + "<li class=\"MenuTreeLeaf\">"
                            + childNode.getDisplayText() + "</li></a>\n");
                } else if (nodesInPath.contains(childNode) && !childNode.children.isEmpty()) {
                    sb.append("<a href=\""
                            + url + "\">"
                            + "<li class=\"MenuTreeExpanded\">"
                            + childNode.getDisplayText() + "</li></a>\n");
                } else {
                    sb.append("<a href=\""
                            + url + "\">"
                            + "<li class=\"MenuTree\">"
                            + childNode.getDisplayText() + "</li></a>\n");
                }

                if (!childNode.children.isEmpty() && nodesInPath.contains(childNode)) {
                    buildChildMenuHtml(childNode, selectedNode, nodesInPath, sb, engine);
                }
            }
            
            sb.append("</ul>\n");
        }
   }
}