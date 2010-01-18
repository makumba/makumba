package org.makumba.providers.datadefinition.mdd;

import org.makumba.commons.NameResolver;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class MDDParserTest {

    public static void main(String[] args) {

        DataDefinitionImpl dd = (DataDefinitionImpl) MDDProvider.getMDD("ParserTest");
        
        //Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());
        //t.executeQuery("select s.name from ParserTest t, t.test1 s", null);
        //t.executeQuery("select s.name from ParserTest t, t.test2 s", null);
        
    }
   
    
}
