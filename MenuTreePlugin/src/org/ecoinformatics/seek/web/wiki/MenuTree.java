/*
 * Created on Mar 12, 2004
 * CVS: $Header: /cvs/seek/web/SeekWikiWebApp/src/org/ecoinformatics/seek/web/wiki/MenuTree.java,v 1.4 2004/04/22 21:11:54 tekell Exp $
 */
package org.ecoinformatics.seek.web.wiki;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the menu tree and builds the structured used to
 * generate the output HTML.
 * 
 * @author stekell
 * @author Manuel Gay
 *
 */
public class MenuTree {

    static public HashMap<String, MenuTreeNode> MenuTreeNodes = new HashMap<String, MenuTreeNode>();

    static public MenuTreeNode root = new MenuTreeNode("ROOT");

    public static MenuTreeNode put(String key, MenuTreeNode node) {
        return MenuTreeNodes.put(key, node);
    }

    public static MenuTreeNode get(String key) {
        return MenuTreeNodes.get(key);
    }

    /**
     * Initializes the menu tree
     * @param wikiMenuText
     *            the text of the menu
     */
    public static void compute(String wikiMenuText) {

        root = new MenuTreeNode("ROOT");

        MenuTreeNode node;
        MenuTreeNode parentNode = root;
        MenuTreeNode prevNode = root;

        int levelNum = 0;
        int prevLevelNum = 0;

        String displayName;
        String pageName;

        if (wikiMenuText != null) {

            Matcher m = Pattern.compile("(?m)^(\\**)\\s*\\[(\\s*(.+)\\s*\\|)?\\s*(\\S+)\\s*\\]\\s*$").matcher(
                    wikiMenuText);

            while (m.find()) {
                levelNum = m.group(1).length();
                displayName = m.group(3);
                pageName = m.group(4);
                node = new MenuTreeNode(pageName, displayName);

                // skip errors where input indents more than level (* -> ***)
                if ((levelNum - prevLevelNum) > 1) {
                    break;
                }

                if (levelNum == 1) {
                    parentNode = root; // temp error workaround code
                }
                // indenting another bullet, going to lower level (* -> **)
                else if (levelNum > prevLevelNum) {
                    parentNode = prevNode;
                }
                // unindenting one or more bullets, going back up to higher
                // level (*** -> *)
                else if (levelNum < prevLevelNum) {
                    for (int i = 1; i <= (prevLevelNum - levelNum); i++) {
                        parentNode = parentNode.getParent();
                    }
                }

                parentNode.addChild(node);
                prevNode = node;
                prevLevelNum = levelNum;
            }
        } else { // null wikiMenuText
            node = new MenuTreeNode("MenuTree", "Create a MenuTree");
            root.addChild(node);
        }
    }
}
