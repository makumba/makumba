package org.makumba.providers.query.hql;

import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

public class HQLQueryAnalysisProvider implements QueryAnalysisProvider {

    public QueryAnalysis getQueryAnalysis(String query) {
        return getHqlAnalyzer(query);
    }

 
    static public HqlAnalyzer getHqlAnalyzer(String hqlQuery) {
        return (HqlAnalyzer) NamedResources.getStaticCache(parsedHqlQueries).getResource(hqlQuery);
    }
    
    public static int parsedHqlQueries = NamedResources.makeStaticCache("Hibernate HQL parsed queries",
        new NamedResourceFactory() {
            private static final long serialVersionUID = 1L;
        
            protected Object makeResource(Object nm, Object hashName) throws Exception {
                return new HqlAnalyzer((String) nm);
            }
        }, true);
    
    
    


}
