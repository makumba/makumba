package org.makumba.providers.query;

import org.makumba.providers.query.mql.ASTUtil;
import org.makumba.providers.query.mql.HqlASTFactory;
import org.makumba.providers.query.mql.HqlTokenTypes;

import antlr.collections.AST;

/**
 * Prints a pass1 AST tree back into a query.
 * @author Cristian Bogdan
 */
public class Pass1ASTPrinter {

    public static StringBuffer printAST(AST tree) {
        StringBuffer sb = new StringBuffer();
        tree = new HqlASTFactory().dupTree(tree);
        printRecursive(null, tree, sb);
        return sb;
    }

    private static void printRecursive(AST parent, AST ast, StringBuffer sb) {
        if (ast == null)
            return;
        space(sb);
        AST right;
        boolean noPar = false;

        // FIXME: elements
        // FIXME: stack
        // this is for printing the current node and its children. after this, we always print the first child
        switch (ast.getType()) {
            case HqlTokenTypes.QUERY:
                boolean subquery = false;
                if (sb.length() != 0) {
                    subquery = true;
                    sb.append("(");
                }

                AST sele_from = ast.getFirstChild();
                AST where = sele_from.getNextSibling();

                AST q = sele_from.getFirstChild().getNextSibling();
                if (q != null)
                    q.setNextSibling(sele_from.getFirstChild());
                else
                    q = sele_from.getFirstChild();
                printRecursive(null, q, sb);
                printRecursive(null, where, sb);
                if (subquery)
                    sb.append(")");
                break;

            case HqlTokenTypes.SELECT:
            case HqlTokenTypes.HAVING:
                printKeyword(ast, sb);
                printList(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.GROUP:
            case HqlTokenTypes.ORDER:
                printKeyword(ast, sb);
                sb.append("BY ");
                printList(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.ELEMENTS:
                printKeyword(ast, sb);
            case HqlTokenTypes.IN_LIST:
                noPar = ast.getFirstChild().getType() == HqlTokenTypes.ELEMENTS;
            case HqlTokenTypes.EXPR_LIST:
                if (!noPar)
                    sb.append('(');
                printList(ast, ast.getFirstChild(), sb);
                if (!noPar)
                    sb.append(')');
                break;

            case HqlTokenTypes.FROM:
                printKeyword(ast, sb);
                AST a = ast.getFirstChild();
                right = ast.getNextSibling();
                ast.setNextSibling(null);
                boolean first = true;
                while (a != null) {
                    right = a.getNextSibling();
                    a.setNextSibling(null);

                    switch (a.getType()) {
                        case HqlTokenTypes.INNER:
                        case HqlTokenTypes.OUTER:
                        case HqlTokenTypes.LEFT:
                            printKeyword(a, sb);
                            a = a.getNextSibling();
                        case HqlTokenTypes.JOIN:
                            printKeyword(a, sb);
                            break;
                        case HqlTokenTypes.RANGE:
                            sb.append(first ? "" : ", ");
                    }
                    printRecursive(a, a.getFirstChild(), sb);

                    a = right;
                    first = false;
                }
                break;

            case HqlTokenTypes.DOT:
                sb.append(ASTUtil.getPath(ast));
                break;

            case HqlTokenTypes.METHOD_CALL:
                printRecursive(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.WHEN:
                sb.append(ast.getText());
                AST then = ast.getFirstChild().getNextSibling();
                ast.getFirstChild().setNextSibling(null);
                printRecursive(ast, ast.getFirstChild(), sb);

                sb.append(" then ");
                printRecursive(ast, then, sb);

                break;

            case HqlTokenTypes.AS:
            case HqlTokenTypes.PLUS:
            case HqlTokenTypes.MINUS:
            case HqlTokenTypes.STAR:
            case HqlTokenTypes.DIV:
            case HqlTokenTypes.AND:
            case HqlTokenTypes.OR:
            case HqlTokenTypes.LT:
            case HqlTokenTypes.GT:
            case HqlTokenTypes.LE:
            case HqlTokenTypes.GE:
            case HqlTokenTypes.EQ:
            case HqlTokenTypes.NE:
            case HqlTokenTypes.LIKE:
            case HqlTokenTypes.NOT_LIKE:
            case HqlTokenTypes.IN:
                boolean prio = checkPriority(parent, ast);
                if (prio)
                    sb.append('(');
                right = ast.getFirstChild().getNextSibling();
                ast.getFirstChild().setNextSibling(null);
                printRecursive(ast, ast.getFirstChild(), sb);
                switch (ast.getType()) {
                    case HqlTokenTypes.DOT:
                        sb.append(ast.getText());
                    default:
                        printKeyword(ast, sb);
                }
                printRecursive(ast, right, sb);
                if (prio)
                    sb.append(')');
                break;
            default:
                sb.append(ast.getText());
                printRecursive(ast, ast.getFirstChild(), sb);

        }
        printRecursive(parent, ast.getNextSibling(), sb);
        if (ast.getType() == HqlTokenTypes.CASE)
            sb.append(" end");

    }

    private static boolean checkPriority(AST prnt, AST ast) {
        int parent = prnt.getType();
        int child = ast.getType();

        if (child == HqlTokenTypes.OR && //
                parent == HqlTokenTypes.AND)
            return true;
        if ((child == HqlTokenTypes.AND || child == HqlTokenTypes.OR) && //
                (parent == HqlTokenTypes.EQ || parent == HqlTokenTypes.NE || parent == HqlTokenTypes.NOT))
            return true;
        if ((child == HqlTokenTypes.PLUS || child == HqlTokenTypes.MINUS)
                && //
                (parent == HqlTokenTypes.STAR || parent == HqlTokenTypes.DIV) || parent == HqlTokenTypes.UNARY_MINUS
                || parent == HqlTokenTypes.UNARY_PLUS)

            return true;
        return false;

    }

    private static void space(StringBuffer sb) {
        if (sb.length() > 0 && !(sb.charAt(sb.length() - 1) == ' ' || sb.charAt(sb.length() - 1) == '('))
            sb.append(' ');
    }

    private static void printKeyword(AST ast, StringBuffer sb) {
        space(sb);
        sb.append(ast.getText());
        sb.append(' ');
    }

    private static void printList(AST parent, AST ast, StringBuffer sb) {
        if (ast == null)
            return;
        AST right = ast.getNextSibling();
        ast.setNextSibling(null);
        printRecursive(parent, ast, sb);
        while (right != null) {
            if (ast.getType() != HqlTokenTypes.DISTINCT && right.getType() != HqlTokenTypes.ASCENDING
                    && right.getType() != HqlTokenTypes.DESCENDING)
                sb.append(",");
            ast = right;
            right = ast.getNextSibling();
            ast.setNextSibling(null);
            printRecursive(parent, ast, sb);
        }

    }

}
