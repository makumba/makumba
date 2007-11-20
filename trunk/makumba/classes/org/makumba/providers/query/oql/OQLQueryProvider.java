package org.makumba.providers.query.oql;

import java.io.StringReader;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.OQLParseError;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;



public class OQLQueryProvider extends QueryProvider {

    private Configuration config = new Configuration();
        
    private Transaction tr;

    @Override
    public Vector execute(String query, Map args, int offset, int limit) throws LogicException {
        return ((MultipleAttributeParametrizer) queries.getResource(query)).execute(tr, args, offset, limit);
    }

    @Override
    public void close() {
        tr.close();
    }

    @Override
    public void init(String dataSource) {
        super.init(dataSource);
        tr = new TransactionProvider(config).getConnectionTo(dataSource);

    }

    NamedResources queries = new NamedResources("Composed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm, Object hashName) {

            return new MultipleAttributeParametrizer((String) nm);
        }
    });
    
    public static int parsedQueries = NamedResources.makeStaticCache("OQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;
    
        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return OQLQueryProvider.parseQueryFundamental((String) nm);
        }
    }, true);

    @Override
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

    @Override
    public String getPrimaryKeyNotation(String label) {
        return label;
    }

    @Override
    public boolean selectGroupOrOrderAsLabels() {
        return true;
    }
    @Override
    public FieldDefinition getAlternativeField(DataDefinition dd, String fn) {
        return null;
    }

    @Override
    public String transformPointer(String ptrExpr, String fromSection) {
        return ptrExpr;
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
