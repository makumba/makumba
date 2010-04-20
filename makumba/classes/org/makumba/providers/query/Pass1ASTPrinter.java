package org.makumba.providers.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.makumba.commons.ClassResource;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.datadefinition.mdd.MakumbaDumpASTVisitor;
import org.makumba.providers.query.mql.HqlASTFactory;
import org.makumba.providers.query.mql.HqlTokenTypes;

import antlr.collections.AST;

/**
 * Prints a pass1 AST tree back into a query.
 * 
 * @author Cristian Bogdan
 */
public class Pass1ASTPrinter {

    /**
     * print a AST pass1 tree back to its "original" HQL query
     * 
     * @param tree
     * @return the HQL query
     */
    public static StringBuffer printAST(AST tree) {
        StringBuffer sb = new StringBuffer();
        tree = new HqlASTFactory().dupTree(tree);
        printRecursive(null, tree, sb);
        return sb;
    }

    /**
     * Recursively prints the tree. In principle printing the node, its first child and its next sibling should generate
     * a query. However:
     * <ul>
     * <li>some nodes produce nothing (query, in_list, expr_list).
     * <li>the query natural order is re-arranged in the tree (FROM before SELECT).
     * <li>some text items are missing (case-when's then, end).
     * <li>commas are missing in lists (IN, SELECT, function call, sometimes in FROM)
     * </ul>
     * 
     * @param parent
     *            parent of the current element
     * @param ast
     *            current element to print
     * @param sb
     *            string buffer result
     */
    private static void printRecursive(AST parent, AST ast, StringBuffer sb) {
        if (ast == null)
            return;
        space(sb);
        AST right;
        boolean noPar = false;
        boolean prio = false;

        // this is for printing the current node and its children. after this, we always print the next sibling
        switch (ast.getType()) {
            case HqlTokenTypes.QUERY:
                // subqueries are normally in parantheses
                boolean subquery = false;
                if (parent != null) {
                    subquery = true;
                    sb.append("(");
                }

                // we undo the arrangement that pass1 normally makes (it puts FROM before SELECT for easier pass2)
                AST sele_from = ast.getFirstChild();
                AST where = sele_from.getNextSibling();

                // first we print the FROM
                printRecursive(null, sele_from.getFirstChild().getNextSibling(), sb);

                // we remove the link between FROM and SELECt
                sele_from.getFirstChild().setNextSibling(null);
                // and we print the FROM
                printRecursive(null, sele_from.getFirstChild(), sb);

                // this will print all the next siblings like WHERE, ORDERBY, etc.
                printRecursive(null, where, sb);
                if (subquery)
                    sb.append(")");
                break;

            case HqlTokenTypes.SELECT:
            case HqlTokenTypes.HAVING:
                printKeyword(ast, sb);
                // comma-separated children
                printList(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.GROUP:
            case HqlTokenTypes.ORDER:
                printKeyword(ast, sb);
                // wrong keyword in the AST
                sb.append("BY ");
                // comma separated children
                printList(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.ELEMENTS:
            case HqlTokenTypes.AGGREGATE:
            case HqlTokenTypes.COUNT:
                // we don't use printKeyword as that would print a space after
                space(sb);
                sb.append(ast.getText());

                // then come parantheses after elements
            case HqlTokenTypes.IN_LIST:
                noPar = ast.getFirstChild().getType() == HqlTokenTypes.ELEMENTS;
                // no parantheses in IN_LIST if it contains elements
                if (!noPar)
                    sb.append('(');
                // comma-separated children
                printList(ast, ast.getFirstChild(), sb);
                if (!noPar)
                    sb.append(')');
                break;
                
            case HqlTokenTypes.FROM:
                printKeyword(ast, sb);
                // in principle children of FROM are comma separated but
                // sometimes the comma is replaced with joins
                AST a = ast.getFirstChild();
                right = ast.getNextSibling();
                ast.setNextSibling(null);
                boolean first = true;
                while (a != null) {
                    right = a.getNextSibling();

                    // to print the separator, we must unlink the AST from its next sibling
                    // otherwise printing the AST will also print its next sibling
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
                            // basically only RANGE produces a comma, and only if it's not first
                            sb.append(first ? "" : ", ");
                    }
                    printRecursive(a, a.getFirstChild(), sb);

                    a = right;
                    first = false;
                }
                break;

            case HqlTokenTypes.METHOD_CALL:
                // we don't print anything, just the first child
                // the expr_list will print the function call prarantheses
                printRecursive(ast, ast.getFirstChild(), sb);
                break;

            case HqlTokenTypes.EXPR_LIST:
                if(sb.charAt(sb.length()-1)==' ')
                    sb.setCharAt(sb.length()-1, '(');
                else
                    sb.append('(');
                // comma-separated children
                printList(ast, ast.getFirstChild(), sb);
                sb.append(')');                
                break;

            case HqlTokenTypes.WHEN:
                // then is not part of the tree
                sb.append(ast.getText());
                // so we unconnect the then branch from the first child (condition)
                AST then = ast.getFirstChild().getNextSibling();
                ast.getFirstChild().setNextSibling(null);
                // print first child
                printRecursive(ast, ast.getFirstChild(), sb);
                // print then
                sb.append(" then ");
                printRecursive(ast, then, sb);

                break;

            // not in and not like do
            // NOT ( their binary expression )
            case HqlTokenTypes.NOT_IN:
            case HqlTokenTypes.NOT_LIKE:
                space(sb);
                sb.append("not");
                prio = true;

                // binary operators, some may be missing, please add!
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
            case HqlTokenTypes.IN:
                // dot is treated like a normal binary operator, except it won't print spaces for aesthetic reasons
            case HqlTokenTypes.DOT:
                // we compare with the parent operator, if any
                prio = checkPriority(parent, ast);
                // if we are lower precedence, we print parantheses
                if (prio)
                    sb.append('(');
                // disconnect first from second child
                right = ast.getFirstChild().getNextSibling();
                ast.getFirstChild().setNextSibling(null);
                // print first child
                printRecursive(ast, ast.getFirstChild(), sb);
                // print operator
                printKeyword(ast, sb);
                // print second child
                printRecursive(ast, right, sb);
                if (prio)
                    sb.append(')');
                break;

            default:
                // for all other cases, we print the node, then its first child
                sb.append(ast.getText());
                printRecursive(ast, ast.getFirstChild(), sb);

        }
        // the end of case
        if (parent != null && parent.getType() == HqlTokenTypes.CASE && ast.getNextSibling() == null)
            sb.append(" end");

        // we then print the next sibling, with the same parent.
        // this tail recursion is ready to be eliminated but I leave it in for code clarity
        printRecursive(parent, ast.getNextSibling(), sb);

    }

    // check operator precedence
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

    // print a space if we didn't print already or we are not after an open para
    private static void space(StringBuffer sb) {
        if (sb.length() == 0)
            return;
        char last = sb.charAt(sb.length() - 1);
        if (!(last == ' ' || last == '(' || last == ':' || last == '.'))
            sb.append(' ');
    }

    private static void printKeyword(AST ast, StringBuffer sb) {
        // print spaces before and after, if we are not a dot
        if (ast.getType() != HqlTokenTypes.DOT)
            space(sb);
        // the indexOf(}) is used for {not}in and {not}like.
        // it normally returns -1, and then +1 it gives 0, i.e. the whole string
        sb.append(ast.getText().substring(ast.getText().lastIndexOf('}') + 1));
        if (ast.getType() != HqlTokenTypes.DOT)
            sb.append(' ');
    }

    /*
     * print a comma-separated list
     */
    private static void printList(AST parent, AST ast, StringBuffer sb) {
        if (ast == null)
            return;
        // disconnect first from second child
        AST right = ast.getNextSibling();
        ast.setNextSibling(null);
        // print first element
        printRecursive(parent, ast, sb);
        while (right != null) {
            // comma is not printed after DISTINCT, before ASC or DESC
            if (ast.getType() != HqlTokenTypes.DISTINCT && right.getType() != HqlTokenTypes.ASCENDING
                    && right.getType() != HqlTokenTypes.DESCENDING)
                sb.append(",");
            // iterate in list
            ast = right;
            // disconnect next element
            right = ast.getNextSibling();
            ast.setNextSibling(null);
            // print element
            printRecursive(parent, ast, sb);
        }
    }

    /**
     * Test method that prints a given AST, re-parses it and compares the initial and final AST
     * 
     * @param f
     *            the initial AST
     * @param query
     *            the query from which the initial AST came. Used only for debug purposes in case of error
     * @return
     */
    public static boolean testPrinter(AST f, String query) {
        String printed = printAST(f).toString();
        try {
            AST printedAST = QueryAnalysisProvider.parseQuery(printed);
            if (!QueryAnalysisProvider.compare(new ArrayList<AST>(), printedAST, f)) {
                // new MakumbaDumpASTVisitor(false).visit(f);
                System.out.println(query);
                System.out.println(printed);
                // new MakumbaDumpASTVisitor(false).visit(printedAST);
                System.out.println("\n\n");
                return false;
            }
            if(printed.toUpperCase().trim().equals(query.toUpperCase().trim()))
                System.out.print('.');
            //System.out.println(printed);
            
        } catch (Throwable e) {
            System.out.println(e);
            new MakumbaDumpASTVisitor(false).visit(f);

            System.out.println(query);
            System.out.println(printed);
            return false;
        }
        return true;
    }

    public static void main(String[] argv) {
        testCorpus("org/makumba/providers/query/mql/queries.txt", false);
        //testCorpus("org/makumba/providers/query/inlinerCorpus.txt", true);
    }

    private static void testCorpus(String corpusFile, boolean inline) {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    (InputStream) ClassResource.get(corpusFile).getContent()));
            String query = null;
            int line = 1;
            while ((query = rd.readLine()) != null) {
                if (!query.trim().startsWith("#")) {
                    query = QueryAnalysisProvider.checkForFrom(query);
                    try {
                        AST a = QueryAnalysisProvider.parseQuery(query);
                        testPrinter(a, query);
                        if(inline){
                            a = QueryAnalysisProvider.inlineFunctions(query);
                            testPrinter(a, FunctionInliner.inline(query, QueryProvider.getQueryAnalzyer("oql")));
                        }
                    } catch (Throwable t) {
                        System.err.println(line + ": " + t);
                    }
                }
                line++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}