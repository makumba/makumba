package org.makumba.providers.datadefinition.mdd;

import org.makumba.commons.NameResolver;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class MDDParserTest {

    public static void main(String[] args) {

        DataDefinitionImpl dd = (DataDefinitionImpl) MDDProvider.getMDD("ParserTest");
        
        QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer(TransactionProvider.getInstance().getQueryLanguage());
        QueryAnalysis qA = qap.getQueryAnalysis("SELECT 1+1, 2-2, (SELECT o.name FROM t.other o) as test, case when 1=1 then 1 else 0 end FROM ParserTest t WHERE 1 = 1 and 2 = 2 order by o.name asc, 2 desc");
        System.out.println(qA.writeInSQLQuery(new NameResolver()));
        
        //Transaction t = TransactionProvider.getInstance().getConnectionTo(TransactionProvider.getInstance().getDefaultDataSourceName());
        //t.executeQuery("select s.name from ParserTest t, t.test1 s", null);
        //t.executeQuery("select s.name from ParserTest t, t.test2 s", null);
        
    }
   
    
}
