package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NameResolver;
import org.makumba.providers.SQLParameterTransformer;

import antlr.collections.AST;

/**
 * Test the Mql analyzer against a query corpus found in queries.txt. Compare the generated sql with the one of the old
 * OQL parser.
 * 
 * @author Cristian Bogdan
 * @version $Id: ParserTest.java,v 1.1 Aug 5, 2008 5:55:08 PM cristi Exp $
 */
public class ParserTest {
    
    private static Vector<String> errors = new Vector<String>();

    private static NameResolver nr;
    static {
        String databaseProperties = "test/localhost_mysql_makumba.properties";

        Properties p = new Properties();
        try {
            p.load(org.makumba.commons.ClassResource.get(databaseProperties).openStream());
        } catch (Exception e) {
            throw new org.makumba.ProgrammerError(databaseProperties);
        }

        nr = new NameResolver();
    }

    public static void main(String[] argv) {
        boolean automaticLeftJoin=false;
        int line = 1;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/queries.txt").getContent()));
            String query = null;
            while ((query = rd.readLine()) != null) {
                if(query.startsWith("#automaticLeftJoin"))
                    automaticLeftJoin=true;
                if (!query.trim().startsWith("#")) {
                    analyseQuery(line, query, automaticLeftJoin);
                }
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println("analyzed " + line + " queries");
        System.err.println("**********************************");
        System.err.println("********** Found following errors:");
        for(String e : errors) {
            System.err.println(e);
        }

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

    private static void analyseQuery(int line, String query, boolean automaticLeftJoin) {
        AST hql_sql = null;
        boolean passedMql = false;
        Throwable thr = null;
        boolean printedError;
        String mql_sql = null; 

        MqlQueryAnalysis mq = null;
        SQLParameterTransformer qG = null;
        Throwable mqlThr=null;
        try {
            mq = new MqlQueryAnalysis(query, false, automaticLeftJoin);
            qG = (SQLParameterTransformer) qG;
            
            mql_sql = qG.getSQLQuery(nr).toLowerCase();
        } catch (Throwable t) {
            mqlThr=t;
        }
        try {
            if (mqlThr==null) {
                mql_sql = cleanUp(mql_sql, " ");

                /* comparison with OLD mql parser. it's here to keep the mechanism in case we evolve to something else
                StringBuffer sb= new StringBuffer();
                compareMdds("parameter", sb, mq.getParameterTypes(), oq.getParameterTypes());
                if(sb.length()>0) {
                    System.err.println(line + ": "+ sb+" "+ query);
                }
                sb= new StringBuffer();
                compareMdds("projection", sb, mq.getProjectionType(), oq.getProjectionType());
                if(sb.length()>0) {
                    System.err.println(line + ": "+ sb+" "+ query); 
                }
                String mqLabels = mq.getLabelTypes().toString();
                if(!mqLabels.equals(oq.getLabelTypes().toString())&&!mqLabels.equals("{c=org.makumba.db.makumba.Catalog}")){
                    System.err.println(line + ": "+ query+"\n\t"+mq.getLabelTypes()+"\n\t"+oq.getLabelTypes()); 
                }
                */
            }
            /*
             * HqlAnalyzeWalker walker = new HqlAnalyzeWalker(); walker.setAllowLogicalExprInSelect(true);
             * walker.setTypeComputer(new MddObjectType()); walker.setDebug(query); walker.statement(hql);
             */
        } catch (Throwable t) {
            if(mqlThr!=null) {
                error(line + ": MQL: " + mqlThr.getMessage() + " " + query);
            }
            
            error(line + ":"+(mqlThr==null?" only in":"")+" OQL: " + t.getMessage() + " " + query);
            if(mqlThr==null)
                error(line+": MQL SQL: "+mql_sql);
            return;
        }
        if(mqlThr!=null){
            error(line + ": only in MQL: " + mqlThr.getMessage() + " " + query);
            if(!(mqlThr instanceof OQLParseError))
                mqlThr.printStackTrace();
        }
    }

    private static void compareMdds(String what, StringBuffer sb, DataDefinition mdd1, DataDefinition mdd2) {
        HashSet<String> fieldsDone= new HashSet<String>();
        for(String s: mdd1.getFieldNames()){
            FieldDefinition fd1= mdd1.getFieldDefinition(s);
            FieldDefinition fd2= mdd2.getFieldDefinition(s);
            
            if(fd2==null){
                sb.append("extra MQL ").append(what).append(": ");
                appendFieldDefinition(sb, fd1);
                sb.append("\n");
                continue;
            }
            if(!fd1.isAssignableFrom(fd2) && !(fd1.getType().equals("boolean")&&fd2.getType().equals("int"))){
                sb.append(what).append(" ").append(s).append(" MQL: ");
                appendFieldDefinition(sb, fd1);
                sb.append(" OQL: ");
                appendFieldDefinition(sb, fd2);
                sb.append("\n");
            }
            fieldsDone.add(s);
        }
        if(mdd2!=null)
        for(String s: mdd2.getFieldNames()){
            if(!fieldsDone.contains(s)){
                FieldDefinition fd2= mdd2.getFieldDefinition(s);
                sb.append("extra OQL ").append(what).append(": ");
                appendFieldDefinition(sb, fd2);
                sb.append("\n");
            }
        }
    }

    private static void appendFieldDefinition(StringBuffer sb, FieldDefinition fd) {
        sb.append(fd.getName()).append("=").append(fd.getType());
    }
    
    private static void error(String s) {
        System.err.println(s);
        errors.add(s);
    }

}
