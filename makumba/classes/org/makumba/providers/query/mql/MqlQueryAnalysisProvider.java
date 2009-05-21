package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

public class MqlQueryAnalysisProvider extends QueryAnalysisProvider {
    public static int parsedQueries = NamedResources.makeStaticCache("MQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new MqlQueryAnalysis((String) nm, true, true);
        }
    }, true);

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(query);
    }

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query, String insertIn) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(MqlQueryAnalysis.formatQueryAndInsert(query, insertIn));
    }

    
    @Override
    public boolean selectGroupOrOrderAsLabels() {
        return false;
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
