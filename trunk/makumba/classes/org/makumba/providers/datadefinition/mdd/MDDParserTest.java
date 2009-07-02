package org.makumba.providers.datadefinition.mdd;

import java.util.Dictionary;
import java.util.Vector;

import org.makumba.ValidationRule;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.datadefinition.makumba.RecordInfo;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.RegExpValidationRule;


public class MDDParserTest {

    public static void main(String[] args) {
        /*
        Configuration.setDataDefinitionProviderClass("org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory");
        RecordInfo dd = (RecordInfo) DataDefinitionProvider.getInstance().getDataDefinition("OldTest");
        
        QueryProvider query = QueryProvider.makeQueryRunner(Configuration.getDefaultDataSourceName(), "oql");
        Vector<Dictionary<String, Object>> res = query.execute("SELECT (lower(t.name) = t.name) as expression FROM OldTest t", null, 0, -1);

        Vector<Dictionary<String, Object>> res2 = query.execute("SELECT e.enum FROM OldTest t, t.testSetInt e", null, 0, -1);

        Vector<Dictionary<String, Object>> res3 = query.execute("SELECT e.subname1 FROM OldTest t, t.name3 e", null, 0, -1);

        Vector<Dictionary<String, Object>> res4 = query.execute("SELECT e.OldTest FROM OldTest t, t.testSet e", null, 0, -1);

        */
        
        
        
        DataDefinitionImpl dd = (DataDefinitionImpl) MDDProvider.getMDD("ParserTest");
        
        for(ValidationRule r : dd.validationRules.values()) {
            
            if(r instanceof RegExpValidationRule) {
                System.out.println(r.getRuleName() + " : " + r.validate("http://www.com"));
            }
            
            if(r instanceof ComparisonValidationRule) {
                System.out.println(r.getRuleName() + " : " + r.validate("AAA"));
            }
        }

    }
   
    
}
