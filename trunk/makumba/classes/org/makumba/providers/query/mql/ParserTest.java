package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.hibernate.hql.ast.HqlParser;
import org.hibernate.hql.ast.tree.Node;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.makumba.commons.ClassResource;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;
import org.makumba.providers.query.hql.HqlAnalyzeWalker;
import org.makumba.providers.query.hql.MddObjectType;

import antlr.collections.AST;

//import antlr.debug.misc.ASTFrame;

public class ParserTest {

    private static QueryAnalysisProvider qap= new HQLQueryAnalysisProvider();
    private static PrintWriter pw = new PrintWriter(System.out);
    private static ASTPrinter printer = new ASTPrinter(HqlTokenTypes.class);

    public static void main(String[] argv) {
        
        int line = 1;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/queries.txt").getContent()));
            String query = null;
            while ((query = rd.readLine()) != null) {
                query = preProcess(query);
                analyseQuery(line, query);                   
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("analyzed " + line + " queries");

    }

    private static void transformOQL(AST a) {
        if (a == null)
            return;
        if (a.getType() == HqlTokenTypes.IDENT && a.getText().startsWith("$")) {
            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);
            para.setText("makumbaParam" + a.getText().substring(1));
            a.setFirstChild(para);
            a.setText(":");
        } else {
            if (a.getType() == HqlTokenTypes.EQ || a.getType() == HqlTokenTypes.NE) {
                if (isNil(a.getFirstChild())) {
                    setNullTest(a);
                    a.setFirstChild(a.getFirstChild().getNextSibling());
                } else if (isNil(a.getFirstChild().getNextSibling())) {
                    setNullTest(a);
                    a.getFirstChild().setNextSibling(null);
                }

            }
        }
        transformOQL(a.getFirstChild());
        transformOQL(a.getNextSibling());
    }

    private static void setNullTest(AST a) {
        if (a.getType() == HqlTokenTypes.EQ) {
            a.setType(HqlTokenTypes.IS_NULL);
            a.setText("is null");
        } else {
            a.setType(HqlTokenTypes.IS_NOT_NULL);
            a.setText("is not null");
        }
    }

    private static boolean isNil(AST a) {
        return a.getType() == HqlTokenTypes.IDENT && a.getText().equals("NIL");
    }

    private static void analyseQuery(int line, String query) {
        try {
            HqlParser parser = HqlParser.getInstance(query);
            parser.statement();
            if (parser.getParseErrorHandler().getErrorCount() > 0)
                parser.getParseErrorHandler().throwQueryException();
            AST a= parser.getAST();
            transformOQL(a);

            if (line == 295) {

                // ASTFrame frame = new ASTFrame("normal",a);
                // frame.setVisible(true);

                printer.showAst(a, pw);
            }
            HqlAnalyzeWalker walker = new HqlAnalyzeWalker();
            walker.setTypeComputer(new MddObjectType());
            walker.setDebug(query);
            walker.statement(a);

        } catch (Throwable t) {
            System.out.println(line + ": " + t.getMessage() + " " + query);
        }
    }

    public static final String regExpInSET = "in" + RegExpUtils.minOneWhitespace + "set" + RegExpUtils.whitespace
            + "\\(";

    public static final Pattern patternInSet = Pattern.compile(regExpInSET);

    public static String preProcess(String query) {
        // replace -> (subset separators) with __
        query = query.replaceAll("->", "__");

        // replace IN SET with IN.
        Matcher m = patternInSet.matcher(query.toLowerCase()); // find all occurrences of lower-case "in set"
        while (m.find()) {
            int start = m.start();
            int beginSet = m.group().indexOf("set"); // find location of "set" keyword
            // System.out.println(query);
            // composing query by concatenating the part before "set", 3 space and the part after "set"
            query = query.substring(0, start + beginSet) + "   " + query.substring(start + beginSet + 3);
            // System.out.println(query);
            // System.out.println();
        }
        query = query.replaceAll("IN SET", "IN    ");
        return query;
    }

}
