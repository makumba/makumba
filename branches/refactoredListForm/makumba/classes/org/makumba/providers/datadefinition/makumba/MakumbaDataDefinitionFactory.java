package org.makumba.providers.datadefinition.makumba;

import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class is the Makumba implementation of a data definition provider, based on MDD files. TODO refactor together
 * with RecordInfo to build objects (and not use static methods)
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class MakumbaDataDefinitionFactory extends DataDefinitionProvider {

    private static DataDefinitionProvider singleton = null;

    /**
     * {@inheritdoc} The type a.b.C will generate a lookup for the file CLASSPATH/a/b/C.mdd and then for
     * CLASSPATH/dataDefinitions/a/b/C.mdd
     */
    @Override
    public DataDefinition getDataDefinition(String typeName) {
        return RecordInfo.getRecordInfo(typeName);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public DataDefinition getVirtualDataDefinition(String name) {
        return new RecordInfo(name);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public FieldDefinition makeFieldDefinition(String name, String definition) {
        return FieldInfo.getFieldInfo(name, definition, true);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public FieldDefinition makeFieldOfType(String name, String type) {
        return FieldInfo.getFieldInfo(name, type, false);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return FieldInfo.getFieldInfo(name, type, false, description);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return FieldInfo.getFieldInfo(name, type, false);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return FieldInfo.getFieldInfo(name, type, false, description);
    }

    public static DataDefinitionProvider getInstance() {
        if (singleton == null) {
            singleton = new MakumbaDataDefinitionFactory();
        }
        return singleton;
    }

    private MakumbaDataDefinitionFactory() {

    }

    @Override
    public Vector getDataDefinitionsInLocation(String location) {
        return mddsInDirectory(location);
    }

    /**
     * Discover mdds in a directory in classpath.
     * 
     * @return filenames as Vector of Strings.
     */
    private java.util.Vector mddsInDirectory(String dirInClasspath) {
        java.net.URL u = org.makumba.util.ClassResource.get(dirInClasspath);
        java.io.File dir = new java.io.File(u.getFile());
        java.util.Vector mdds = new java.util.Vector();
        fillMdds(dir.toString().length() + 1, dir, mdds);
        return mdds;
    }

    private void fillMdds(int baselength, java.io.File dir, java.util.Vector mdds) {
        if (dir.isDirectory()) {
            String[] list = dir.list();
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                if (s.endsWith(".mdd")) {
                    s = dir.toString() + java.io.File.separatorChar + s;
                    s = s.substring(baselength, s.length() - 4); // cut off the ".mdd"
                    s = s.replace(java.io.File.separatorChar, '.');
                    mdds.add(s);
                } else {
                    java.io.File f = new java.io.File(dir, s);
                    if (f.isDirectory())
                        fillMdds(baselength, f, mdds);
                }
            }
        }
    }

}
