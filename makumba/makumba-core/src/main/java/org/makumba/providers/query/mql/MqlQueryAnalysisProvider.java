package org.makumba.providers.query.mql;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

import antlr.collections.AST;

public class MqlQueryAnalysisProvider extends QueryAnalysisProvider {

    private static final long serialVersionUID = 1L;

    public static int parsedQueries = NamedResources.makeStaticCache("MQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        @Override
        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new MqlQueryAnalysis((String) nm, true, true);
        }
    }, true);

    @Override
    public String getName() {
        return "oql";
    }

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(query);
    }

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query, String insertIn) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(
            MqlQueryAnalysis.formatQueryAndInsert(query, insertIn));
    }

    @Override
    public FieldDefinition getAlternativeField(DataDefinition dd, String fn) {
        if (fn.equals("id")) {
            return dd.getFieldDefinition(dd.getIndexPointerFieldName());
        }
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

    @Override
    public QueryAnalysis getQueryAnalysis(AST pass1, DataDefinition knownLabels) {
        return new MqlQueryAnalysis(pass1, knownLabels);
    }

}
