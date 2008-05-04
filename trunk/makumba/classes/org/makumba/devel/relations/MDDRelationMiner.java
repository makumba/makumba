package org.makumba.devel.relations;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;

public class MDDRelationMiner extends RelationMiner {
    
    public MDDRelationMiner(RelationCrawler rc) {
        super(rc);
    }

    private static final String MDD_PATH = "/WEB-INF/classes/dataDefinitions/";

    @Override
    public void crawl(String path) {
        DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();
        
        String mddPath = path.substring(MDD_PATH.length(), path.length());
        
        if(!new File(rc.getWebappRoot() + File.separator + mddPath).exists()) {
            logger.warning("MDD "+mddPath + " does not exist in webapp "+rc.getWebappRoot());
            return;
        }
        
        String type = path.substring(MDD_PATH.length(), path.length() - 4).replace('/', '.');

        DataDefinition dd = ddp.getDataDefinition(type);

        Vector<String> fields = dd.getFieldNames();
        for (Iterator<String> iterator = fields.iterator(); iterator.hasNext();) {
            String fieldName = iterator.next();
            FieldDefinition fd = dd.getFieldDefinition(fieldName);

            if (fd.getType().equals("ptr") || fd.getType().equals("set")) {
                addMDD2MDDRelation(path, typeToPath(fd.getPointedType().getName()), fd.getName());
            }
        }
        
    }
    

    /**
     * Transforms a MDD name into the relative path to the MDD
     * 
     * @param typeName
     *            the name of the type
     * @return the path relative to the webapp root
     */
    private String typeToPath(String typeName) {
        return MDD_PATH + typeName.replace('.', '/') + ".mdd";
    }

    /**
     * Adds a MDD -> MDD relation
     * 
     * @param fromFile
     *            the file this relation originates from
     * @param toFile
     *            the MDD of the relation
     * @param fromField
     *            the field responsible for the relation
     */
    private void addMDD2MDDRelation(String fromFile, String toFile, String fromField) {

        Dictionary<String, Object> relation = new Hashtable<String, Object>();
        relation.put("fromFile", fromFile);
        relation.put("type", "dependsOn");

        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("field", fromField);

        relation.put("origin", relationOrigin);

        rc.addRelation(toFile, relation);
    }
    


}
