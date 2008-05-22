package org.makumba.db.makumba;

import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
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
    public Vector execute(String query, Map args, int offset, int limit) {
       return tr.executeQuery(query, args, offset, limit);
    }

    @Override
    public void close() {
        tr.close();
    }

    @Override
    public void init(String dataSource) {
        super.init(dataSource);
        tr = new TransactionProvider(new MakumbaTransactionProvider()).getConnectionTo(dataSource);

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
    
 

}
