/*
 * Created on Mar 9, 2004
 *
 * CVS: $Header: /cvs/seek/web/SeekWikiWebApp/src/org/ecoinformatics/seek/web/wiki/MenuTreeNode.java,v 1.3 2004/03/30 15:40:06 tekell Exp $
 */
package org.ecoinformatics.seek.web.wiki;

import java.util.ArrayList;

/**
 * A menu tree node, representing one node in the menu
 * 
 * @author stekell
 * @author Manuel Gay
 * 
 */
public class MenuTreeNode {
    private MenuTreeNode parent;
    private String displayText;
    private String linkText;
    protected ArrayList<MenuTreeNode> children;

    public MenuTreeNode(String linkTextIn, String displayTextIn) {
        this.linkText = linkTextIn;
        this.displayText = displayTextIn;

        if (this.displayText == null) {
            this.displayText = this.linkText;
        }

        this.children = new ArrayList<MenuTreeNode>();
        MenuTree.put(this.linkText, this);
    }

    public MenuTreeNode(String pageNameIn) {
        this(pageNameIn, pageNameIn);
    }

    public MenuTreeNode(String pageNameIn, String displayNameIn, MenuTreeNode parentIn) {
        this(pageNameIn, displayNameIn);
        this.parent = parentIn;
    }

    /**
     * Adds a child to the current node
     */
    public void addChild(MenuTreeNode child) {
        child.parent = this;
        this.children.add(child);
    }

    /**
     * Gets the parent of this node
     */
    public MenuTreeNode getParent() {
        return this.parent;
    }

    /**
     * Gets the text for this link
     */
    public String getDisplayText() {
        return this.displayText;
    }

    /**
     * Gets the target this node links to
     */
    public String getLinkText() {
        return this.linkText;
    }
}
