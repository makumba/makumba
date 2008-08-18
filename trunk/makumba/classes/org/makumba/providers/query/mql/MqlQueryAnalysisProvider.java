package org.makumba.providers.query.mql;


import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.OQLParseError;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

public class MqlQueryAnalysisProvider extends QueryAnalysisProvider {
    public static int parsedQueries = NamedResources.makeStaticCache("MQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;
    
        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new MqlQueryAnalysis((String)nm, true);
        }
    }, true);

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query) {
        
        try {
            return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(query);
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof antlr.ANTLRException) {
                Exception f = (antlr.ANTLRException) e.getCause();
                String s = f.getMessage();
                if (s.startsWith("line"))
                    s = s.substring(s.indexOf(':') + 1);
                throw new OQLParseError("\r\nin query:\r\n" + query, f);
            }
            throw e;
        }
    }


    @Override
    public boolean selectGroupOrOrderAsLabels() {
        return true;
    }
    @Override
    public FieldDefinition getAlternativeField(DataDefinition dd, String fn) {
        if (fn.equals("id"))
            return dd.getFieldDefinition(dd.getIndexPointerFieldName());
        return null;
    }

    @Override
    public String getPrimaryKeyNotation(String label) {
        return label;
    }
    
    @Override
    public String getParameterSyntax() {
        return "$";
    }

}
