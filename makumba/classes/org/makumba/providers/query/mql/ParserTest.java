package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NameResolver;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;
import org.makumba.providers.query.oql.QueryAST;

import antlr.collections.AST;

/**
 * Test the Mql analyzer against a query corpus found in queries.txt. Compare the generated sql with the one of the old
 * OQL parser.
 * 
 * @author Cristian Bogdan
 * @version $Id: ParserTest.java,v 1.1 Aug 5, 2008 5:55:08 PM cristi Exp $
 */
public class ParserTest {

    private static QueryAnalysisProvider qap = new HQLQueryAnalysisProvider();

    private static PrintWriter pw = new PrintWriter(System.out);

    private static ASTPrinter printerHql = new ASTPrinter(HqlTokenTypes.class);

    private static ASTPrinter printerHqlSql = new ASTPrinter(HqlSqlTokenTypes.class);

    private static NameResolver nr;
    static {
        String databaseProperties = "test/localhost_mysql_makumba.properties";

        Properties p = new Properties();
        try {
            p.load(org.makumba.commons.ClassResource.get(databaseProperties).openStream());
        } catch (Exception e) {
            throw new org.makumba.ConfigFileError(databaseProperties);
        }

        nr = new NameResolver();
    }

    public static void main(String[] argv) {

        int line = 1;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/queries.txt").getContent()));
            String query = null;
            while ((query = rd.readLine()) != null) {
                if (!query.trim().startsWith("#")) {
                    analyseQuery(line, query);
                }
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("analyzed " + line + " queries");

    }

    /** cleanup of generated SQL code for MQL-OQL comparison purposes */
    static String cleanUp(String s, String toRemove) {
        StringBuffer ret = new StringBuffer();
        char lastNonSpace = 0;
        boolean prevSpace = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (toRemove.indexOf(c) == -1) {
                if (prevSpace) {
                    if (Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(lastNonSpace)) {
                        ret.append(' ');
                    }
                }
                lastNonSpace = c;
                ret.append(c);
                prevSpace = false;
                continue;
            }
            prevSpace = true;
        }
        return ret.toString();
    }

    private static void analyseQuery(int line, String query) {
        AST hql_sql = null;
        boolean passedMql = false;
        Throwable thr = null;
        boolean printedError;
        String oql_sql = null;

        MqlQueryAnalysis mq = null;
        Throwable mqlThr=null;
        try {
            mq = new MqlQueryAnalysis(query);
        } catch (Throwable t) {
            mqlThr=t;
        }
        try {
            oql_sql = ((QueryAST) QueryAST.parseQueryFundamental(query)).writeInSQLQuery(nr).toLowerCase();
            if (mqlThr==null) {
                String mql_sql = mq.writeInSQLQuery(nr).toLowerCase();

                oql_sql = cleanUp(oql_sql, " ").replace('\"', '\'');
                mql_sql = cleanUp(mql_sql, " ");

                if (!oql_sql.equals(mql_sql) && !cleanUp(oql_sql, "()").equals(cleanUp(mql_sql, "()"))) {
                    System.out.println(line + ": OQL!=MQL: " + query + "\n\t" + mql_sql + "\n\t" + oql_sql);
                }
             
            }
            /*
             * HqlAnalyzeWalker walker = new HqlAnalyzeWalker(); walker.setAllowLogicalExprInSelect(true);
             * walker.setTypeComputer(new MddObjectType()); walker.setDebug(query); walker.statement(hql);
             */
        } catch (Throwable t) {
            if(mqlThr!=null)
                System.err.println(line + ": MQL: " + t.getMessage() + " " + query);
            if (mqlThr!=null // we also had an MQL problem
                    || "survey".equals(t.getMessage()) // HQL analyzer fails stuff like FROM T t, t n
                    || t.toString().indexOf("FROM expected") != -1 // FROM-less queries can't pass
                    || t.toString().indexOf("In operand") != -1 // OQL has issues with set operands
                    || t.toString().indexOf("unexpected token: JOIN") != -1 // OQL doesn't know join
                    || t.toString().indexOf("defined twice") != -1 // HQLAnalyzer is really strict
            ) {
                System.err.println(line + ":"+(mqlThr==null?" only in":"")+" OQL: " + t.getMessage() + " " + query);

                return;
            }
            System.err.println("Unchecked error only in OQL: ");
            t.printStackTrace();
            return;
        }
        if(mqlThr!=null){
            System.err.println(line + ": only in MQL: " + mqlThr.getMessage() + " " + query);
            System.out.println(line+": OQL SQL: " + oql_sql);
        }
    }

}
