package test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.makumba.DataDefinition;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.RecordInfo;
import org.makumba.providers.datadefinition.mdd.DataDefinitionImpl;
import org.makumba.providers.datadefinition.mdd.MDDProvider;

public class MDDParserDiff {
    
    public static void main(String[] args) {
        
        Configuration.setPropery("dataSourceConfig", "dataDefinitionProvider", "recordinfo");
        DataDefinition dd = DataDefinitionProvider.getInstance().getDataDefinition("ParserComparison");
        
        
        String old = ((RecordInfo)dd).getStructure();
        File oldFile = new File("old.txt");
        try {
            FileUtils.writeStringToFile(oldFile, old, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Configuration.setPropery("dataSourceConfig", "dataDefinitionProvider", "mdd");
        DataDefinition dd1 = MDDProvider.getMDD("ParserComparison");
        

        String newDD = ((DataDefinitionImpl)dd1).getStructure();
        
        File newFile = new File("new.txt");
        try {
            FileUtils.writeStringToFile(newFile, newDD, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

}
