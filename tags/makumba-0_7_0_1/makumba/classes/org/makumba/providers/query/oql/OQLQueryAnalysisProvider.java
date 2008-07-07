package org.makumba.providers.query.oql;

import java.io.StringReader;
import java.util.Date;

import org.makumba.OQLParseError;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

public class OQLQueryAnalysisProvider implements QueryAnalysisProvider {
    public static int parsedQueries = NamedResources.makeStaticCache("OQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;
    
        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return OQLQueryAnalysisProvider.parseQueryFundamental((String) nm);
        }
    }, true);

    public QueryAnalysis getQueryAnalysis(String query) {
        
        try {
            return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(query);
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof antlr.RecognitionException) {
                Exception f = (antlr.RecognitionException) e.getCause();
                String s = f.getMessage();
                if (s.startsWith("line"))
                    s = s.substring(s.indexOf(':') + 1);
                throw new OQLParseError("\r\nin query:\r\n" + query, f);
            }
            throw e;
        }
    }

    /**
     * Performs the analysis of an OQL query
     * @param oqlQuery the query to analyse
     * @return the OQL analysis correponding to the query
     * @throws antlr.RecognitionException
     */
    public static QueryAnalysis parseQueryFundamental(String oqlQuery) throws antlr.RecognitionException {
        Date d = new Date();
        OQLLexer lexer = new OQLLexer(new StringReader(oqlQuery));
        OQLParser parser = new OQLParser(lexer);
        // Parse the input expression
        QueryAST t = null;
        try {
    
            parser.setASTNodeClass("org.makumba.providers.query.oql.OQLAST");
            parser.queryProgram();
            t = (QueryAST) parser.getAST();
            t.setOQL(oqlQuery);
            // Print the resulting tree out in LISP notation
            // MakumbaSystem.getLogger("debug.db").severe(t.toStringTree());
    
            // see the tree in a window
            /*
             * if(t!=null) { ASTFrame frame = new ASTFrame("AST JTree Example", t); frame.setVisible(true); }
             */
        } catch (antlr.TokenStreamException f) {
            java.util.logging.Logger.getLogger("org.makumba." + "db.query.compilation").warning(f + ": " + oqlQuery);
            throw new org.makumba.MakumbaError(f, oqlQuery);
        }
        long diff = new java.util.Date().getTime() - d.getTime();
        java.util.logging.Logger.getLogger("org.makumba." + "db.query.compilation").fine("OQL to SQL: " + diff + " ms: " + oqlQuery);
        return t;
    }

}
