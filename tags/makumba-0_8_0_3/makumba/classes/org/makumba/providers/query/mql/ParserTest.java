package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.hibernate.hql.ast.HqlParser;
import org.makumba.commons.ClassResource;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;

import antlr.collections.AST;
import antlr.debug.misc.ASTFrame;

public class ParserTest {

    private static QueryAnalysisProvider qap;

    public static void main(String[] argv) {
        qap = new HQLQueryAnalysisProvider();
        int line = 1;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/queries.txt").getContent()));
            String query = null;
            while ((query = rd.readLine()) != null) {
                AST a = analyseQuery(line, query);
                if (line == 1075) {
                    ASTFrame frame = new ASTFrame("normal",a);
                    frame.setVisible(true);
                }
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("analyzed " + line + " queries");

    }

    private static AST analyseQuery(int line, String query) {
        try {
            HqlParser parser = HqlParser.getInstance(query);
            parser.statement();
            if (parser.getParseErrorHandler().getErrorCount() > 0)
                parser.getParseErrorHandler().throwQueryException();
            return parser.getAST();
        } catch (Throwable t) {
            System.out.println(line + ": " + t.getMessage() + " " + query);
            return null;
        }
    }
}
