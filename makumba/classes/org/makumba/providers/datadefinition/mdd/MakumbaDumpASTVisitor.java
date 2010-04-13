package org.makumba.providers.datadefinition.mdd;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * $Id: //depot/code/org.antlr/release/antlr-2.7.6/antlr/DumpASTVisitor.java#1 $
 */

import antlr.ASTVisitor;
import antlr.collections.AST;

/**
 * @author Manuel Gay
 * @version $Id: MakumbaDumpASTVisitor.java,v 1.1 13 Apr 2010 16:04:13 rudi Exp $
 */
public class MakumbaDumpASTVisitor implements ASTVisitor {

    private boolean showclass;

    public MakumbaDumpASTVisitor(boolean showClass) {
        this.showclass = showClass;
    }

    protected int level = 0;

    private StringBuilder tabs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("   ");
        }
        return sb;
    }

    public String toString(AST node) {
        StringBuilder sb = new StringBuilder();

        // Flatten this level of the tree if it has no children
        boolean flatten = /* true */false;
        AST node2;
        for (node2 = node; node2 != null; node2 = node2.getNextSibling()) {
            if (node2.getFirstChild() != null) {
                flatten = false;
                break;
            }
        }

        for (node2 = node; node2 != null; node2 = node2.getNextSibling()) {
            if (!flatten || node2 == node) {
                sb.append(tabs());
            }
            if (node2.getText() == null) {
                sb.append("nil");
            } else {
                sb.append(node2.getText());
            }

            sb.append(" [" + node2.getType() + "] "
                    + (showclass ? "{ " + node2.getClass().getCanonicalName() + " }" : ""));

            if (flatten) {
                sb.append(" ");
            } else {
                sb.append("\n");
            }

            if (node2.getFirstChild() != null) {
                level++;
                visit(node2.getFirstChild());
                level--;
            }
        }

        if (flatten) {
            sb.append("\n");
        }

        return sb.toString();
    }

    public void visit(AST node) {
        System.out.println(toString(node));
    }
}
