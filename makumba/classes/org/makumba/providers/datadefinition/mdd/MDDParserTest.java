package org.makumba.providers.datadefinition.mdd;

import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;

public class MDDParserTest {

    public static void main(String[] args) {

        DataDefinitionImpl dd = (DataDefinitionImpl) MDDProvider.getMDD("ParserTest2");
        
        Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());
        t.executeQuery("select s.name from ParserTest2 t, t.test1 s", null);
        t.executeQuery("select s.name from ParserTest2 t, t.test2 s", null);
        
    }
   
    
}
