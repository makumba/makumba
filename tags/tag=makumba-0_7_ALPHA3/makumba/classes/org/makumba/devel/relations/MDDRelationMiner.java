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

    private static final String CLASSES_PATH = "WEB-INF/classes/";

    private static final String DEFAULT_DATADEFINITIONS_PATH = "WEB-INF/classes/dataDefinitions/";

    public MDDRelationMiner(RelationCrawler rc) {
        super(rc);
    }

    @Override
    public void crawl(String path) {
        DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

        
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        
        String mddPath;
        if (path.startsWith(DEFAULT_DATADEFINITIONS_PATH)) {
            mddPath = path.substring(DEFAULT_DATADEFINITIONS_PATH.length());
        } else if (path.startsWith(CLASSES_PATH)) {
            mddPath = path.substring(CLASSES_PATH.length());
        } else {
            System.out.println("\nIgnoring MDD not in default MDD path (" + CLASSES_PATH + " or "
                    + DEFAULT_DATADEFINITIONS_PATH + "): " + path);
            return;
        }

        if (!new File(rc.getWebappRoot() + File.separator + path).exists()) {
            logger.warning("MDD " + mddPath + " does not exist in webapp " + rc.getWebappRoot());
            return;
        }

        String type = path.substring(DEFAULT_DATADEFINITIONS_PATH.length(), path.length() - 4).replace('/', '.');
        try {
            DataDefinition dd = ddp.getDataDefinition(type);

            Vector<String> fields = dd.getFieldNames();
            for (Iterator<String> iterator = fields.iterator(); iterator.hasNext();) {
                String fieldName = iterator.next();
                FieldDefinition fd = dd.getFieldDefinition(fieldName);

                if (fd.getType().equals("ptr") || fd.getType().equals("set")) {
                    addMDD2MDDRelation(path, typeToPath(fd.getPointedType().getName()), fd.getName());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
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
        return DEFAULT_DATADEFINITIONS_PATH + typeName.replace('.', '/') + ".mdd";
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
        Dictionary<String, Object> relationOrigin = new Hashtable<String, Object>();
        relationOrigin.put("field", fromField);
        rc.addRelation(fromFile, toFile, relationOrigin);
    }
}