package org.makumba.db.makumba;

import java.util.Map;
import java.util.Vector;

import org.makumba.Transaction;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;



public class OQLQueryProvider extends QueryProvider {
    public static final String OQLQUERY_ANALYSIS_PROVIDER = "org.makumba.providers.query.oql.OQLQueryAnalysisProvider";
    private Transaction tr;

    @Override
    protected String getQueryAnalysisProviderClass() {
        return OQLQUERY_ANALYSIS_PROVIDER;
    }

    
    @Override
    public Vector executeRaw(String query, Map args, int offset, int limit) {
       return tr.executeQuery(query, args, offset, limit);
    }

    @Override
    public void close() {
        tr.close();
    }

    @Override
    protected void init(String dataSource) {
        super.init(dataSource);
        tr = new TransactionProvider(new MakumbaTransactionProvider()).getConnectionTo(dataSource);

    } 
}
