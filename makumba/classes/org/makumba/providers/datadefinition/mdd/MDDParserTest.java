package org.makumba.providers.datadefinition.mdd;

import java.util.Dictionary;
import java.util.Vector;

import org.makumba.ValidationRule;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.datadefinition.makumba.RecordInfo;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;


public class MDDParserTest {

    public static void main(String[] args) {
        /*
        RecordInfo dd = (RecordInfo) DataDefinitionProvider.getInstance().getDataDefinition("OldTest");
        System.out.println(dd.getFieldDefinition("name3").getPointedType().getName());
        System.out.println(dd.toString());
        
        QueryProvider query = QueryProvider.makeQueryRunner(Configuration.getDefaultDataSourceName(), "oql");
        Vector<Dictionary<String, Object>> res = query.execute("SELECT (lower(t.name) = t.name) as expression FROM OldTest t", null, 0, -1);

        Vector<Dictionary<String, Object>> res2 = query.execute("SELECT e.enum FROM OldTest t, t.testSetInt e", null, 0, -1);

        Vector<Dictionary<String, Object>> res3 = query.execute("SELECT e.subname1 FROM OldTest t, t.name3 e", null, 0, -1);

        Vector<Dictionary<String, Object>> res4 = query.execute("SELECT e.OldTest FROM OldTest t, t.testSet e", null, 0, -1);

        
        */
        
        
        
        DataDefinitionImpl dd = (DataDefinitionImpl) MDDProvider.getMDD("ParserTest");
        
        System.out.println(dd.getFieldDefinition("name3").getType());
        
        for(ValidationRule r : dd.validationRules.values()) {
            
            if(r instanceof ComparisonValidationRule) {
                System.out.println(r.getRuleName() + " : " + r.validate("AAA"));
            }
        }
        
        
        System.out.println("set cplx getSetOwnerFieldName : "+dd.getFieldDefinition("name3").getSubtable().getSetOwnerFieldName());
        System.out.println("set cplx getSetMemberFieldName: "+dd.getFieldDefinition("name3").getSubtable().getSetMemberFieldName());

        System.out.println("set int getSetOwnerFieldName : "+dd.getFieldDefinition("testSetInt").getSubtable().getSetOwnerFieldName());
        System.out.println("set int getSetMemberFieldName: "+dd.getFieldDefinition("testSetInt").getSubtable().getSetMemberFieldName());

        System.out.println("set getSetOwnerFieldName : "+dd.getFieldDefinition("testSet").getSubtable().getSetOwnerFieldName());
        System.out.println("set getSetMemberFieldName: "+dd.getFieldDefinition("testSet").getSubtable().getSetMemberFieldName());


    }
   
    
}
