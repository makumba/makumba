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

    // FIXME: having the current level of recursion as a global variable is not good for concurrency
    protected int level = 0;

    private void tabs(StringBuilder sb) {
        for (int i = 0; i < level; i++) {
            sb.append("   ");
        }
    }

    protected void toString(AST node, StringBuilder sb) {

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
                tabs(sb);
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
                toString(node2.getFirstChild(), sb);
                level--;
            }
        }

        if (flatten) {
            sb.append("\n");
        }
    }

    public String toString(AST node) {
        StringBuilder sb = new StringBuilder();
        toString(node, sb);
        return sb.toString();
    }

    public void visit(AST node) {
        System.out.print(toString(node));
    }

}
